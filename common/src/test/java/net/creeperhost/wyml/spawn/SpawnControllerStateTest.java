package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnControllerStateTest
{
    @Test
    void timeoutEntersABoundedProbeAndSuccessRecovers()
    {
        SpawnControllerState controller = throttledAt(100);
        controller.backoff(110, 20, 4, 25);

        assertTrue(controller.blocksCategory(129));
        assertEquals(ControllerState.PROBE, controller.current(130));

        completeProbe(controller, 130, true, false, false, false);
        assertEquals(ControllerState.ACTIVE, controller.current(130));
    }

    @Test
    void failedProbeReturnsToBackoffThenProbesAgain()
    {
        SpawnControllerState controller = throttledAt(100);
        controller.backoff(110, 20, 2, 50);

        assertEquals(ControllerState.PROBE, controller.current(130));
        completeProbe(controller, 130, false, false);
        assertEquals(ControllerState.BACKOFF, controller.current(130));
        assertTrue(controller.blocksCategory(149));
        assertEquals(ControllerState.PROBE, controller.current(150));
    }

    @Test
    void backoffDoesNotPermitAttemptsAndProbeIsBounded()
    {
        SpawnControllerState controller = throttledAt(0);
        controller.backoff(10, 10, 1, 100);

        assertFalse(controller.tryAcquireAttempt(19));
        assertTrue(controller.tryAcquireAttempt(20));
        assertFalse(controller.tryAcquireAttempt(20));
        controller.recordOutcome(true, 20);
        assertEquals(ControllerState.ACTIVE, controller.current(20));
    }

    @Test
    void probePercentageComparisonDoesNotRoundUp()
    {
        SpawnControllerState controller = throttledAt(0);
        controller.backoff(10, 10, 8, 13);

        assertEquals(ControllerState.PROBE, controller.current(20));
        completeProbe(controller, 20, true, false, false, false, false, false, false, false);
        assertEquals(ControllerState.BACKOFF, controller.current(20));
    }

    @Test
    void transitionsHandleTickWraparound()
    {
        SpawnControllerState controller = throttledAt(Integer.MAX_VALUE - 20);
        controller.backoff(Integer.MAX_VALUE - 5, 10, 1, 100);

        assertTrue(controller.blocksCategory(Integer.MIN_VALUE + 3));
        assertEquals(ControllerState.PROBE, controller.current(Integer.MIN_VALUE + 4));
    }

    private static SpawnControllerState throttledAt(int tick)
    {
        SpawnControllerState controller = new SpawnControllerState();
        controller.throttle(tick);
        return controller;
    }

    private static void completeProbe(SpawnControllerState controller, int tick, boolean... outcomes)
    {
        for (boolean outcome : outcomes)
        {
            assertTrue(controller.tryAcquireAttempt(tick));
            controller.recordOutcome(outcome, tick);
        }
    }
}
