package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SpawnAttemptTrackerTest
{
    @Test
    void everyAttemptHasExactlyOneOutcome()
    {
        SpawnAttemptTracker tracker = new SpawnAttemptTracker();
        try (SpawnAttemptTracker.Attempt attempt = tracker.begin())
        {
            attempt.advance(SpawnAttemptStage.PLAYER_PROXIMITY);
            attempt.fail(SpawnFailureReason.NO_NEARBY_PLAYER);
            attempt.succeed();
        }
        try (SpawnAttemptTracker.Attempt attempt = tracker.begin())
        {
            attempt.succeed();
        }
        try (SpawnAttemptTracker.Attempt ignored = tracker.begin())
        {
            // Closing an unresolved attempt records one explicit fallback reason.
        }

        SpawnAttemptSnapshot snapshot = tracker.snapshot();
        assertEquals(3, snapshot.attempts());
        assertEquals(1, snapshot.successes());
        assertEquals(1, snapshot.failures().get(SpawnFailureReason.NO_NEARBY_PLAYER));
        assertEquals(1, snapshot.failures().get(SpawnFailureReason.UNCLASSIFIED));
    }

    @Test
    void transientFailuresCannotEnterALocationCache()
    {
        assertFalse(SpawnFailureReason.NO_NEARBY_PLAYER.mayCache());
        assertFalse(SpawnFailureReason.PLAYER_DISTANCE_OR_SPAWN_POINT.mayCache());
        assertFalse(SpawnFailureReason.PER_MOB_POPULATION_LIMIT.mayCache());
        assertFalse(SpawnFailureReason.LOADER_OR_POSITION_VETO.mayCache());
        for (SpawnFailureReason reason : SpawnFailureReason.values())
        {
            assertFalse(reason.mayCache());
            assertEquals(SpawnCacheScope.NONE, reason.cacheScope());
        }
    }

    @Test
    void completionCallbackRunsExactlyOnce()
    {
        SpawnAttemptTracker tracker = new SpawnAttemptTracker();
        AtomicInteger completions = new AtomicInteger();
        try (SpawnAttemptTracker.Attempt attempt = tracker.begin(success -> completions.incrementAndGet()))
        {
            attempt.fail(SpawnFailureReason.NO_NEARBY_PLAYER);
            attempt.succeed();
        }

        assertEquals(1, completions.get());
    }

    @Test
    void untrackedAttemptHasNoAccountingSideEffects()
    {
        SpawnAttemptTracker.Attempt attempt = SpawnAttemptTracker.untracked();
        attempt.advance(SpawnAttemptStage.ENTITY_CREATION);
        attempt.fail(SpawnFailureReason.ENTITY_CREATION_FAILED);
        attempt.close();
    }
}
