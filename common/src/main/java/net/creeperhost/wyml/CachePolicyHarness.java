package net.creeperhost.wyml;

import java.util.Arrays;

/**
 * Dependency-free regression harness for the old 1.16.5 Gradle line.
 * Run with {@code ./gradlew :common:runCachePolicyHarness}.
 */
public final class CachePolicyHarness
{
    private CachePolicyHarness()
    {
    }

    public static void main(String[] args)
    {
        managerExpiryUsesElapsedTime();
        tickExpiryHandlesCounterWrap();
        transientFailuresAreNeverCached();
        failuresAreScopedByPositionAndEntityType();
        structuralPlacementIdentitySurvivesHashCollisions();
        aCacheHitSkipsOnlyItsCandidate();
        cacheHitsDoNotRefreshTheirLifetime();
        configGenerationInvalidatesFailures();
        successAndCleanupRemoveFailures();
        System.out.println("WYML cache policy harness passed");
    }

    private static void managerExpiryUsesElapsedTime()
    {
        check(!TickExpiry.managerHasExpired(true, false, 100, 699, 600), "manager expired before TTL");
        check(TickExpiry.managerHasExpired(true, false, 100, 700, 600), "manager did not expire at TTL");
        check(!TickExpiry.managerHasExpired(false, false, 100, 700, 600), "dirty manager expired");
        check(!TickExpiry.managerHasExpired(true, true, 100, 700, 600), "paused manager expired");
    }

    private static void tickExpiryHandlesCounterWrap()
    {
        check(!TickExpiry.hasElapsed(Integer.MAX_VALUE - 5, Integer.MIN_VALUE + 3, 10), "tick wrap expired early");
        check(TickExpiry.hasElapsed(Integer.MAX_VALUE - 5, Integer.MIN_VALUE + 4, 10), "tick wrap missed TTL boundary");
    }

    private static void transientFailuresAreNeverCached()
    {
        SpawnLocationCache cache = new SpawnLocationCache();
        for (SpawnFailureReason reason : SpawnFailureReason.values())
        {
            if (reason == SpawnFailureReason.STABLE_PLACEMENT_RULE_REJECTED) continue;
            check(!cache.recordFailure(1L, "minecraft:cow", 10, reason, 1L, 100, 600), "cached transient reason " + reason);
        }
        check(cache.size() == 0, "transient failure changed cache size");
    }

    private static void failuresAreScopedByPositionAndEntityType()
    {
        SpawnLocationCache cache = new SpawnLocationCache();
        check(cache.recordFailure(1L, "minecraft:cow", 10, SpawnFailureReason.STABLE_PLACEMENT_RULE_REJECTED, 1L, 100, 600), "stable failure was not cached");
        check(cache.isKnownFailure(1L, "minecraft:cow", 10, 1L, 101, 600), "matching typed failure was not reused");
        check(!cache.isKnownFailure(1L, "minecraft:sheep", 10, 1L, 101, 600), "cow failure suppressed sheep");
        check(!cache.isKnownFailure(2L, "minecraft:cow", 10, 1L, 101, 600), "failure leaked to another position");
        check(!cache.isKnownFailure(1L, "minecraft:cow", 11, 1L, 101, 600), "failure survived a placement-state change");
    }

    private static void cacheHitsDoNotRefreshTheirLifetime()
    {
        SpawnLocationCache cache = new SpawnLocationCache();
        cache.recordFailure(1L, "minecraft:cow", 10, SpawnFailureReason.STABLE_PLACEMENT_RULE_REJECTED, 1L, 100, 600);
        check(!cache.recordFailure(1L, "minecraft:cow", 10, SpawnFailureReason.STABLE_PLACEMENT_RULE_REJECTED, 1L, 500, 600), "cache hit refreshed timestamp");
        check(cache.isKnownFailure(1L, "minecraft:cow", 10, 1L, 699, 600), "cache expired before exact TTL");
        check(!cache.isKnownFailure(1L, "minecraft:cow", 10, 1L, 700, 600), "cache survived exact TTL");
    }

    private static void structuralPlacementIdentitySurvivesHashCollisions()
    {
        SpawnLocationCache cache = new SpawnLocationCache();
        Object first = Arrays.asList("Aa");
        Object second = Arrays.asList("BB");
        check(first.hashCode() == second.hashCode(), "test identities must collide");
        cache.recordFailure(1L, "minecraft:cow", first, SpawnFailureReason.STABLE_PLACEMENT_RULE_REJECTED, 1L, 100, 600);
        check(!cache.isKnownFailure(1L, "minecraft:cow", second, 1L, 101, 600), "hash collision reused a different placement state");
    }

    private static void aCacheHitSkipsOnlyItsCandidate()
    {
        SpawnLocationCache cache = new SpawnLocationCache();
        cache.recordFailure(1L, "minecraft:cod", 10, SpawnFailureReason.STABLE_PLACEMENT_RULE_REJECTED, 1L, 100, 600);
        int candidatesEvaluated = 0;
        for (long position = 1L; position <= 3L; position++)
        {
            if (cache.isKnownFailure(position, "minecraft:cod", 10, 1L, 101, 600)) continue;
            candidatesEvaluated++;
        }
        check(candidatesEvaluated == 2, "cache hit aborted more than its own candidate");
    }

    private static void configGenerationInvalidatesFailures()
    {
        SpawnLocationCache cache = new SpawnLocationCache();
        cache.recordFailure(1L, "minecraft:cow", 10, SpawnFailureReason.STABLE_PLACEMENT_RULE_REJECTED, 1L, 100, 600);
        check(!cache.isKnownFailure(1L, "minecraft:cow", 10, 2L, 101, 600), "old rule generation survived reload");
    }

    private static void successAndCleanupRemoveFailures()
    {
        SpawnLocationCache cache = new SpawnLocationCache();
        cache.recordFailure(1L, "minecraft:cow", 10, SpawnFailureReason.STABLE_PLACEMENT_RULE_REJECTED, 1L, 100, 600);
        check(cache.recordSuccess(1L, "minecraft:cow"), "success did not clear failure");
        cache.recordFailure(1L, "minecraft:cow", 10, SpawnFailureReason.STABLE_PLACEMENT_RULE_REJECTED, 1L, 100, 600);
        cache.recordFailure(2L, "minecraft:cow", 10, SpawnFailureReason.STABLE_PLACEMENT_RULE_REJECTED, 1L, 200, 600);
        check(cache.cleanExpired(1L, 700, 600) == 1, "cleanup did not remove exactly the expired entry");
        check(cache.size() == 1, "cleanup removed a live entry");
    }

    private static void check(boolean condition, String message)
    {
        if (!condition) throw new AssertionError(message);
    }
}
