package net.creeperhost.wyml.spawn;

/** Shared pause/slow claim and force-load exemption policy. */
public final class ChunkInterventionPolicy
{
    private ChunkInterventionPolicy()
    {
    }

    public static boolean allows(
            boolean enabled,
            boolean claimed,
            boolean forceLoaded,
            boolean allowClaimed,
            boolean allowForced)
    {
        return enabled && (!claimed || allowClaimed) && (!forceLoaded || allowForced);
    }
}
