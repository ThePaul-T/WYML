package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdentityPopulationCounterTest
{
    @Test
    void addMoveAndRemoveRemainExactAndIdempotent()
    {
        IdentityPopulationCounter<Object, String> counter = new IdentityPopulationCounter<>();
        Object first = new Object();
        Object second = new Object();
        Object neverAdmitted = new Object();

        counter.reassignIfPresent(neverAdmitted, "a");
        assertEquals(0, counter.count("a"));

        counter.assign(first, "a");
        counter.assign(first, "a");
        counter.assign(second, "a");
        assertEquals(2, counter.count("a"));

        counter.assign(first, "b");
        assertEquals(1, counter.count("a"));
        assertEquals(1, counter.count("b"));

        counter.remove(first);
        counter.remove(first);
        assertEquals(0, counter.count("b"));
    }
}
