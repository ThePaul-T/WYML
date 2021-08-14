package net.creeperhost.wyml;

public class WymlConfig
{
    public static final String CATEGORY_GENERAL = "general";
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec.ConfigValue<Integer> MOB_TRIES;
    public static ForgeConfigSpec.ConfigValue<Boolean> MULTIPLY_BY_PLAYERS;
    public static ForgeConfigSpec.ConfigValue<Integer> SLOW_TICKS;
    public static ForgeConfigSpec.ConfigValue<Integer> PAUSE_TICKS;
    public static ForgeConfigSpec.ConfigValue<Integer> PAUSE_RATE;
    public static ForgeConfigSpec.ConfigValue<Integer> RESUME_RATE;
    public static ForgeConfigSpec.ConfigValue<Double>  MOJANG_MAGIC_NUM;
    public static ForgeConfigSpec.ConfigValue<Integer> PAUSE_MIN;
    public static ForgeConfigSpec.ConfigValue<Integer> SAMPLE_TICKS;
    public static ForgeConfigSpec.ConfigValue<Integer> SPAWNLOC_CACHE_TICKS;
    public static ForgeConfigSpec.ConfigValue<Integer> MANAGER_CACHE_TICKS;
    public static ForgeConfigSpec.ConfigValue<Boolean> ALLOW_PAUSE;
    public static ForgeConfigSpec.ConfigValue<Boolean> ALLOW_SLOW;
    public static ForgeConfigSpec.ConfigValue<Boolean> DEBUG_PRINT;
    public static ForgeConfigSpec.ConfigValue<Boolean> CLEAN_PRINT;
    public static ForgeConfigSpec.ConfigValue<Boolean> DOWNSCALE_MAGIC_NUM;
    public static ForgeConfigSpec.ConfigValue<Double>  DOWNSCALE_MAGIC_NUM_MIN;
    public static ForgeConfigSpec.ConfigValue<Integer> MAX_CHUNK_SPAWN_REQ_TICK;

    static
    {
        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        COMMON_BUILDER.pop();

        MOB_TRIES = COMMON_BUILDER.comment("Set the max amount of spawn tries base rate")
                .defineInRange("maxSpawnsRate", 1, 1,  100);

        MULTIPLY_BY_PLAYERS = COMMON_BUILDER.comment("Set to true to multiply base rate by online player count")
                .define("multiplyByPlayers", true);

        MOJANG_MAGIC_NUM = COMMON_BUILDER.comment("Replace Mojang magic number (pow2) with this")
                .defineInRange("mojangMagicNum", 17D, 1D,  50D);

        DOWNSCALE_MAGIC_NUM = COMMON_BUILDER.comment("Downscale the Mojang Magic Number by the online player count")
                .define("mojangMagicNumScale", true);

        DOWNSCALE_MAGIC_NUM_MIN = COMMON_BUILDER.comment("If downscaling enabled, do not allow below this value")
                .defineInRange("mojangMagicNumScaleMin", 8D, 1D,  50D);

        PAUSE_RATE = COMMON_BUILDER.comment("At what what percentage of failed spawns should we then pause spawning in a chunk")
                .defineInRange("pauseRate", 65, 10,  99);

        RESUME_RATE = COMMON_BUILDER.comment("At what what percentage of successful spawns, after we pause spawning, should we resume spawning")
                .defineInRange("resumeRate", 10, 1,  99);

        PAUSE_TICKS = COMMON_BUILDER.comment("How long to pause spawning if pause spawn failure rate reached")
                .defineInRange("pauseDelay", 1800, 90,  6000);

        PAUSE_MIN = COMMON_BUILDER.comment("The minimum amount of attempted spawns of a type in a chunk before we allow pausing")
                .defineInRange("pauseMin", 256, 5,  2048);

        ALLOW_PAUSE = COMMON_BUILDER.comment("Allow pausing of spawns in specific chunks")
                .define("pauseEnable", false);

        SLOW_TICKS = COMMON_BUILDER.comment("How long to stay in slow mode after spawn rates are under control")
                .defineInRange("slowDelay", 600, 90,  6000);

        ALLOW_SLOW = COMMON_BUILDER.comment("Allow slowing of spawns in specific chunks")
                .define("slowEnable", true);

        SAMPLE_TICKS = COMMON_BUILDER.comment("How many ticks to sample (and average) spawn rates over")
                .defineInRange("sampleTicks", 5, 2,  20);

        MAX_CHUNK_SPAWN_REQ_TICK = COMMON_BUILDER.comment("Maximum spawn requests per chunk per tick based off average spawn rate in sample spawn rate")
                .defineInRange("maxSpawnsPerChunkPerTick", 12, 1,  500);

        SPAWNLOC_CACHE_TICKS = COMMON_BUILDER.comment("How many ticks to remember if we fail spawning in a block position")
                .defineInRange("spawnlocTicks", 600, 1,  2048);

        MANAGER_CACHE_TICKS = COMMON_BUILDER.comment("How many ticks to store a SpawnManager for a chunk after it's last update")
                .defineInRange("spawnManagerTicks", 600, 1,  2048);

        DEBUG_PRINT = COMMON_BUILDER.comment("Spam your console and make performance terrible...")
                .define("debugPrint", false);

        CLEAN_PRINT = COMMON_BUILDER.comment("Disable this once you're happy with the configs you have and their impact on memory. This will print how many SpawnManagers and SpawnCaches we have at once, every 10 seconds.")
                .define("cleanPrint", true);

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path)
    {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {}

    @SubscribeEvent
    public static void onReload(final ModConfig.Reloading configEvent) {}

}
