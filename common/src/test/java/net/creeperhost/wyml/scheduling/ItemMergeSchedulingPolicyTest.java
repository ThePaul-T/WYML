package net.creeperhost.wyml.scheduling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemMergeSchedulingPolicyTest
{
    @Test
    void vanillaRejectedItemNeverInitiatesANeighborhoodQuery()
    {
        assertFalse(ItemMergeSchedulingPolicy.shouldInitiate(false, 40, 0, 40));
    }

    @Test
    void eligibleItemKeepsItsDeterministicOpportunity()
    {
        assertTrue(ItemMergeSchedulingPolicy.shouldInitiate(true, 40, 0, 40));
        assertFalse(ItemMergeSchedulingPolicy.shouldInitiate(true, 41, 0, 40));
    }
}
