package net.creeperhost.wyml.scheduling;

/**
 * Assigns entity work to deterministic tick phases without consuming entity RNG.
 */
public final class DeterministicTickScheduler
{
    private DeterministicTickScheduler()
    {
    }

    public static boolean shouldRun(int tick, int entityId, int interval)
    {
        int safeInterval = Math.max(1, interval);
        return Math.floorMod(tick - entityId, safeInterval) == 0;
    }
}
