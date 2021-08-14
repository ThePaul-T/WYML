package net.creeperhost.wyml;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.shedaniel.architectury.platform.Platform;
import net.creeperhost.wyml.mixins.AccessorMinecraftServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WhyYouMakeLag
{
    public static final String MOD_ID = "wyml";
    public static int realMax = 0;
    public static int trueCount = 0;
    public static MinecraftServer minecraftServer;
    public static Object2IntOpenHashMap<MobCategory> mobCategoryCounts;
    public static HashMap<MobCategory, Integer> spawnableChunkCount = new HashMap<>();
    private static AtomicReference<HashMap<String, WYMLSpawnManager>> spawnManager = new AtomicReference<>();
    public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    public static Logger LOGGER = LogManager.getLogger();
    public static Path configFile = Platform.getConfigFolder().resolve(MOD_ID + ".json");


    public static void init()
    {
        WymlConfig.init(configFile.toFile());

        if(spawnManager.get() == null) spawnManager.set(new HashMap<String, WYMLSpawnManager>());
    }

    public static boolean isFtbChunksLoaded()
    {
        try
        {
            Class.forName("dev.ftb.mods.ftbchunks.FTBChunks");
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public static int getTicks()
    {
        return ((AccessorMinecraftServer) minecraftServer).getTickCount();
    }

    public static void serverStopping()
    {
        scheduledExecutorService.shutdown();
        scheduledExecutorService.shutdownNow();
    }

    public synchronized static boolean hasSpawnManager(ChunkPos pos, MobCategory classification)
    {
        String id = pos + classification.getName();
        return spawnManager.get().containsKey(id);
    }
    public synchronized static void removeSpawnManager(String id)
    {
        spawnManager.getAndUpdate((existing) -> {
            existing.remove(id);
            return existing;
        });
    }
    public synchronized static void removeSpawnManager(WYMLSpawnManager manager)
    {
        String id = manager.chunk + manager.classification.getName();
        spawnManager.getAndUpdate((existing) -> {
            existing.remove(id);
            return existing;
        });
    }
    public synchronized static WYMLSpawnManager getSpawnManager(ChunkPos pos, MobCategory classification)
    {
        String id = pos + classification.getName();
        if(spawnManager.get().containsKey(id))
        {
            return spawnManager.get().get(id);
        }
        return new WYMLSpawnManager(pos, classification);
    }
    public static double getMagicNum()
    {
        double magicNum = WymlConfig.instance.MOJANG_MAGIC_NUM.get();
        if(WymlConfig.instance.DOWNSCALE_MAGIC_NUM.get())
        {
            int players = WhyYouMakeLag.minecraftServer.getPlayerList().getPlayerCount();
            magicNum = magicNum - players;
            if(magicNum < WymlConfig.instance.DOWNSCALE_MAGIC_NUM_MIN.get()) magicNum = WymlConfig.instance.DOWNSCALE_MAGIC_NUM_MIN.get();
        }
        return magicNum;
    }
    public synchronized static void updateSpawnManager(WYMLSpawnManager manager)
    {
        if(manager.isSaved()) return;
        String id = manager.chunk + manager.classification.getName();
        spawnManager.getAndUpdate((existing) -> {
            manager.isSaving();
            existing.put(id, manager);
            return existing;
        });
    }

    public static void serverStarted(MinecraftServer minecraftServer)
    {
        //TODO get server via mixin
        WhyYouMakeLag.minecraftServer = minecraftServer;
        Runnable cleanThread = () ->
        {
            try {
                int managersRemoved = 0;
                int managersTotal = 0;
                int blockCacheRemoved = 0;
                int blockCacheTotal = 0;
                List<String> toRemove = new ArrayList<String>();
                Set<String> ids = spawnManager.get().keySet();
                for (String id : ids) {
                    managersTotal++;
                    WYMLSpawnManager sm = spawnManager.get().get(id);
                    if (sm.hasExpired()) {
                        toRemove.add(id);
                    } else {
                        blockCacheTotal += sm.countBlockCache();
                        int amRemoved = sm.cleanBlockCache();
                        if (amRemoved > 0 || !sm.isSaved()) {
                            updateSpawnManager(sm);
                        }
                        blockCacheRemoved += amRemoved;
                    }
                }
                for(String id : toRemove)
                {
                    removeSpawnManager(id);
                    managersRemoved = managersRemoved + 1;
                }
                if (true)//WymlConfig.DEBUG_PRINT.get())
                {
                    //if (managersRemoved > 0 || blockCacheRemoved > 0)
                    //{
                    //HashMap<String, WYMLSpawnManager> penis = spawnManager.get();
                    long usage = 0;
                    try {
                        //usage = MemoryMeasurer.measureBytes(penis);
                    } catch(Throwable t)
                    {
                        t.printStackTrace();
                    }
                    if(WymlConfig.instance.CLEAN_PRINT.get()) LOGGER.info("Cleaned up caches, removed " + managersRemoved + "/" + managersTotal + " Chunk SpawnManagers and " + blockCacheRemoved + "/" + blockCacheTotal + " block spawn caches. ["+usage+"]");
                    //}
                }
            } catch (Exception whatthefuck) {
                whatthefuck.printStackTrace();
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(cleanThread, 0, 10, TimeUnit.SECONDS);
    }

//    public void serverTick(TickEvent.ServerTickEvent tickEvent)
//    {
//        ticks++;
//    }
//
//    public void spawnEvent(EntityJoinWorldEvent event)
//    {
//        if(WymlConfig.DEBUG_PRINT.get()) {
//            if (event.getEntity() instanceof SlimeEntity) {
//                event.setCanceled(true);
//            }
//        }
//    }

    public static int calculateSpawnCount(MobCategory entityClassification, Object2IntOpenHashMap<MobCategory> mobCategoryCounts, int spawnableChunkCount)
    {
        if(WhyYouMakeLag.minecraftServer == null) return 0;
        WhyYouMakeLag.mobCategoryCounts = mobCategoryCounts;
        WhyYouMakeLag.spawnableChunkCount.put(entityClassification, spawnableChunkCount);

        MinecraftServer minecraftServer = WhyYouMakeLag.minecraftServer;
        int onlineCount = minecraftServer.getPlayerList().getPlayerCount();

        int i = entityClassification.getMaxInstancesPerChunk() * spawnableChunkCount / (int)Math.pow(17.0D, 2.0D);
        WhyYouMakeLag.realMax = i;
        int retVal = 0;
        int curMobs = mobCategoryCounts.getInt(entityClassification);
        if(curMobs < i)
        {
            int tries = WymlConfig.instance.MOB_TRIES.get();
            if(WymlConfig.instance.MULTIPLY_BY_PLAYERS.get()) tries = (tries * onlineCount);
            retVal = curMobs + tries;
        }
        if(retVal > WhyYouMakeLag.realMax) retVal = WhyYouMakeLag.realMax;
        return retVal;
    }

    public static boolean shouldSpawn(MobCategory entityClassification, Object2IntOpenHashMap<MobCategory> mobCategoryCounts, int spawnableChunkCount)
    {
        int retVal = calculateSpawnCount(entityClassification, mobCategoryCounts, spawnableChunkCount);
        int curMobs = mobCategoryCounts.getInt(entityClassification);

        boolean value = curMobs < retVal;

        if(value) WhyYouMakeLag.trueCount++;

        return value;
    }

}
