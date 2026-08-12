package net.creeperhost.wyml.spawn;

public final class PauseEligibility
{
    private PauseEligibility()
    {
    }

    public static int inclusiveMinimum(int configuredInclusiveMinimum, int legacyExclusiveMinimum)
    {
        if (configuredInclusiveMinimum >= 0) return configuredInclusiveMinimum;
        return Math.max(0, legacyExclusiveMinimum + 1);
    }

    public static boolean hasMinimumPlayers(int onlinePlayers, int inclusiveMinimum)
    {
        return onlinePlayers >= Math.max(0, inclusiveMinimum);
    }
}
