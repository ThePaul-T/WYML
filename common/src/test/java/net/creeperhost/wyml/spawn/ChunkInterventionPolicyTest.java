package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkInterventionPolicyTest
{
    @Test
    void claimedAndForcedExemptionsAreIndependent()
    {
        assertFalse(ChunkInterventionPolicy.allows(true, true, false, false, true));
        assertTrue(ChunkInterventionPolicy.allows(true, true, false, true, false));
        assertFalse(ChunkInterventionPolicy.allows(true, false, true, true, false));
        assertTrue(ChunkInterventionPolicy.allows(true, false, true, false, true));
    }

    @Test
    void disabledInterventionNeverRuns()
    {
        assertFalse(ChunkInterventionPolicy.allows(false, false, false, true, true));
    }
}
