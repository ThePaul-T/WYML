package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttemptBudgetPolicyTest
{
    @Test
    void defaultMigrationPreservesLegacyLinearPlayerScaling()
    {
        assertEquals(3, AttemptBudgetPolicy.resolve(-1, 1, "legacy", true, 3));
    }

    @Test
    void explicitBudgetAndScalingOverrideLegacyFields()
    {
        assertEquals(4, AttemptBudgetPolicy.resolve(4, 99, "none", true, 3));
        assertEquals(12, AttemptBudgetPolicy.resolve(4, 99, "linear", false, 3));
    }

    @Test
    void invalidScalingFallsBackToLegacyBehavior()
    {
        assertEquals(6, AttemptBudgetPolicy.resolve(2, 99, "unknown", true, 3));
        assertEquals(2, AttemptBudgetPolicy.resolve(2, 99, "unknown", false, 3));
    }

    @Test
    void linearScalingIsSafeAtZeroPlayersAndOverflow()
    {
        assertEquals(2, AttemptBudgetPolicy.resolve(2, 1, "linear", false, 0));
        assertEquals(Integer.MAX_VALUE,
                AttemptBudgetPolicy.resolve(Integer.MAX_VALUE, 1, "linear", false, 2));
    }
}
