package net.creeperhost.wyml.config;

import blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;

public class ConfigData
{
    @Comment("Authoritative WYML runtime switch. Set false together with master_enabled=false in wyml-mixins.properties, then restart, for the vanilla-behaviour baseline.")
    public boolean ENABLE_WYML = true;

    // ******************************************
    // * Misc
    // ******************************************
    @Comment("Legacy slow-mode attempt allowance. Retained for migration; use ATTEMPT_BUDGET_PER_WINDOW.")
    public int MOB_TRIES = 1;

    @Comment("Legacy slow-mode player scaling. Retained when ATTEMPT_BUDGET_PLAYER_SCALING is legacy.")
    public boolean MULTIPLY_BY_PLAYERS = true;

    @Comment("Natural-spawn candidate budget per SAMPLE_TICKS window while throttled. -1 migrates MOB_TRIES.")
    public int ATTEMPT_BUDGET_PER_WINDOW = -1;

    @Comment("Throttled budget player scaling: legacy, none, or linear.")
    public String ATTEMPT_BUDGET_PLAYER_SCALING = "legacy";

    @Comment("Enable WYML category cap scaling and despawn-distance overrides independently of spawn throttling. Restart required.")
    public boolean ENABLE_CATEGORY_CAP_POLICY = true;

    @Comment("Replace Mojang magic number (pow2) with this")
    public double MOJANG_MAGIC_NUM = 17D;

    @Comment("Spam your console and make performance terrible...")
    public boolean DEBUG_PRINT = false;

    @Comment("Show numeric latency beside each player in the client player list")
    public boolean SHOW_NUMERIC_PING = true;

    @Comment("Downscale the Mojang Magic Number by the online player count")
    public boolean DOWNSCALE_MAGIC_NUM = true;

    @Comment("If downscaling enabled, do not allow below this value")
    public double DOWNSCALE_MAGIC_NUM_MIN = 8D;

//    @Comment("Amount of nano seconds to wait for a task (Mojang default is 100000), thread locking uses the futex[https://man7.org/linux/man-pages/man2/futex.2.html] syscall on linux, causing a lot of syscalls when the value is too low")
//    public long TASK_WAIT_NANOS = 5000000L;
//
//    @Comment("Enable Minecraft dataFixerUpper (enables you to upgrade worlds between Minecraft versions), disabling this stops the big ram spike at server start when loading existing worlds [ENABLE THIS IF YOU HAVE ANOTHER DFU CHANGING MOD!]")
//    public boolean ENABLE_DFU = true;

    @Comment("Advanced, default-off: request one full JVM garbage collection after initial level loading. The JVM may ignore the request or pause the server; review the recorded duration before retaining it.")
    public boolean ENABLE_GARBAGE_COLLECTION_LOAD = false;

    @Comment("Deprecated no-op retained for configuration migration. Modern Minecraft already waits against its own tick deadline.")
    public boolean NORMALIZE_TICKS = true;

    @Comment("Set the amount of time it takes for an item to de-spawn in ticks, This can only be reduced (default 6000)")
    public int ITEM_DESPAWN_TIME = 6000;

    @Comment("List of items which are not to have their de-spawn time changed\r\nExample: minecraft:kelp")
    public List<String> ITEM_DESPAWN_DENYLIST = new ArrayList<String>();

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

    @Comment("Number of natural-spawn candidates allowed in the bounded probe window after backoff")
    public int PROBE_ATTEMPTS = 8;

    @Comment("At what what percentage of failed spawns should we then pause spawning in a chunk in claimed chunks")
    public int PAUSE_CLAIMED_RATE = 65;

    @Comment("How long to pause spawning if pause spawn failure rate reached in claimed chunks")
    public int PAUSE_CLAIMED_TICKS = 1800;

    @Comment("At what what percentage of successful spawns, after we pause spawning, should we resume spawning in claimed chunks")
    public int RESUME_CLAIMED_RATE = 10;

    @Comment("The minimum amount of attempted spawns of a type in a chunk before we allow pausing")
    public int PAUSE_MIN = 256;

    @Comment("Legacy exclusive player threshold. Retained for migration; PAUSE_MIN_PLAYERS=-1 translates this value by adding one.")
    public int MINIMUM_PAUSE_PLAYERS = 2;

    @Comment("Inclusive online-player minimum for entering backoff. -1 migrates the legacy exclusive threshold.")
    public int PAUSE_MIN_PLAYERS = -1;


    // ******************************************
    // * Slowing
    // ******************************************
    @Comment("Allow slowing of spawns in specific chunks")
    public boolean ALLOW_SLOW = true;

    @Comment("Allow slow-mode throttling in FTB Chunks claimed chunks; true preserves historical behavior")
    public boolean ALLOW_SLOW_CLAIMED = true;

    @Comment("Allow slow-mode throttling in force-loaded chunks; true preserves historical behavior")
    public boolean ALLOW_SLOW_FORCED = true;

    @Comment("Maximum spawn requests per chunk per tick based off average spawn rate in sample spawn rate")
    public int MAX_CHUNK_SPAWN_REQ_TICK = 12;

    @Comment("How long to stay in slow mode after spawn rates are under control")
    public int SLOW_TICKS = 600;

    @Comment("Set to true to spread entity pushing updates between ticks. (Reduces network and CPU usage)")
    public boolean NORMALIZE_PUSHING = true;

    @Comment("Ticks between scheduled collision-neighbour queries for ordinary living entities. Players, riders, and vehicles are exempt.")
    public int ENTITY_PUSH_INTERVAL = 4;

    @Comment("Set to true to deterministically spread dropped-item merge queries without consuming entity RNG.")
    public boolean NORMALIZE_ITEM_STACK_MERGING = true;

    @Comment("Merge-query interval for an item that crossed a block-coordinate boundary this tick. Vanilla uses 2 ticks.")
    public int ITEM_MERGE_MOVING_INTERVAL = 2;

    @Comment("Merge-query interval for an item that remained in the same block this tick. Vanilla uses 40 ticks.")
    public int ITEM_MERGE_STATIONARY_INTERVAL = 40;


    // ******************************************
    // * Sampling
    // ******************************************
    @Comment("How many ticks to sample (and average) spawn rates over")
    public int SAMPLE_TICKS = 5;


    // ******************************************
    // * Caching
    // ******************************************
    @Comment("Deprecated no-op. Location caching is retired because current spawn failures combine transient, random, collision, and loader-dependent checks; bounded backoff replaces it safely.")
    public int SPAWNLOC_CACHE_TICKS = 600;

    @Comment("How many ticks to store a SpawnManager for a chunk after it's last update")
    public int MANAGER_CACHE_TICKS = 600;

    @Comment("Disable this once you are happy with the configs you have and their impact on memory. This will print how many SpawnManagers and SpawnCaches we have at once, every 10 seconds.")
    public boolean CLEAN_PRINT = true;

    // ******************************************
    // * Paper Bags
    // ******************************************
    @Comment("Allow Paper bags to pickup item spills")
    public boolean ALLOW_PAPER_BAGS = false;

    @Comment("The minimum age an item will need to be in a spill before a Paper bag will be spawned (default 60)")
    public int MIN_ITEM_AGE = 60;

    @Comment("The minimum amount of items in a spill needed for a Paper bag to spawn (default 20)")
    public int MIN_ITEM_COUNT = 20;

    @Comment("The amount of time in seconds before a Paper bag will de-spawn (default 300)")
    public int PAPER_BAG_DESPAWN_TIME = 300;

    @Comment("Maximum queued spill locations processed on the server thread per tick")
    public int PAPER_BAG_CANDIDATES_PER_TICK = 4;

    @Comment("Maximum item entities transferred into Paper Bags per processed spill and tick")
    public int PAPER_BAG_COLLECTION_BUDGET = 128;

    @Comment("Bounded block/entity search radius for one spill candidate (1-16)")
    public int PAPER_BAG_SCAN_RADIUS = 4;

    @Comment("Expiry behavior: legacy_void_with_warning or persist_while_non_empty")
    public String PAPER_BAG_EXPIRY_POLICY = "legacy_void_with_warning";

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

    @Comment("Allow per mob, per category, per chunk, per mob limitations from the SpawnLimits directory.")
    public boolean ENABLE_PER_MOD_CONFIGS = true;

    @Comment("Make per mob, per category per chunk limitations hard limits or soft (Forcefully stop spawns even when not natural).")
    public boolean HARD_MOB_LIMITS = false;

    @Comment("Legacy escape hatch: use Minecraft's chunk-generation creature path without WYML soft per-mob checks or spawn-controller handling. Hard limits still apply after admission.")
    public boolean DISABLE_COUNTING_CHUNK_GENERATED_MOBS = false;

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

    // ******************************************
    // * Data Fixer Upper
    // ******************************************
//    @Comment("Enable DFU patch")
//    public boolean ENABLE_DATA_FIXER_UPPER_NBT_PATCH = true;
}
