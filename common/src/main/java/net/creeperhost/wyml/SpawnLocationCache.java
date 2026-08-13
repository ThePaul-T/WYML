package net.creeperhost.wyml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/** Caches only reusable, entity-typed placement failures. */
public final class SpawnLocationCache
{
    private final Map<Key, Entry> failures = new HashMap<Key, Entry>();

    public synchronized boolean isKnownFailure(long position, String entityType, Object placementIdentity, long ruleGeneration, int currentTick, int ttl)
    {
        if (entityType == null || placementIdentity == null) return false;

        Key key = new Key(position, entityType, placementIdentity);
        Entry entry = failures.get(key);
        if (entry == null) return false;

        if (entry.ruleGeneration != ruleGeneration || TickExpiry.hasElapsed(entry.savedTick, currentTick, ttl))
        {
            failures.remove(key);
            return false;
        }
        return true;
    }

    public synchronized boolean recordFailure(long position, String entityType, Object placementIdentity, SpawnFailureReason reason, long ruleGeneration, int currentTick, int ttl)
    {
        if (entityType == null || placementIdentity == null || reason == null || !reason.mayCache()) return false;

        Key key = new Key(position, entityType, placementIdentity);
        Entry existing = failures.get(key);
        if (existing != null && existing.ruleGeneration == ruleGeneration && !TickExpiry.hasElapsed(existing.savedTick, currentTick, ttl))
        {
            // A cache hit must not refresh its own lifetime.
            return false;
        }

        failures.put(key, new Entry(ruleGeneration, currentTick));
        return true;
    }

    public synchronized boolean recordSuccess(long position, String entityType)
    {
        if (entityType == null) return false;

        boolean removed = false;
        Iterator<Key> iterator = failures.keySet().iterator();
        while (iterator.hasNext())
        {
            Key key = iterator.next();
            if (key.position == position && key.entityType.equals(entityType))
            {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    public synchronized int cleanExpired(long ruleGeneration, int currentTick, int ttl)
    {
        int removed = 0;
        Iterator<Map.Entry<Key, Entry>> iterator = failures.entrySet().iterator();
        while (iterator.hasNext())
        {
            Entry entry = iterator.next().getValue();
            if (entry.ruleGeneration != ruleGeneration || TickExpiry.hasElapsed(entry.savedTick, currentTick, ttl))
            {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    public synchronized int size()
    {
        return failures.size();
    }

    private static final class Entry
    {
        private final long ruleGeneration;
        private final int savedTick;

        private Entry(long ruleGeneration, int savedTick)
        {
            this.ruleGeneration = ruleGeneration;
            this.savedTick = savedTick;
        }
    }

    private static final class Key
    {
        private final long position;
        private final String entityType;
        private final Object placementIdentity;

        private Key(long position, String entityType, Object placementIdentity)
        {
            this.position = position;
            this.entityType = entityType;
            this.placementIdentity = placementIdentity;
        }

        @Override
        public boolean equals(Object other)
        {
            if (this == other) return true;
            if (!(other instanceof Key)) return false;
            Key key = (Key) other;
            return position == key.position && Objects.equals(placementIdentity, key.placementIdentity) && entityType.equals(key.entityType);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(position, entityType, placementIdentity);
        }
    }
}
