package net.creeperhost.wyml.spawn;

/**
 * Statistical failure reasons are deliberately distinct from cache permission.
 * Minecraft's current natural-spawn predicate combines distance, structures,
 * collision, random placement rules, and loader hooks, so no observed failure
 * is safe for reusable location caching. Bounded backoff provides the saved
 * work without retaining a stale position decision.
 */
public enum SpawnFailureReason
{
    NO_NEARBY_PLAYER(SpawnAttemptStage.PLAYER_PROXIMITY),
    PLAYER_DISTANCE_OR_SPAWN_POINT(SpawnAttemptStage.PLAYER_PROXIMITY),
    NO_SPAWN_ENTRY(SpawnAttemptStage.SPAWN_ENTRY_SELECTION),
    PLACEMENT_OR_PREDICATE_REJECTED(SpawnAttemptStage.PLACEMENT_AND_PREDICATE),
    ENTITY_CREATION_FAILED(SpawnAttemptStage.ENTITY_CREATION),
    PER_MOB_POPULATION_LIMIT(SpawnAttemptStage.PER_MOB_POLICY),
    LOADER_OR_POSITION_VETO(SpawnAttemptStage.LOADER_AND_POSITION_RULES),
    ADMISSION_REJECTED(SpawnAttemptStage.ADMISSION),
    UNCLASSIFIED(SpawnAttemptStage.CANDIDATE);

    private final SpawnAttemptStage stage;
    SpawnFailureReason(SpawnAttemptStage stage)
    {
        this.stage = stage;
    }

    public SpawnAttemptStage stage()
    {
        return stage;
    }

    public SpawnCacheScope cacheScope()
    {
        return SpawnCacheScope.NONE;
    }

    public boolean mayCache()
    {
        return false;
    }
}
