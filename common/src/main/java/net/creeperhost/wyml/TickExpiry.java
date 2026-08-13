package net.creeperhost.wyml;

/** Tick-based expiry helpers shared by manager and spawn-location caches. */
public final class TickExpiry
{
    private TickExpiry()
    {
    }

    public static boolean hasElapsed(int savedTick, int currentTick, int ttl)
    {
        if (ttl <= 0) return true;
        long elapsed = Integer.toUnsignedLong(currentTick - savedTick);
        return elapsed >= ttl;
    }

    public static boolean managerHasExpired(boolean saved, boolean paused, int savedTick, int currentTick, int ttl)
    {
        return saved && !paused && hasElapsed(savedTick, currentTick, ttl);
    }
}
