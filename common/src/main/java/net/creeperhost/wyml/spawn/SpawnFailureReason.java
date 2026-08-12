package net.creeperhost.wyml.spawn;

/**
 * Statistical failure reasons are deliberately distinct from cache permission.
 * Only a narrowly identified stable placement rule may be cached in the future.
 */
public enum SpawnFailureReason
{
    NO_NEARBY_PLAYER(SpawnAttemptStage.PLAYER_PROXIMITY, SpawnCacheScope.NONE),
    PLAYER_DISTANCE_OR_SPAWN_POINT(SpawnAttemptStage.PLAYER_PROXIMITY, SpawnCacheScope.NONE),
    NO_SPAWN_ENTRY(SpawnAttemptStage.SPAWN_ENTRY_SELECTION, SpawnCacheScope.NONE),
    PLACEMENT_OR_PREDICATE_REJECTED(SpawnAttemptStage.PLACEMENT_AND_PREDICATE, SpawnCacheScope.NONE),
    STABLE_PLACEMENT_RULE_REJECTED(SpawnAttemptStage.PLACEMENT_AND_PREDICATE, SpawnCacheScope.ENTITY_RULE_AND_GENERATION),
    ENTITY_CREATION_FAILED(SpawnAttemptStage.ENTITY_CREATION, SpawnCacheScope.NONE),
    PER_MOB_POPULATION_LIMIT(SpawnAttemptStage.PER_MOB_POLICY, SpawnCacheScope.NONE),
    LOADER_OR_POSITION_VETO(SpawnAttemptStage.LOADER_AND_POSITION_RULES, SpawnCacheScope.NONE),
    SPECIAL_SPAWN_HANDLED(SpawnAttemptStage.FINALIZATION, SpawnCacheScope.NONE),
    ADMISSION_REJECTED(SpawnAttemptStage.ADMISSION, SpawnCacheScope.NONE),
    UNCLASSIFIED(SpawnAttemptStage.CANDIDATE, SpawnCacheScope.NONE);

    private final SpawnAttemptStage stage;
    private final SpawnCacheScope cacheScope;

    SpawnFailureReason(SpawnAttemptStage stage, SpawnCacheScope cacheScope)
    {
        this.stage = stage;
        this.cacheScope = cacheScope;
    }

    public SpawnAttemptStage stage()
    {
        return stage;
    }

    public SpawnCacheScope cacheScope()
    {
        return cacheScope;
    }

    public boolean mayCache()
    {
        return cacheScope != SpawnCacheScope.NONE;
    }
}
