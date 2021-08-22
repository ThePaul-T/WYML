package net.creeperhost.wyml;

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

    //@Comment("Enable Minecraft dataFixerUpper (enables you to upgrade worlds between Minecraft versions), stops big ram spike at server start when loading existing worlds [ENABLE THIS IF YOU HAVE ANOTHER DFU CHANGING MOD!]")
    //public boolean ENABLE_DFU = true;

    @Comment("Force Java garbage collector to run once the levels have been generated (Frees up memory after the server is initially started)")
    public boolean ENABLE_GARBAGE_COLLECTION_LOAD = true;

    @Comment("Ensure the tick loop does not run repeatedly, waits until the next tick is due (reduce cpu usage on hardware)")
    public boolean NORMALIZE_TICKS = true;


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

}
