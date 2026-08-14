package net.creeperhost.wyml.paperbag;

/** Pure Paper Bag thresholds and bounds, shared by runtime code and tests. */
public final class PaperBagSpillPolicy
{
    private static final int MAX_SCAN_RADIUS = 16;

    private PaperBagSpillPolicy()
    {
    }

    public static boolean qualifies(int eligibleEntities, int oldestAge, int minimumEntities, int minimumAge)
    {
        return eligibleEntities >= Math.max(1, minimumEntities)
                && oldestAge >= Math.max(0, minimumAge);
    }

    /**
     * Keep a qualifying-size spill in the bounded queue while its oldest item
     * matures. Spills below the count threshold wait for another add event.
     */
    public static boolean shouldRetryAwaitingAge(
            int eligibleEntities,
            int oldestAge,
            int minimumEntities,
            int minimumAge)
    {
        return eligibleEntities >= Math.max(1, minimumEntities)
                && oldestAge < Math.max(0, minimumAge);
    }

    public static int positiveBudget(int configured)
    {
        return Math.max(1, configured);
    }

    public static int radius(int configured)
    {
        return Math.clamp(configured, 1, MAX_SCAN_RADIUS);
    }
}
