package net.creeperhost.wyml;

import net.creeperhost.wyml.config.ModSpawnConfig;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.data.MobSpawnData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;

public class ChunkManager
{
    MobCategory classification;
    ChunkPos chunk;
    DimensionType dimensionType;
    Level level;
    int spawningCount;
    private long startRate;
    private long finishRate;
    private int pauseTick;
    private int pausedFor;
    private int startSpawnSampleTick;
    private int lastSpawnRequestTick;
    private int spawnsInTick;
    private int slowModeStart;
    private int lastActivityTick;
    private boolean requiresSave;
    private boolean isPaused;
    private final SpawnLocationCache locationFailures = new SpawnLocationCache();
    boolean slowMode;

    public ChunkManager(ChunkPos pos, DimensionType dimensionType, MobCategory classification)
    {
        //TODO: Start accepting level name too
        this.classification = classification;
        this.chunk = pos;
        this.dimensionType = dimensionType;
    }

    public ChunkPos getChunk()
    {
        return chunk;
    }

    public MobCategory getClassification()
    {
        return classification;
    }

    public boolean isSlowMode()
    {
        return slowMode;
    }

    public long getFinishRate()
    {
        return finishRate;
    }

    public long getStartRate()
    {
        return startRate;
    }

    public boolean isSaved()
    {
        return !requiresSave;
    }

    public boolean isClaimed()
    {
        return WhyYouMakeLag.cachedClaimedChunks.get().contains(getChunk().toLong());
    }

    public boolean isForceLoaded()
    {
        return WhyYouMakeLag.cachedForceLoadedChunks.get().contains(getChunk().toLong());
    }

    public double getFailRate()
    {
        if (finishRate == 0) return 100;
        if (startRate == 0) return 0;
        double retVal = (100 - ((finishRate / startRate) * 100));
        if (finishRate > 0)
        {
            double wat1 = (double) ((double) finishRate / (double) startRate);
            double wat2 = wat1 * 100d;
            double wat3 = 100d - wat2;
            retVal = Math.round(wat3 * 100d) / 100d;
        }
        if (retVal < 0) return 0;
        if (retVal > 100) return 100;
        return retVal;
    }

    public synchronized void increaseSpawningCount(BlockPos pos)
    {
        startRate++;
        if (WhyYouMakeLag.getTicks() > (startSpawnSampleTick + WymlConfig.cached().SAMPLE_TICKS))
        {
            startSpawnSampleTick = WhyYouMakeLag.getTicks();
            spawnsInTick = 0;
        }
        // Every candidate remains part of slow/pause accounting, but reaching
        // this point alone is not evidence that its position is reusable as a
        // failure. Player distance, light/weather, population and loader vetoes
        // are all evaluated later and must remain transient.
        spawnsInTick++;
        lastSpawnRequestTick = WhyYouMakeLag.getTicks();
        spawningCount++;
        touchActivity();
        requiresSave = true;
    }

    public void isSaving()
    {
        requiresSave = false;
    }

    public boolean hasExpired()
    {
        boolean paused = isPaused();
        return TickExpiry.managerHasExpired(isSaved(), paused, lastActivityTick, WhyYouMakeLag.getTicks(), WymlConfig.cached().MANAGER_CACHE_TICKS);
    }

    public int countBlockCache()
    {
        return locationFailures.size();
    }

    public synchronized int cleanBlockCache()
    {
        int removedCache = locationFailures.cleanExpired(WymlConfig.generation(), WhyYouMakeLag.getTicks(), WymlConfig.cached().SPAWNLOC_CACHE_TICKS);
        if (removedCache > 0) requiresSave = true;
        return removedCache;
    }

    public synchronized void decreaseSpawningCount(BlockPos pos, EntityType<?> entityType)
    {
        locationFailures.recordSuccess(pos.asLong(), entityTypeKey(entityType));
        finishRate++;
        if (finishRate > startRate) startRate = finishRate;
        spawningCount--;
        touchActivity();
        requiresSave = true;
    }

    public int getSpawnsInSample()
    {
        if (WhyYouMakeLag.getTicks() < (startSpawnSampleTick + WymlConfig.cached().SAMPLE_TICKS))
        {
            int retVal = spawnsInTick;
            int sampleLength = (WhyYouMakeLag.getTicks() - startSpawnSampleTick);
            if (sampleLength > 0) retVal = spawnsInTick / sampleLength;
            return retVal;
        }
        return 0;
    }

    public void resetSpawningCount()
    {
        spawningCount = 0;
        touchActivity();
        requiresSave = true;
    }

    public void slowMode()
    {
        resetSpawningCount();
        slowMode = true;
        slowModeStart = WhyYouMakeLag.getTicks();
        requiresSave = true;
    }

    public int ticksSinceSlow()
    {
        int diff = WhyYouMakeLag.getTicks() - slowModeStart;
        if (diff < 0) diff = 99999;
        return diff;
    }

    public void fastMode()
    {
        slowModeStart = 0;
        resetSpawningCount();
        slowMode = false;
        requiresSave = true;
    }

    public int getLastSpawnRequestTick()
    {
        return lastSpawnRequestTick;
    }

    public void pauseSpawns(int ticks)
    {
        isPaused = true;
        pausedFor = ticks;
        pauseTick = WhyYouMakeLag.getTicks();
        touchActivity();
        requiresSave = true;
    }
    public boolean reachedMobLimit(ResourceLocation resourceLocation)
    {
        return reachedMobLimit(resourceLocation.getNamespace(), resourceLocation.getPath());
    }
    public Level getLevel()
    {
        return this.level;
    }
    public boolean reachedMobLimit(String modName, String mobName)
    {
        if(!WymlConfig.cached().ENABLE_PER_MOD_CONFIGS||!MobManager.canManage) return false;
        if(this.level == null) {
            for (ResourceKey<Level> levelKey : WhyYouMakeLag.minecraftServer.levelKeys()) {
                Level _level = WhyYouMakeLag.minecraftServer.getLevel(levelKey);
                if (_level == null) continue;
                if (_level.dimensionType() == dimensionType) {
                    this.level = _level;
                    break;
                }
            }
        }
        if(level == null) level = WhyYouMakeLag.minecraftServer.getLevel(Level.OVERWORLD);
        if(level == null||level.isClientSide()) return false;
        ProfilerFiller profilerFiller = level.getProfiler();
        profilerFiller.push("mobLimit");
        ChunkPos pos = getChunk();
        if(pos == null)
        {
            profilerFiller.pop();
            return false;
        }
        LevelChunk chunk = null;
        try {
            ChunkSource source = level.getChunkSource();
            if(!source.hasChunk(pos.x, pos.z))
            {
                profilerFiller.pop();
                return false;
            }
            ChunkAccess chunkAccess = source.getChunk(pos.x, pos.z, ChunkStatus.FULL, false);
            if(chunkAccess == null)
            {
                profilerFiller.pop();
                return false;
            }
            chunk = (LevelChunk) chunkAccess;
        } catch(Exception e)
        {
            e.printStackTrace();
            profilerFiller.pop();
            return false;
        }
        if(chunk == null)
        {
            profilerFiller.pop();
            return false;
        }
        ClassInstanceMultiMap<Entity> test[] = chunk.getEntitySections();
        int count = 0;
        if(test == null || test.length == 0)
        {
            profilerFiller.pop();
            return false;
        }
        for(ClassInstanceMultiMap<Entity> t : test)
        {
            if(t.isEmpty()) continue;
            for (Entity entity : t) {
                ResourceLocation resourceLocation = Registry.ENTITY_TYPE.getKey(entity.getType());
                if(resourceLocation.getNamespace().equals(modName) && resourceLocation.getPath().equals(mobName))
                {
                    count++;
                }
            }
        }
        ModSpawnConfig modSpawnConfig = MobManager.getMod(modName);
        if(modSpawnConfig == null)
        {
            profilerFiller.pop();
            return false;
        }
        MobSpawnData mobSpawnData = modSpawnConfig.getMob(mobName);
        if(mobSpawnData == null)
        {
            profilerFiller.pop();
            return false;
        }
        profilerFiller.pop();
        return (count >= mobSpawnData.limit);
    }
    public boolean canPause()
    {
        boolean isPausable = WymlConfig.cached().ALLOW_PAUSE && (WhyYouMakeLag.minecraftServer.getPlayerList().getPlayerCount() > WymlConfig.cached().MINIMUM_PAUSE_PLAYERS);
        if (isPausable)
        {
            if (WhyYouMakeLag.isFtbChunksLoaded())
            {
                if (!WymlConfig.cached().ALLOW_PAUSE_CLAIMED)
                {
                    if (isClaimed()) return false;
                }
            }
            if (!WymlConfig.cached().ALLOW_PAUSE_FORCED)
            {
                if (isForceLoaded()) return false;
            }
        }
        return isPausable;
    }

    public boolean isPaused()
    {
        int resumeRate = isClaimed() ? WymlConfig.cached().RESUME_CLAIMED_RATE : WymlConfig.cached().RESUME_RATE;
        if ((isPaused && (pauseTick + pausedFor) > WhyYouMakeLag.getTicks()) || (isPaused && getFailRate() < (100d - resumeRate)))
        {
            return true;
        }
        else
        {
            if (isPaused)
            {
                if (WymlConfig.cached().DEBUG_PRINT)
                    System.out.println("Resuming spawns for class " + getClassification().getName() + " at " + getChunk() + " due to timeout or failure rate decease [" + getFailRate() + "%].");
                isPaused = false;
                startRate = 0;
                finishRate = 0;
                resetSpawningCount();
                pauseTick = 0;
                pausedFor = 0;
                touchActivity();
                requiresSave = true;
            }
            return false;
        }
    }

    public synchronized boolean isKnownBadLocation(BlockPos pos, EntityType<?> entityType, Object placementIdentity)
    {
        if (pos == null || entityType == null) return false;
        return locationFailures.isKnownFailure(pos.asLong(), entityTypeKey(entityType), placementIdentity, WymlConfig.generation(), WhyYouMakeLag.getTicks(), WymlConfig.cached().SPAWNLOC_CACHE_TICKS);
    }

    public synchronized boolean recordFailedSpawnLocation(BlockPos pos, EntityType<?> entityType, Object placementIdentity, SpawnFailureReason reason)
    {
        if (pos == null || entityType == null) return false;
        boolean recorded = locationFailures.recordFailure(pos.asLong(), entityTypeKey(entityType), placementIdentity, reason, WymlConfig.generation(), WhyYouMakeLag.getTicks(), WymlConfig.cached().SPAWNLOC_CACHE_TICKS);
        if (recorded) requiresSave = true;
        return recorded;
    }

    private static String entityTypeKey(EntityType<?> entityType)
    {
        ResourceLocation id = Registry.ENTITY_TYPE.getKey(entityType);
        return id == null ? null : id.toString();
    }

    private void touchActivity()
    {
        lastActivityTick = WhyYouMakeLag.getTicks();
    }

}
