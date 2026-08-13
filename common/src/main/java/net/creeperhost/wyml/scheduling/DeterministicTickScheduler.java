package net.creeperhost.wyml.scheduling;

/**
 * Assigns stable per-entity phases without consuming the entity's gameplay RNG.
 */
public final class DeterministicTickScheduler
{
    private DeterministicTickScheduler()
    {
    }

    public static boolean shouldRun(int tick, int entityId, int interval)
    {
        int safeInterval = Math.max(1, interval);
        return Math.floorMod(tick, safeInterval) == Math.floorMod(entityId, safeInterval);
    }
}
