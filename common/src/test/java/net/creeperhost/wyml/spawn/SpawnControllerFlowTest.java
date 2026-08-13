package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpawnControllerFlowTest
{
    @Test
    void activeControllerCanThrottleBackoffAndEnterProbe()
    {
        SpawnControllerState controller = new SpawnControllerState();
        assertEquals(ControllerState.ACTIVE, controller.current(0));

        controller.throttle(10);
        assertEquals(ControllerState.THROTTLED, controller.current(10));

        SpawnThrottlePolicy.Action action = SpawnThrottlePolicy.decide(true, true, 256, 1);
        assertEquals(SpawnThrottlePolicy.Action.BACKOFF, action);
        apply(action, controller, 20);
        assertEquals(ControllerState.BACKOFF, controller.current(24));
        assertEquals(ControllerState.PROBE, controller.current(25));
    }

    @Test
    void zeroBudgetBlocksOnlyUntilTheThrottleWindowCanTransition()
    {
        SpawnControllerState controller = new SpawnControllerState();
        controller.throttle(10);

        assertEquals(SpawnThrottlePolicy.Action.BLOCK,
                SpawnThrottlePolicy.decide(false, false, 0, 0));

        SpawnThrottlePolicy.Action elapsed = SpawnThrottlePolicy.decide(true, false, 0, 0);
        assertEquals(SpawnThrottlePolicy.Action.ACTIVATE, elapsed);
        apply(elapsed, controller, 20);
        assertEquals(ControllerState.ACTIVE, controller.current(20));
    }

    @Test
    void oneAttemptBudgetAllowsOneThenBlocksWithoutHidingExpiry()
    {
        SpawnControllerState controller = new SpawnControllerState();
        controller.throttle(10);

        assertEquals(SpawnThrottlePolicy.Action.ALLOW,
                SpawnThrottlePolicy.decide(false, false, 0, 1));
        assertEquals(SpawnThrottlePolicy.Action.BLOCK,
                SpawnThrottlePolicy.decide(false, false, 1, 1));

        SpawnThrottlePolicy.Action elapsed = SpawnThrottlePolicy.decide(true, false, 1, 1);
        assertEquals(SpawnThrottlePolicy.Action.ACTIVATE, elapsed);
        apply(elapsed, controller, 20);
        assertEquals(ControllerState.ACTIVE, controller.current(20));
    }

    private static void apply(SpawnThrottlePolicy.Action action, SpawnControllerState controller, int tick)
    {
        if (action == SpawnThrottlePolicy.Action.ACTIVATE)
        {
            controller.activate(tick);
        }
        else if (action == SpawnThrottlePolicy.Action.BACKOFF)
        {
            controller.backoff(tick, 5, 1, 100);
        }
    }
}
