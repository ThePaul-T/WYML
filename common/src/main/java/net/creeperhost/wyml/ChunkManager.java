package net.creeperhost.wyml;

import net.creeperhost.wyml.compat.CompatFTBChunks;
import net.creeperhost.wyml.config.ModSpawnConfig;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.data.MobSpawnData;
import net.creeperhost.wyml.spawn.ChunkBounds;
import net.creeperhost.wyml.spawn.ControllerKey;
import net.creeperhost.wyml.spawn.ControllerState;
import net.creeperhost.wyml.spawn.PauseEligibility;
import net.creeperhost.wyml.spawn.SpawnAttemptSnapshot;
import net.creeperhost.wyml.spawn.SpawnAttemptTracker;
import net.creeperhost.wyml.spawn.SpawnControllerState;
import net.creeperhost.wyml.spawn.TickExpiry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ChunkManager
{
    private final ControllerKey key;
    private final MobCategory classification;
    private final ChunkPos chunk;
    private final ServerLevel level;
    int spawningCount;
    private long startRate;
    private long finishRate;
    private int startSpawnSampleTick;
    private int lastSpawnRequestTick;
    private int spawnsInTick;
    private int lastUpdatedTick;
    private final SpawnAttemptTracker attemptTracker = new SpawnAttemptTracker();
    private final SpawnControllerState controllerState = new SpawnControllerState();
    private boolean requiresSave;

    public ChunkManager(ServerLevel level, ChunkPos pos, MobCategory classification)
    {
        this.level = level;
        this.classification = classification;
        this.chunk = pos;
        this.key = ControllerKey.of(level, pos, classification);
    }

    public ControllerKey getKey()
    {
        return key;
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
        return controllerState.current(getTicks()) == ControllerState.THROTTLED;
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
        return WhyYouMakeLag.isFtbChunksLoaded() && CompatFTBChunks.isClaimed(level, getChunk());
    }

    public boolean isForceLoaded()
    {
        return level.getChunkSource().getForceLoadedChunks().contains(getChunk().pack());
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

    public synchronized void increaseSpawningCount()
    {
        startRate++;
        int currentTick = getTicks();
        int windowTicks = Math.max(1, WymlConfig.cached().SAMPLE_TICKS);
        if (TickExpiry.hasElapsed(startSpawnSampleTick, currentTick, windowTicks))
        {
            startSpawnSampleTick = currentTick;
            spawnsInTick = 0;
        }
        spawnsInTick++;
        lastSpawnRequestTick = currentTick;
        spawningCount++;
        requiresSave = true;
    }

    public SpawnAttemptTracker.Attempt beginNaturalSpawnAttempt()
    {
        int currentTick = getTicks();
        if (!controllerState.tryAcquireAttempt(currentTick)) return null;
        increaseSpawningCount();
        return attemptTracker.begin(success ->
        {
            int completionTick = getTicks();
            ControllerState before = controllerState.current(completionTick);
            controllerState.recordOutcome(success, completionTick);
            if (before == ControllerState.PROBE && controllerState.current(completionTick) != ControllerState.PROBE)
            {
                resetObservationWindow();
            }
        });
    }

    public SpawnAttemptSnapshot getAttemptSnapshot()
    {
        return attemptTracker.snapshot();
    }

    public void isSaving()
    {
        requiresSave = false;
        lastUpdatedTick = getTicks();
    }

    public boolean hasExpired()
    {
        return isSaved()
                && getControllerState() == ControllerState.ACTIVE
                && TickExpiry.hasElapsed(lastUpdatedTick, getTicks(), WymlConfig.cached().MANAGER_CACHE_TICKS);
    }

    public int countBlockCache()
    {
        return 0;
    }

    public synchronized int cleanBlockCache()
    {
        // The legacy cache was keyed only by block position inside a category.
        // It is intentionally quarantined until failures have a stable rule/type
        // identity and an explicit cache-eligibility contract.
        return 0;
    }

    public synchronized void decreaseSpawningCount()
    {
        finishRate++;
        if (finishRate > startRate) startRate = finishRate;
        spawningCount--;
        requiresSave = true;
    }

    public int getSpawnsInSample()
    {
        int currentTick = getTicks();
        int windowTicks = Math.max(1, WymlConfig.cached().SAMPLE_TICKS);
        if (!TickExpiry.hasElapsed(startSpawnSampleTick, currentTick, windowTicks))
        {
            int retVal = spawnsInTick;
            int sampleLength = (int) Integer.toUnsignedLong(currentTick - startSpawnSampleTick);
            if (sampleLength > 0) retVal = spawnsInTick / sampleLength;
            return retVal;
        }
        return 0;
    }

    public int getAttemptsInCurrentWindow()
    {
        if (TickExpiry.hasElapsed(startSpawnSampleTick, getTicks(), Math.max(1, WymlConfig.cached().SAMPLE_TICKS)))
        {
            return 0;
        }
        return spawnsInTick;
    }

    public void resetSpawningCount()
    {
        spawningCount = 0;
        requiresSave = true;
    }

    public void slowMode()
    {
        resetSpawningCount();
        controllerState.throttle(getTicks());
        requiresSave = true;
    }

    public int ticksSinceSlow()
    {
        return controllerState.ticksInState(getTicks());
    }

    public void fastMode()
    {
        resetSpawningCount();
        controllerState.activate(getTicks());
        requiresSave = true;
    }

    public int getLastSpawnRequestTick()
    {
        return lastSpawnRequestTick;
    }

    public void pauseSpawns(int ticks)
    {
        int resumeRate = isClaimed() ? WymlConfig.cached().RESUME_CLAIMED_RATE : WymlConfig.cached().RESUME_RATE;
        controllerState.backoff(getTicks(), ticks, WymlConfig.cached().PROBE_ATTEMPTS, resumeRate);
        requiresSave = true;
    }

    public boolean reachedMobLimit(Identifier resourceLocation)
    {
        return reachedMobLimit(resourceLocation.getNamespace(), resourceLocation.getPath());
    }

    public ServerLevel getLevel()
    {
        return this.level;
    }

    public boolean reachedMobLimit(String modName, String mobName)
    {
        if(!WymlConfig.cached().ENABLE_PER_MOD_CONFIGS||!MobManager.canManage) return false;
        if(level.isClientSide()) return false;
        ProfilerFiller profilerFiller = Profiler.get();
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
            AABB aabb = ChunkBounds.fullHeight(pos, level);
            Identifier resourceLocation = Identifier.fromNamespaceAndPath(modName, mobName);
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(resourceLocation);
            if(type == null)
            {
                profilerFiller.pop();
                return false;
            }
            List<Entity> list = level.getEntities((Entity) null, aabb, entity -> entity.getType() == type);
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
        int minimumPlayers = PauseEligibility.inclusiveMinimum(
                WymlConfig.cached().PAUSE_MIN_PLAYERS, WymlConfig.cached().MINIMUM_PAUSE_PLAYERS);
        boolean isPausable = WymlConfig.cached().ALLOW_PAUSE
                && PauseEligibility.hasMinimumPlayers(level.getServer().getPlayerList().getPlayerCount(), minimumPlayers);
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
        ControllerState current = controllerState.current(getTicks());
        if (!WymlConfig.cached().ALLOW_PAUSE
                && (current == ControllerState.BACKOFF || current == ControllerState.PROBE))
        {
            controllerState.activate(getTicks());
            resetObservationWindow();
            return false;
        }
        return controllerState.blocksCategory(getTicks());
    }

    private void resetObservationWindow()
    {
        startRate = 0;
        finishRate = 0;
        resetSpawningCount();
    }

    public boolean canEnterBackoff()
    {
        return controllerState.canEnterBackoff(getTicks());
    }

    public ControllerState getControllerState()
    {
        return controllerState.current(getTicks());
    }

    private int getTicks()
    {
        return WhyYouMakeLag.getTicks(level.getServer());
    }

}
