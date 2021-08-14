package net.creeperhost.wyml.wyml;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class WhyYouMakeLag
{
    public static final String MOD_ID = "wyml";
    public static int realMax = 0;
    public static int trueCount = 0;
    public static MinecraftServer minecraftServer;
    public static int ticks = 0;
    public static Object2IntOpenHashMap<EntityClassification> mobCategoryCounts;
    public static HashMap<EntityClassification, Integer> spawnableChunkCount = new HashMap<>();
    public static boolean ITWORKS = false;
    public static AtomicReference<HashMap<String, Integer>> FAIL_COUNT = new AtomicReference<>();
    public static int lastTick = 0;
    public static int lastTick2 = 0;
    public static int mobsInTick = 0;

    public WhyYouMakeLag()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        FAIL_COUNT.set(new HashMap<String, Integer>());

        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(this::serverTick);
        MinecraftForge.EVENT_BUS.addListener(this::spawnEvent);

        WymlConfig.loadConfig(WymlConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-common.toml"));

    }

    public void serverStarted(FMLServerStartedEvent event)
    {
        minecraftServer = event.getServer();
    }

    public void serverTick(TickEvent.ServerTickEvent tickEvent)
    {
        ticks++;
    }

    public void spawnEvent(EntityJoinWorldEvent event)
    {
//        System.out.println("WhyYouMakeLag.mobsInTick " + WhyYouMakeLag.mobsInTick);


        if(event.getEntity() instanceof MobEntity)
        {
            MobEntity mobEntity = (MobEntity) event.getEntity();
            ChunkPos chunkPos = new ChunkPos(mobEntity.blockPosition().getX(), mobEntity.blockPosition().getZ());

            if (mobEntity != null && mobEntity.isAlive() && mobEntity.level != null)
            {
                WhyYouMakeLag.mobsInTick++;

                EntityClassification entityClassification = mobEntity.getClassification(true);
                String id = chunkPos + entityClassification.getName();

//                System.out.println("SpawnEventTriggered " + mobEntity.getEntity().getName().getString() + " mobsInTicks: " + WhyYouMakeLag.mobsInTick + " FailCount ["+id+"]: " + WhyYouMakeLag.FAIL_COUNT.get().get(id));

                if (WhyYouMakeLag.FAIL_COUNT.get().containsKey(id))
                {
                    WhyYouMakeLag.FAIL_COUNT.getAndUpdate((thing) -> {
                        if(thing.containsKey(id)) thing.put(id, thing.get(id)-1);
                        return thing;
                    });
                }
            }
        }
    }

    public static int calculateSpawnCount(EntityClassification entityClassification, Object2IntOpenHashMap<EntityClassification> mobCategoryCounts, int spawnableChunkCount)
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
            int tries = WymlConfig.MOB_TRIES.get();
            if(WymlConfig.MULTIPLY_BY_PLAYERS.get()) tries = (tries * onlineCount);
            retVal = curMobs + tries;
        }
        if(retVal > WhyYouMakeLag.realMax) retVal = WhyYouMakeLag.realMax;
        return retVal;
    }

    public static boolean shouldSpawn(EntityClassification entityClassification, Object2IntOpenHashMap<EntityClassification> mobCategoryCounts, int spawnableChunkCount)
    {
        int retVal = calculateSpawnCount(entityClassification, mobCategoryCounts, spawnableChunkCount);
        int curMobs = mobCategoryCounts.getInt(entityClassification);

        boolean value = curMobs < retVal;

        if(value) WhyYouMakeLag.trueCount++;

        return value;
    }

}
