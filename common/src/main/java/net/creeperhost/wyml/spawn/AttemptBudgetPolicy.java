package net.creeperhost.wyml.spawn;

import java.util.Locale;

public final class AttemptBudgetPolicy
{
    public static final String LEGACY = "legacy";
    public static final String NONE = "none";
    public static final String LINEAR = "linear";

    private AttemptBudgetPolicy()
    {
    }

    public static int resolve(
            int configuredBudget,
            int legacyBudget,
            String configuredScaling,
            boolean legacyMultiplyByPlayers,
            int onlinePlayers)
    {
        int base = configuredBudget >= 0 ? configuredBudget : Math.max(0, legacyBudget);
        String scaling = configuredScaling == null ? LEGACY : configuredScaling.trim().toLowerCase(Locale.ROOT);
        boolean linear = switch (scaling)
        {
            case NONE -> false;
            case LINEAR -> true;
            default -> legacyMultiplyByPlayers;
        };
        if (!linear) return base;

        long scaled = (long) base * Math.max(1, onlinePlayers);
        return (int) Math.min(Integer.MAX_VALUE, scaled);
    }
}
