package net.creeperhost.wyml;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.creeperhost.polylib.event.events.server.PolyServerLifecycleEvents;
import net.creeperhost.polylib.platform.Services;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.config.WymlBootConfig;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.creeperhost.wyml.init.WYMLContainers;
import net.creeperhost.wyml.init.WYMLScreens;
import net.creeperhost.wyml.mixins.AccessorMinecraftServer;
import net.creeperhost.wyml.spawn.AttemptBudgetPolicy;
import net.creeperhost.wyml.spawn.CategoryCapPolicy;
import net.creeperhost.wyml.spawn.ControllerKey;
import net.creeperhost.wyml.spawn.MobPopulationIndex;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class WhyYouMakeLag
{
    public static final String MOD_ID = "wyml";
    public static int realMax = 0;
    public static int trueCount = 0;
    public static MinecraftServer minecraftServer;
    private static final Map<MinecraftServer, Map<ControllerKey, ChunkManager>> controllerRegistries = new IdentityHashMap<>();
    public static ScheduledExecutorService scheduledExecutorService;
    public static ScheduledExecutorService scheduledExecutorService2;
    public static Logger LOGGER = LogManager.getLogger();
    public static Path configFile = Services.PLATFORM.getConfigFolder().resolve(MOD_ID + ".json");

    public static void init()
    {
        WymlConfig.init(configFile.toFile());
        WYMLBlocks.init();
        WYMLContainers.init();
        if (Services.PLATFORM.isClient())
        {
            WYMLScreens.init();
        }

        if (!WymlConfig.isEnabled())
        {
            LOGGER.info("WYML feature runtime is disabled; compatibility content remains registered.");
            return;
        }

        createExecutors();
        WymlConfig.startWatcher(scheduledExecutorService2);

        if (WymlBootConfig.moduleEnabled("spawn_controller")
                || WymlBootConfig.moduleEnabled("per_mob_rules")
                || WymlBootConfig.moduleEnabled("paper_bags"))
        {
            PolyServerLifecycleEvents.SERVER_STARTED.register(WhyYouMakeLag::serverStarted);
        }
        PolyServerLifecycleEvents.SERVER_STOPPING.register(WhyYouMakeLag::serverStopping);
    }

    public static List<ChunkHolder> shuffle(final List<ChunkHolder> input)
    {
        if (input.isEmpty()) return input;


        final List<ChunkHolder> copy = new ArrayList<>(input);
        for (int i = 0; i < copy.size(); i++)
        {
            try
            {
                int random = ThreadLocalRandom.current().nextInt(0, copy.size());

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
        return Services.PLATFORM.isModLoaded("ftbchunks");
    }

    public static int getTicks(MinecraftServer server)
    {
        return server == null ? 0 : ((AccessorMinecraftServer) server).getTickCount();
    }
    public static void serverStopping(MinecraftServer server)
    {
        WymlConfig.stopWatcher();
        if (scheduledExecutorService2 != null) scheduledExecutorService2.shutdownNow();
        if (scheduledExecutorService != null) scheduledExecutorService.shutdownNow();
        synchronized (controllerRegistries)
        {
            controllerRegistries.remove(server);
        }
        MobPopulationIndex.clear(server);
        if (minecraftServer == server) minecraftServer = null;
    }

    public synchronized static boolean hasChunkManager(ServerLevel level, ChunkPos pos, MobCategory classification)
    {
        Map<ControllerKey, ChunkManager> registry = controllerRegistries.get(level.getServer());
        return registry != null && registry.containsKey(ControllerKey.of(level, pos, classification));
    }

    public synchronized static void removeChunkManager(MinecraftServer server, ControllerKey key)
    {
        Map<ControllerKey, ChunkManager> registry = controllerRegistries.get(server);
        if (registry != null) registry.remove(key);
    }

    @SuppressWarnings("unused")
    public synchronized static void removeChunkManager(ChunkManager manager)
    {
        removeChunkManager(manager.getLevel().getServer(), manager.getKey());
    }

    public synchronized static ChunkManager getChunkManager(ServerLevel level, ChunkPos pos, MobCategory classification)
    {
        Map<ControllerKey, ChunkManager> registry = controllerRegistries.computeIfAbsent(level.getServer(), ignored -> new HashMap<>());
        ControllerKey key = ControllerKey.of(level, pos, classification);
        return registry.computeIfAbsent(key, ignored -> new ChunkManager(level, pos, classification));
    }

    public static double getCategoryCapRadius()
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

    public synchronized static void updateChunkManager(ChunkManager manager)
    {
        if (manager.isSaved()) return;
        Map<ControllerKey, ChunkManager> registry = controllerRegistries.computeIfAbsent(manager.getLevel().getServer(), ignored -> new HashMap<>());
        manager.isSaving();
        registry.put(manager.getKey(), manager);
    }

    public static void serverStarted(MinecraftServer minecraftServer)
    {
        if (!WymlConfig.isEnabled()) return;
        createExecutors();
        WymlConfig.startWatcher(scheduledExecutorService2);

        WhyYouMakeLag.minecraftServer = minecraftServer;
        synchronized (controllerRegistries)
        {
            controllerRegistries.computeIfAbsent(minecraftServer, ignored -> new HashMap<>());
        }
        if (WymlBootConfig.moduleEnabled("paper_bags") && WymlConfig.cached().ALLOW_PAPER_BAGS) BagHandler.create();

        if (WymlBootConfig.moduleEnabled("per_mob_rules"))
        {
            CompletableFuture.runAsync(MobManager::init).thenRun(() ->
                    LOGGER.info("Finished preparing WYML per-mod per-category per-mob configurations."));
        }

        MinecraftServer startedServer = minecraftServer;
        Runnable cleanThread = () ->
        {
            if (startedServer.isRunning())
            {
                startedServer.execute(() -> cleanControllers(startedServer));
            }
        };
        if (WymlBootConfig.moduleEnabled("spawn_controller"))
            scheduledExecutorService.scheduleAtFixedRate(cleanThread, 0, 10, TimeUnit.SECONDS);
    }

    public static int getAttemptBudget(ServerLevel level)
    {
        return AttemptBudgetPolicy.resolve(
                WymlConfig.cached().ATTEMPT_BUDGET_PER_WINDOW,
                WymlConfig.cached().MOB_TRIES,
                WymlConfig.cached().ATTEMPT_BUDGET_PLAYER_SCALING,
                WymlConfig.cached().MULTIPLY_BY_PLAYERS,
                level.getServer().getPlayerList().getPlayerCount());
    }

    private static void cleanControllers(MinecraftServer server)
    {
        int managersRemoved = 0;
        int managersTotal = 0;
        int blockCacheRemoved = 0;
        int blockCacheTotal = 0;
        Map<ControllerKey, ChunkManager> snapshot;
        synchronized (controllerRegistries)
        {
            Map<ControllerKey, ChunkManager> registry = controllerRegistries.get(server);
            if (registry == null) return;
            snapshot = new HashMap<>(registry);
        }

        List<ControllerKey> toRemove = new ArrayList<>();
        for (Map.Entry<ControllerKey, ChunkManager> entry : snapshot.entrySet())
        {
            managersTotal++;
            ChunkManager manager = entry.getValue();
            if (manager.hasExpired())
            {
                toRemove.add(entry.getKey());
            }
            else
            {
                blockCacheTotal += manager.countBlockCache();
                int removed = manager.cleanBlockCache();
                if (removed > 0 || !manager.isSaved()) updateChunkManager(manager);
                blockCacheRemoved += removed;
            }
        }
        for (ControllerKey key : toRemove)
        {
            removeChunkManager(server, key);
            managersRemoved++;
        }
        if (WymlConfig.cached().CLEAN_PRINT)
        {
            LOGGER.info("Cleaned up controllers, removed " + managersRemoved + "/" + managersTotal
                    + " Chunk SpawnManagers and " + blockCacheRemoved + "/" + blockCacheTotal + " block spawn caches.");
        }
    }

    private static synchronized void createExecutors()
    {
        if (scheduledExecutorService == null || scheduledExecutorService.isShutdown())
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
        if (scheduledExecutorService2 == null || scheduledExecutorService2.isShutdown())
            scheduledExecutorService2 = Executors.newScheduledThreadPool(1);
    }

    public static int calculateCategoryCap(MobCategory category, int spawnableChunkCount)
    {
        int cap = CategoryCapPolicy.calculate(
                category.getMaxInstancesPerChunk(), spawnableChunkCount, getCategoryCapRadius());
        WhyYouMakeLag.realMax = cap;
        return cap;
    }

    public static boolean shouldSpawn(MobCategory entityClassification, Object2IntOpenHashMap<MobCategory> mobCategoryCounts, int spawnableChunkCount)
    {
        int cap = calculateCategoryCap(entityClassification, spawnableChunkCount);
        int curMobs = mobCategoryCounts.getInt(entityClassification);

        boolean value = curMobs < cap;

        if (value) WhyYouMakeLag.trueCount++;

        return value;
    }

}
