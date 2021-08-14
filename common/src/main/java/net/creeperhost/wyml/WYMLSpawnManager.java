package net.creeperhost.wyml;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class WYMLSpawnManager
{
    EntityClassification classification;
    ChunkPos chunk;
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
    private HashMap<Long, spawnLocation> prevSpawns = new HashMap<Long, spawnLocation>();
    boolean slowMode;
    public WYMLSpawnManager(ChunkPos pos, EntityClassification classification)
    {
        this.classification = classification;
        this.chunk = pos;
    }

    public ChunkPos getChunk() {
        return chunk;
    }
    public EntityClassification getClassification() {
        return classification;
    }
    public boolean isSlowMode() {
        return slowMode;
    }

    public long getFinishRate() {
        return finishRate;
    }

    public long getStartRate() {
        return startRate;
    }

    public boolean isSaved()
    {
        return !requiresSave;
    }

    public double getFailRate()
    {
        if(finishRate == 0) return 100;
        if(startRate == 0) return 0;
        double retVal = ( 100 - ((finishRate / startRate) * 100) );
        if(finishRate > 0)
        {
            double wat1 = (double)((double)finishRate / (double)startRate);
            double wat2 = wat1 * 100d;
            double wat3 = 100d - wat2;
            retVal = Math.round(wat3 * 100d) / 100d;
        }
        if(retVal < 0) return 0;
        if(retVal > 100) return 100;
        return retVal;
    }
    public synchronized void increaseSpawningCount(BlockPos pos)
    {
        startRate++;
        if(WhyYouMakeLag.ticks > (startSpawnSampleTick + WymlConfig.SAMPLE_TICKS.get()))
        {
            startSpawnSampleTick = WhyYouMakeLag.ticks;
            spawnsInTick = 0;
        }
        spawnLocation sl = new spawnLocation();
        if(prevSpawns.containsKey(pos.asLong()))
        {
            sl = prevSpawns.get(pos.asLong());
        }
        if(!sl.success){
            sl.position = pos;
            sl.success = false;
            sl.lastUpdated = WhyYouMakeLag.ticks;
        }
        prevSpawns.put(pos.asLong(), sl);
        spawnsInTick++;
        lastSpawnRequestTick = WhyYouMakeLag.ticks;
        spawningCount++;
        requiresSave = true;
    }
    public void isSaving()
    {
        requiresSave = false;
        lastUpdatedTick = WhyYouMakeLag.ticks;
    }
    public boolean hasExpired()
    {
        return ((lastUpdatedTick + WymlConfig.MANAGER_CACHE_TICKS.get()) > WhyYouMakeLag.ticks && !isPaused() && isSaved());
    }
    public int countBlockCache()
    {
        return prevSpawns.size();
    }
    public synchronized int cleanBlockCache()
    {
        int removedCache = 0;
        try {
            List<Long> toRemove = new ArrayList<Long>();
            Set<Long> ids = prevSpawns.keySet();
            for (long id : ids) {
                spawnLocation sl = prevSpawns.get(id);
                if (sl.lastUpdated > (WhyYouMakeLag.ticks + WymlConfig.SPAWNLOC_CACHE_TICKS.get()) || sl.success) {
                    toRemove.add(id);
                }
            }
            for (long id : toRemove) {
                prevSpawns.remove(id);
                removedCache++;
            }
            if(toRemove.size() > 0)
            {
                requiresSave = true;
            }
        } catch (Exception err) {
            err.printStackTrace();
            //            System.out.println(err.getStackTrace());
        }
        return removedCache;
    }
    public synchronized void decreaseSpawningCount(BlockPos pos)
    {
        if(prevSpawns.containsKey(pos.asLong()))
        {
            //TODO: Just remove from prevSpawns maybe if it's success? Only remember failures as we do nothing special with the success cache
            spawnLocation sl = prevSpawns.get(pos.asLong());
            sl.success = true;
            sl.lastUpdated = WhyYouMakeLag.ticks;
            prevSpawns.put(pos.asLong(), sl);
        }
        finishRate++;
        if(finishRate > startRate) startRate = finishRate;
        spawningCount--;
        requiresSave = true;
    }
    public int getSpawnsInSample()
    {
        if(WhyYouMakeLag.ticks < startSpawnSampleTick+WymlConfig.SAMPLE_TICKS.get())
        {
            int retVal = spawnsInTick;
            int sampleLength = (WhyYouMakeLag.ticks-startSpawnSampleTick);
            if(sampleLength > 0) retVal = spawnsInTick/sampleLength;
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
        slowModeStart = WhyYouMakeLag.ticks;
        requiresSave = true;
    }
    public int ticksSinceSlow()
    {
        int diff = WhyYouMakeLag.ticks - slowModeStart;
        if(diff < 0) diff = 99999;
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
        pauseTick = WhyYouMakeLag.ticks;
        requiresSave = true;
    }
    public boolean isPaused()
    {
        if((isPaused && (pauseTick + pausedFor) > WhyYouMakeLag.ticks)||(isPaused && getFailRate() < (100d - WymlConfig.RESUME_RATE.get())))
        {
            return true;
        } else {
            if(isPaused) {
                if(WymlConfig.DEBUG_PRINT.get()) System.out.println("Resuming spawns for class "+getClassification().getName() + " at " + getChunk() + " due to timeout or failure rate decease ["+getFailRate()+"%].");
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
        if(prevSpawns.containsKey(pos.asLong()))
        {
            spawnLocation sl = prevSpawns.get(pos.asLong());
            if(sl.lastUpdated < (WhyYouMakeLag.ticks + WymlConfig.SPAWNLOC_CACHE_TICKS.get()))
            {
                return !sl.success;
            }
        }
        return false;
    }
    class spawnLocation
    {
        BlockPos position;
        boolean success;
        int lastUpdated;
    }

}
