package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickExpiryTest
{
    @Test
    void expiresOnlyWhenTheTtlHasElapsed()
    {
        assertFalse(TickExpiry.hasElapsed(100, 699, 600));
        assertTrue(TickExpiry.hasElapsed(100, 700, 600));
        assertTrue(TickExpiry.hasElapsed(100, 701, 600));
    }

    @Test
    void handlesTickCounterWraparound()
    {
        assertFalse(TickExpiry.hasElapsed(Integer.MAX_VALUE - 5, Integer.MIN_VALUE + 3, 10));
        assertTrue(TickExpiry.hasElapsed(Integer.MAX_VALUE - 5, Integer.MIN_VALUE + 4, 10));
    }

    @Test
    void nonPositiveTtlExpiresImmediately()
    {
        assertTrue(TickExpiry.hasElapsed(100, 100, 0));
        assertTrue(TickExpiry.hasElapsed(100, 100, -1));
    }
}
