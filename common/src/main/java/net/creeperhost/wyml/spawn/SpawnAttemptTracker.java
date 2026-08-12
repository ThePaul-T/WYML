package net.creeperhost.wyml.spawn;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/** Server-thread accounting for natural-spawn candidates. */
public final class SpawnAttemptTracker
{
    private long attempts;
    private long successes;
    private final EnumMap<SpawnAttemptStage, Long> stages = new EnumMap<>(SpawnAttemptStage.class);
    private final EnumMap<SpawnFailureReason, Long> failures = new EnumMap<>(SpawnFailureReason.class);

    public synchronized Attempt begin()
    {
        return begin(success -> { });
    }

    public synchronized Attempt begin(Consumer<Boolean> completion)
    {
        attempts++;
        reached(SpawnAttemptStage.CANDIDATE);
        return new Attempt(this, completion);
    }

    public synchronized SpawnAttemptSnapshot snapshot()
    {
        return new SpawnAttemptSnapshot(attempts, successes, Map.copyOf(stages), Map.copyOf(failures));
    }

    public static Attempt untracked()
    {
        return new Attempt(null, success -> { });
    }

    private void reached(SpawnAttemptStage stage)
    {
        stages.merge(stage, 1L, Long::sum);
    }

    private synchronized void advance(SpawnAttemptStage previous, SpawnAttemptStage next)
    {
        if (next.ordinal() > previous.ordinal())
        {
            reached(next);
        }
    }

    private synchronized void succeeded()
    {
        successes++;
    }

    private synchronized void failed(SpawnFailureReason reason)
    {
        failures.merge(reason, 1L, Long::sum);
    }

    public static final class Attempt implements AutoCloseable
    {
        private final SpawnAttemptTracker tracker;
        private final Consumer<Boolean> completion;
        private SpawnAttemptStage stage = SpawnAttemptStage.CANDIDATE;
        private boolean completed;

        private Attempt(SpawnAttemptTracker tracker, Consumer<Boolean> completion)
        {
            this.tracker = tracker;
            this.completion = completion;
        }

        public void advance(SpawnAttemptStage next)
        {
            if (completed || next.ordinal() <= stage.ordinal()) return;
            if (tracker != null) tracker.advance(stage, next);
            stage = next;
        }

        public void fail(SpawnFailureReason reason)
        {
            if (completed) return;
            advance(reason.stage());
            if (tracker != null) tracker.failed(reason);
            completed = true;
            completion.accept(false);
        }

        public void succeed()
        {
            if (completed) return;
            advance(SpawnAttemptStage.ADMISSION);
            if (tracker != null) tracker.succeeded();
            completed = true;
            completion.accept(true);
        }

        @Override
        public void close()
        {
            if (!completed)
            {
                fail(SpawnFailureReason.UNCLASSIFIED);
            }
        }
    }
}
