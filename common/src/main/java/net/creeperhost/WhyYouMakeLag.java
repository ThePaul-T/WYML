package net.creeperhost;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    private static AtomicReference<HashMap<String, WYMLSpawnManager>> spawnManager = new AtomicReference<>();
    public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    public static Logger LOGGER = LogManager.getLogger();

    public static void init()
    {

    }
}
