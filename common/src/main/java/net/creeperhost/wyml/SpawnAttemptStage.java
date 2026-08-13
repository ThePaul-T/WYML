package net.creeperhost.wyml;

/** Ordered stages reached by one natural-spawn candidate. */
public enum SpawnAttemptStage
{
    CANDIDATE,
    PLAYER_PROXIMITY,
    SPAWN_ENTRY_SELECTION,
    PLACEMENT_AND_PREDICATE,
    ENTITY_CREATION,
    PER_MOB_POLICY,
    LOADER_AND_POSITION_RULES,
    FINALIZATION,
    ADMISSION
}
