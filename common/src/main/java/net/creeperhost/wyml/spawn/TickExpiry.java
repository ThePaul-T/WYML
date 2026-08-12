package net.creeperhost.wyml.spawn;

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
}
