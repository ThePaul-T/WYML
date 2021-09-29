package net.creeperhost.wyml;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.Env;
import net.creeperhost.wyml.compat.CompatFTBChunks;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.creeperhost.wyml.init.WYMLContainers;
import net.creeperhost.wyml.init.WYMLScreens;
import net.creeperhost.wyml.mixins.AccessorMinecraftServer;
import net.creeperhost.wyml.network.PacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
    public static AtomicReference<List<Long>> cachedClaimedChunks = new AtomicReference<>();
    public static AtomicReference<List<Long>> cachedForceLoadedChunks = new AtomicReference<>();
    private static AtomicReference<HashMap<String, WYMLSpawnManager>> spawnManager = new AtomicReference<>();
    public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    public static ScheduledExecutorService scheduledExecutorService2 = Executors.newScheduledThreadPool(1);
    public static Logger LOGGER = LogManager.getLogger();
    public static Path configFile = WymlExpectPlatform.getConfigDirectory().resolve(MOD_ID + ".json");

    public static long tickStartNano;
    public static long tickStopNano;

    public static void init()
    {
        WymlConfig.init(configFile.toFile());
        WYMLBlocks.init();
        WYMLContainers.MENUS.register();
        PacketHandler.init();

        if (Platform.getEnvironment() == Env.CLIENT)
        {
            ClientLifecycleEvent.CLIENT_SETUP.register(WhyYouMakeLag::onClientSetup);
        }

        if (spawnManager.get() == null) spawnManager.set(new HashMap<String, WYMLSpawnManager>());
        if (cachedClaimedChunks.get() == null) cachedClaimedChunks.set(new ArrayList<Long>());
        if (cachedForceLoadedChunks.get() == null) cachedForceLoadedChunks.set(new ArrayList<Long>());
    }

    @Environment(EnvType.CLIENT)
    private static void onClientSetup(Minecraft minecraft)
    {
        WYMLScreens.init();
    }

    public static List<ChunkHolder> shuffle(final List<ChunkHolder> input)
    {
        if (input.isEmpty()) return input;


        WYMLRandom.generate(0, input.size() - 1, input.size());
        final List<ChunkHolder> copy = new ArrayList<>(input);
        for (int i = 0; i < copy.size(); i++)
        {
            try
            {
                //TODO: If this performs worse, just switch back to a normal random
                int random = WYMLRandom.get();
                //int random = ThreadLocalRandom.current().nextInt(0, copy.size());

                copy.set(random, copy.get(i));
                copy.set(i, input.get(random));
            } catch (Exception ignored)
            {
            }
        }
        return copy;
    }


    public static boolean isFtbChunksLoaded()
    {
        return WymlExpectPlatform.isModLoaded("ftbchunks");
    }

    public static LongSet getForceLoadedChunks()
    {
        if (minecraftServer == null) return null;
        if (minecraftServer.getLevel(Level.OVERWORLD) == null) return null;
        return minecraftServer.getLevel(Level.OVERWORLD).getForcedChunks();
    }

    public static int getTicks()
    {
        return ((AccessorMinecraftServer) minecraftServer).getTickCount();
    }

    public static void serverStopping()
    {
        scheduledExecutorService2.shutdown();
        scheduledExecutorService2.shutdownNow();
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
        spawnManager.getAndUpdate((existing) ->
        {
            existing.remove(id);
            return existing;
        });
    }

    @SuppressWarnings("unused")
    public synchronized static void removeSpawnManager(WYMLSpawnManager manager)
    {
        String id = manager.chunk + manager.classification.getName();
        spawnManager.getAndUpdate((existing) ->
        {
            existing.remove(id);
            return existing;
        });
    }

    public synchronized static WYMLSpawnManager getSpawnManager(ChunkPos pos, MobCategory classification)
    {
        String id = pos + classification.getName();
        if (spawnManager.get().containsKey(id))
        {
            return spawnManager.get().get(id);
        }
        return new WYMLSpawnManager(pos, classification);
    }

    public static double getMagicNum()
    {
        double magicNum = WymlConfig.cached().MOJANG_MAGIC_NUM;
        if (WymlConfig.cached().DOWNSCALE_MAGIC_NUM)
        {
            int players = WhyYouMakeLag.minecraftServer.getPlayerList().getPlayerCount();
            magicNum = magicNum - players;
            if (magicNum < WymlConfig.cached().DOWNSCALE_MAGIC_NUM_MIN)
                magicNum = WymlConfig.cached().DOWNSCALE_MAGIC_NUM_MIN;
        }
        return magicNum;
    }

    public synchronized static void updateSpawnManager(WYMLSpawnManager manager)
    {
        if (manager.isSaved()) return;
        String id = manager.chunk + manager.classification.getName();
        spawnManager.getAndUpdate((existing) ->
        {
            manager.isSaving();
            existing.put(id, manager);
            return existing;
        });
    }

    public static void serverStarted(MinecraftServer minecraftServer)
    {
        if (scheduledExecutorService.isShutdown()) scheduledExecutorService = Executors.newScheduledThreadPool(1);
        if (scheduledExecutorService2.isShutdown()) scheduledExecutorService2 = Executors.newScheduledThreadPool(1);

        WhyYouMakeLag.minecraftServer = minecraftServer;
        if (WymlConfig.cached().ALLOW_PAPER_BAGS) BagHandler.create();

        Runnable cleanThread = () ->
        {
            try
            {
                if (WhyYouMakeLag.isFtbChunksLoaded())
                {
                    List<Long> pos = CompatFTBChunks.getChunkPosList();
                    cachedClaimedChunks.set(pos);
                }
                List<Long> forceLoaded = new ArrayList<>();
                if (getForceLoadedChunks() != null)
                {
                    getForceLoadedChunks().stream().iterator().forEachRemaining(forceLoaded::add);
                }
                cachedForceLoadedChunks.set(forceLoaded);
                int managersRemoved = 0;
                int managersTotal = 0;
                int blockCacheRemoved = 0;
                int blockCacheTotal = 0;
                HashMap<String, WYMLSpawnManager> spawnManagers = new HashMap<>(spawnManager.get());
                List<String> toRemove = new ArrayList<String>();
                Set<String> ids = spawnManagers.keySet();
                for (String id : ids)
                {
                    managersTotal++;
                    WYMLSpawnManager sm = spawnManagers.get(id);
                    if (sm.hasExpired())
                    {
                        toRemove.add(id);
                    }
                    else
                    {
                        blockCacheTotal += sm.countBlockCache();
                        int amRemoved = sm.cleanBlockCache();
                        if (amRemoved > 0 || !sm.isSaved())
                        {
                            updateSpawnManager(sm);
                        }
                        blockCacheRemoved += amRemoved;
                    }
                }
                spawnManagers.clear();
                for (String id : toRemove)
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
                    try
                    {
                        //usage = MemoryMeasurer.measureBytes(penis);
                    } catch (Throwable t)
                    {
                        t.printStackTrace();
                    }
                    if (WymlConfig.cached().CLEAN_PRINT)
                        LOGGER.info("Cleaned up caches, removed " + managersRemoved + "/" + managersTotal + " Chunk SpawnManagers and " + blockCacheRemoved + "/" + blockCacheTotal + " block spawn caches. [" + usage + "]");
                    //}
                }
            } catch (Exception ignored)
            {
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(cleanThread, 0, 10, TimeUnit.SECONDS);
    }

    public static int calculateSpawnCount(MobCategory entityClassification, Object2IntOpenHashMap<MobCategory> mobCategoryCounts, int spawnableChunkCount)
    {
        if (WhyYouMakeLag.minecraftServer == null) return 0;
        WhyYouMakeLag.mobCategoryCounts = mobCategoryCounts;
        WhyYouMakeLag.spawnableChunkCount.put(entityClassification, spawnableChunkCount);

        MinecraftServer minecraftServer = WhyYouMakeLag.minecraftServer;
        int onlineCount = minecraftServer.getPlayerList().getPlayerCount();

        int i = entityClassification.getMaxInstancesPerChunk() * spawnableChunkCount / (int) Math.pow(17.0D, 2.0D);
        WhyYouMakeLag.realMax = i;
        int retVal = 0;
        int curMobs = mobCategoryCounts.getInt(entityClassification);
        if (curMobs < i)
        {
            int tries = WymlConfig.cached().MOB_TRIES;
            if (WymlConfig.cached().MULTIPLY_BY_PLAYERS) tries = (tries * onlineCount);
            retVal = curMobs + tries;
        }
        if (retVal > WhyYouMakeLag.realMax) retVal = WhyYouMakeLag.realMax;
        return retVal;
    }

    public static boolean shouldSpawn(MobCategory entityClassification, Object2IntOpenHashMap<MobCategory> mobCategoryCounts, int spawnableChunkCount)
    {
        int retVal = calculateSpawnCount(entityClassification, mobCategoryCounts, spawnableChunkCount);
        int curMobs = mobCategoryCounts.getInt(entityClassification);

        boolean value = curMobs < retVal;

        if (value) WhyYouMakeLag.trueCount++;

        return value;
    }

}
