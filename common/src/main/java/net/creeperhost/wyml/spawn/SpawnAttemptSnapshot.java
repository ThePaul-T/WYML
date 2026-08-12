package net.creeperhost.wyml.spawn;

import java.util.Map;

public record SpawnAttemptSnapshot(
        long attempts,
        long successes,
        Map<SpawnAttemptStage, Long> stages,
        Map<SpawnFailureReason, Long> failures)
{
}
