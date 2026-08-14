package net.creeperhost.wyml.paperbag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperBagSpillPolicyTest
{
    @Test
    void minimumCountIncludesTheTriggerEntity()
    {
        assertTrue(PaperBagSpillPolicy.qualifies(20, 60, 20, 60));
        assertFalse(PaperBagSpillPolicy.qualifies(19, 60, 20, 60));
    }

    @Test
    void oldestEligibleEntityControlsAgeThreshold()
    {
        assertTrue(PaperBagSpillPolicy.qualifies(20, 61, 20, 60));
        assertFalse(PaperBagSpillPolicy.qualifies(20, 59, 20, 60));
    }

    @Test
    void qualifyingPileIsRetriedUntilItsItemsMature()
    {
        assertTrue(PaperBagSpillPolicy.shouldRetryAwaitingAge(20, 59, 20, 60));
        assertFalse(PaperBagSpillPolicy.shouldRetryAwaitingAge(19, 59, 20, 60));
        assertFalse(PaperBagSpillPolicy.shouldRetryAwaitingAge(20, 60, 20, 60));
    }

    @Test
    void budgetsAndRadiusAreBounded()
    {
        assertEquals(1, PaperBagSpillPolicy.positiveBudget(0));
        assertEquals(1, PaperBagSpillPolicy.radius(-4));
        assertEquals(16, PaperBagSpillPolicy.radius(99));
    }
}
