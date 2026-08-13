package net.creeperhost.wyml.spawn;

/**
 * Orders throttled-controller transitions ahead of per-window budget gating.
 * This prevents a zero budget from hiding an elapsed throttle window forever.
 */
public final class SpawnThrottlePolicy
{
    public enum Action
    {
        ALLOW,
        BLOCK,
        ACTIVATE,
        BACKOFF
    }

    private SpawnThrottlePolicy()
    {
    }

    public static Action decide(
            boolean throttleWindowElapsed,
            boolean shouldEnterBackoff,
            int attemptsInWindow,
            int attemptBudget)
    {
        if (throttleWindowElapsed)
        {
            return shouldEnterBackoff ? Action.BACKOFF : Action.ACTIVATE;
        }

        return attemptsInWindow >= Math.max(0, attemptBudget) ? Action.BLOCK : Action.ALLOW;
    }
}
