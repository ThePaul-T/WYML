package net.creeperhost.wyml.spawn;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** Identity-based assignment counter with idempotent add/move/remove semantics. */
public final class IdentityPopulationCounter<E, K>
{
    private final IdentityHashMap<E, K> assignments = new IdentityHashMap<>();
    private final Map<K, Integer> counts = new HashMap<>();

    public void assign(E entity, K key)
    {
        K previous = assignments.put(entity, key);
        if (key.equals(previous)) return;
        if (previous != null) decrement(previous);
        counts.merge(key, 1, Integer::sum);
    }

    public void remove(E entity)
    {
        K previous = assignments.remove(entity);
        if (previous != null) decrement(previous);
    }

    public void reassignIfPresent(E entity, K key)
    {
        if (assignments.containsKey(entity)) assign(entity, key);
    }

    public int count(K key)
    {
        return counts.getOrDefault(key, 0);
    }

    public void clear()
    {
        assignments.clear();
        counts.clear();
    }

    private void decrement(K key)
    {
        counts.computeIfPresent(key, (ignored, value) -> value <= 1 ? null : value - 1);
    }
}
