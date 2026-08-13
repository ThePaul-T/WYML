package net.creeperhost.wyml;

import net.creeperhost.wyml.config.ModSpawnConfig;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.data.MobSpawnData;
import net.creeperhost.wyml.spawn.ChunkHorizontalBounds;
import net.creeperhost.wyml.spawn.TickExpiry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;

import java.util.List;

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
    private int lastUpdatedTick;
    private boolean requiresSave;
    private boolean isPaused;
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
        spawnsInTick++;
        lastSpawnRequestTick = WhyYouMakeLag.getTicks();
        spawningCount++;
        requiresSave = true;
    }

    public void isSaving()
    {
        requiresSave = false;
        lastUpdatedTick = WhyYouMakeLag.getTicks();
    }

    public boolean hasExpired()
    {
        return !isPaused()
                && isSaved()
                && TickExpiry.hasElapsed(lastUpdatedTick, WhyYouMakeLag.getTicks(),
                WymlConfig.cached().MANAGER_CACHE_TICKS);
    }

    public int countBlockCache()
    {
        return 0;
    }

    public synchronized int cleanBlockCache()
    {
        // The legacy cache recorded positions before the entity type and failure
        // reason were known. Reusing those entries could suppress unrelated mobs
        // after transient light, player-distance, population, or loader vetoes.
        // Keep statistical backoff, but do not reuse unsafe location failures.
        return 0;
    }

    public synchronized void decreaseSpawningCount(BlockPos pos)
    {
        finishRate++;
        if (finishRate > startRate) startRate = finishRate;
        spawningCount--;
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
        int count = 0;
        try
        {
            ChunkHorizontalBounds bounds = ChunkHorizontalBounds.fromChunkCoordinates(pos.x, pos.z);
            int maxY = level.getMaxBuildHeight();
            int minY = level.getMinBuildHeight();

            AABB aabb = new AABB(bounds.minX(), minY, bounds.minZ(),
                    bounds.maxXExclusive(), maxY, bounds.maxZExclusive());
            ResourceLocation resourceLocation = new ResourceLocation(modName, mobName);
            EntityType<?> type = Registry.ENTITY_TYPE.get(resourceLocation);
            if(type == null)
            {
                profilerFiller.pop();
                return false;
            }
            List<Entity> list = level.getEntities((Entity) null, aabb,
                    entity -> entity.getType() == type && entity.chunkPosition().equals(pos));
            count = list.size();

        } catch(Exception e)
        {
            e.printStackTrace();
            profilerFiller.pop();
            return false;
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
//        System.out.println(mobName + " " + count + " / " + mobSpawnData.limit);
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
        if (isPaused && !TickExpiry.hasElapsed(pauseTick, WhyYouMakeLag.getTicks(), pausedFor))
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
                requiresSave = true;
            }
            return false;
        }
    }

    public synchronized boolean isKnownBadLocation(BlockPos pos)
    {
        // Disabled until entries can be keyed by entity/rule identity and only
        // safe, stable failure reasons are recorded.
        return false;
    }
}
