package net.creeperhost.wyml.spawn;

/** Tick-duration comparisons that remain correct when the server tick counter wraps. */
public final class TickExpiry
{
    private TickExpiry()
    {
    }

    public static boolean hasElapsed(int startedAt, int currentTick, int duration)
    {
        return duration <= 0 || currentTick - startedAt >= duration;
    }
}
