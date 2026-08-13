package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickExpiryTest
{
    @Test
    void expiresAtTheConfiguredBoundary()
    {
        assertFalse(TickExpiry.hasElapsed(100, 699, 600));
        assertTrue(TickExpiry.hasElapsed(100, 700, 600));
    }

    @Test
    void handlesTickCounterWraparound()
    {
        assertFalse(TickExpiry.hasElapsed(Integer.MAX_VALUE - 5, Integer.MIN_VALUE + 3, 10));
        assertTrue(TickExpiry.hasElapsed(Integer.MAX_VALUE - 5, Integer.MIN_VALUE + 4, 10));
    }
}
