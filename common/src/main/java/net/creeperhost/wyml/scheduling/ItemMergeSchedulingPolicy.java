package net.creeperhost.wyml.scheduling;

/** Preserves vanilla eligibility while assigning an eligible item a deterministic phase. */
public final class ItemMergeSchedulingPolicy
{
    private ItemMergeSchedulingPolicy()
    {
    }

    public static boolean shouldInitiate(
            boolean vanillaMergeEligible,
            int tick,
            int entityId,
            int interval)
    {
        return vanillaMergeEligible && DeterministicTickScheduler.shouldRun(tick, entityId, interval);
    }
}
