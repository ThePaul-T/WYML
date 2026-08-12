package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PauseEligibilityTest
{
    @Test
    void legacyTwoRemainsAnEffectiveThreePlayerMinimum()
    {
        int minimum = PauseEligibility.inclusiveMinimum(-1, 2);

        assertEquals(3, minimum);
        assertFalse(PauseEligibility.hasMinimumPlayers(0, minimum));
        assertFalse(PauseEligibility.hasMinimumPlayers(1, minimum));
        assertFalse(PauseEligibility.hasMinimumPlayers(2, minimum));
        assertTrue(PauseEligibility.hasMinimumPlayers(3, minimum));
    }

    @Test
    void explicitInclusiveValueWinsAfterMigration()
    {
        assertEquals(2, PauseEligibility.inclusiveMinimum(2, 99));
        assertTrue(PauseEligibility.hasMinimumPlayers(2, 2));
    }
}
