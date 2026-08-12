package net.creeperhost.wyml.scheduling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterministicTickSchedulerTest
{
    @Test
    void sameTickCohortIsSpreadAcrossEveryPhase()
    {
        int scheduled = 0;
        for (int entityId = 0; entityId < 40; entityId++)
        {
            if (DeterministicTickScheduler.shouldRun(80, entityId, 40)) scheduled++;
        }
        assertEquals(1, scheduled);
    }

    @Test
    void everyEntityGetsOneOpportunityPerInterval()
    {
        for (int entityId = 1; entityId <= 64; entityId++)
        {
            int scheduled = 0;
            for (int tick = 100; tick < 140; tick++)
            {
                if (DeterministicTickScheduler.shouldRun(tick, entityId, 40)) scheduled++;
            }
            assertEquals(1, scheduled);
        }
    }

    @Test
    void invalidIntervalsAreSafelyTreatedAsEveryTick()
    {
        assertTrue(DeterministicTickScheduler.shouldRun(7, 99, 0));
        assertTrue(DeterministicTickScheduler.shouldRun(7, 99, -4));
    }

    @Test
    void phaseDoesNotDependOnCallHistory()
    {
        assertTrue(DeterministicTickScheduler.shouldRun(42, 2, 10));
        assertFalse(DeterministicTickScheduler.shouldRun(43, 2, 10));
        assertTrue(DeterministicTickScheduler.shouldRun(52, 2, 10));
    }
}
