package net.creeperhost.wyml.config;

import blue.endless.jankson.Comment;

public class ConfigData
{
    // ******************************************
    // * Misc
    // ******************************************
    @Comment("Set the max amount of spawn tries base rate")
    public int MOB_TRIES = 1;

    @Comment("Set to true to multiply base rate by online player count")
    public boolean MULTIPLY_BY_PLAYERS = true;

    @Comment("Replace Mojang magic number (pow2) with this")
    public double MOJANG_MAGIC_NUM = 17D;

    @Comment("Spam your console and make performance terrible...")
    public boolean DEBUG_PRINT = false;

    @Comment("Downscale the Mojang Magic Number by the online player count")
    public boolean DOWNSCALE_MAGIC_NUM = true;

    @Comment("If downscaling enabled, do not allow below this value")
    public double DOWNSCALE_MAGIC_NUM_MIN = 8D;

    @Comment("Amount of nano seconds to wait for a task (Mojang default is 100000), thread locking uses the futex[https://man7.org/linux/man-pages/man2/futex.2.html] syscall on linux, causing a lot of syscalls when the value is too low")
    public long TASK_WAIT_NANOS = 5000000L;

    @Comment("Enable Minecraft dataFixerUpper (enables you to upgrade worlds between Minecraft versions), disabling this stops the big ram spike at server start when loading existing worlds [ENABLE THIS IF YOU HAVE ANOTHER DFU CHANGING MOD!]")
    public boolean ENABLE_DFU = true;

    @Comment("Force Java garbage collector to run once the levels have been generated or loaded the first time (Frees up memory after the server is initially started)")
    public boolean ENABLE_GARBAGE_COLLECTION_LOAD = true;

    @Comment("Ensure the tick loop does not run repeatedly, waits until the next tick is due (reduce cpu usage on hardware)")
    public boolean NORMALIZE_TICKS = true;

    @Comment("Set the amount of time it takes for an item to despawn in ticks, This can only be reduced (default 6000)")
    public int ITEM_DESPAWN_TIME = 6000;


    // ******************************************
    // * Pausing
    // ******************************************
    @Comment("Allow pausing of spawns in specific chunks")
    public boolean ALLOW_PAUSE = true;

    @Comment("Allow pausing of chunks claimed using FTB Chunks")
    public boolean ALLOW_PAUSE_CLAIMED = false;

    @Comment("Allow pausing of force chunk loaded chunks")
    public boolean ALLOW_PAUSE_FORCED = false;

    @Comment("At what what percentage of failed spawns should we then pause spawning in a chunk")
    public int PAUSE_RATE = 65;

    @Comment("How long to pause spawning if pause spawn failure rate reached")
    public int PAUSE_TICKS = 1800;

    @Comment("At what what percentage of successful spawns, after we pause spawning, should we resume spawning")
    public int RESUME_RATE = 10;

    @Comment("At what what percentage of failed spawns should we then pause spawning in a chunk in claimed chunks")
    public int PAUSE_CLAIMED_RATE = 65;

    @Comment("How long to pause spawning if pause spawn failure rate reached in claimed chunks")
    public int PAUSE_CLAIMED_TICKS = 1800;

    @Comment("At what what percentage of successful spawns, after we pause spawning, should we resume spawning in claimed chunks")
    public int RESUME_CLAIMED_RATE = 10;

    @Comment("The minimum amount of attempted spawns of a type in a chunk before we allow pausing")
    public int PAUSE_MIN = 256;

    @Comment("The minimum amount connected players to enable pausing")
    public int MINIMUM_PAUSE_PLAYERS = 2;


    // ******************************************
    // * Slowing
    // ******************************************
    @Comment("Allow slowing of spawns in specific chunks")
    public boolean ALLOW_SLOW = true;

    @Comment("Maximum spawn requests per chunk per tick based off average spawn rate in sample spawn rate")
    public int MAX_CHUNK_SPAWN_REQ_TICK = 12;

    @Comment("How long to stay in slow mode after spawn rates are under control")
    public int SLOW_TICKS = 600;


    // ******************************************
    // * Sampling
    // ******************************************
    @Comment("How many ticks to sample (and average) spawn rates over")
    public int SAMPLE_TICKS = 5;


    // ******************************************
    // * Caching
    // ******************************************
    @Comment("How many ticks to remember if we fail spawning in a block position")
    public int SPAWNLOC_CACHE_TICKS = 600;

    @Comment("How many ticks to store a SpawnManager for a chunk after it's last update")
    public int MANAGER_CACHE_TICKS = 600;

    @Comment("Disable this once you are happy with the configs you have and their impact on memory. This will print how many SpawnManagers and SpawnCaches we have at once, every 10 seconds.")
    public boolean CLEAN_PRINT = true;

    // ******************************************
    // * Paper Bags
    // ******************************************
    @Comment("Allow Paperbags to pickup item spills")
    public boolean ALLOW_PAPER_BAGS = false;

    @Comment("The minimum age an item will need to be in a spill before a Paperbag will be spawned (default 60)")
    public int MIN_ITEM_AGE = 60;

    @Comment("The minimum amount of items in a spill needed for a Paperbag to spawn (default 20)")
    public int MIN_ITEM_COUNT = 20;

    @Comment("The amount of time in seconds before a Paperbag will despawn (default 300)")
    public int PAPER_BAG_DESPAWN_TIME = 300;

    // ******************************************
    // * Spawn Limit
    // ******************************************
    @Comment("The amount of monster creatures that can spawn in a chunk")
    public int MONSTER_PER_CHUNK = 70;

    @Comment("The amount of creatures that can spawn in a chunk")
    public int CREATURES_PER_CHUNK = 10;

    @Comment("The amount of ambient creatures that can spawn in a chunk")
    public int AMBIENT_CREATURES_PER_CHUNK = 15;

    @Comment("The amount of water creatures that can spawn in a chunk")
    public int WATER_CREATURES_PER_CHUNK = 5;

    @Comment("The amount of water_ambient creatures that can spawn in a chunk")
    public int WATER_AMBIENT_PER_CHUNK = 20;

    @Comment("The amount of misc creatures that can spawn in a chunk")
    public int MISC_CREATURES_PER_CHUNK = -1;

    // ******************************************
    // * Mob Despawn Distance
    // ******************************************
    @Comment("The distance an monster will need to be from the player to despawn")
    public int MONSTER_DESPAWN_DISTANCE = 128;

    @Comment("TThe distance an creature will need to be from the player to despawn")
    public int CREATURES_DESPAWN_DISTANCE = 128;

    @Comment("The distance an ambient creature will need to be from the player to despawn")
    public int AMBIENT_CREATURES_DESPAWN_DISTANCE = 128;

    @Comment("The distance an water creature will need to be from the player to despawn")
    public int WATER_CREATURES_DESPAWN_DISTANCE = 128;

    @Comment("The distance an water ambient creature will need to be from the player to despawn")
    public int WATER_AMBIENT_DESPAWN_DISTANCE = 64;

    @Comment("The distance an misc creature will need to be from the player to despawn")
    public int MISC_CREATURES_DESPAWN_DISTANCE = 128;
}
