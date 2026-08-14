# Why You Make Lag (WYML)

WYML is a server-oriented performance mod that reduces work caused by mob spawning, crowded entities, and large item spills. It monitors spawn attempts per chunk and mob category, then slows or temporarily pauses spawning where repeated attempts are producing little or no useful result.

It also provides configurable mob limits and despawn distances, spreads expensive entity work across ticks, can shorten dropped-item lifetimes, and includes an optional Paper Bag system for collecting large item spills.

## Latest local benchmark results

### Fabric

| Comparison | Off average (MSPT) | On average (MSPT) | On - off (MSPT) | Difference |
| --- | ---: | ---: | ---: | ---: |
| All WYML features | 19.368 | 11.347 | -8.021 | -41.41% |
| Spawn controller | 13.693 | 11.347 | -2.346 | -17.13% |
| Category policy | 13.248 | 11.347 | -1.901 | -14.35% |
| Per-mob rules | 14.339 | 11.347 | -2.992 | -20.87% |
| Item lifetime (1,600-item expiry) | 4.234 | 0.632 | -3.602 | -85.08% |
| Paper Bags (200-item spill) | 3.182 | 0.620 | -2.562 | -80.53% |
| Item merging | 11.619 | 11.347 | -0.272 | -2.34% |
| Entity pushing | 20.296 | 11.347 | -8.949 | -44.09% |

### NeoForge

| Comparison | Off average (MSPT) | On average (MSPT) | On - off (MSPT) | Difference |
| --- | ---: | ---: | ---: | ---: |
| All WYML features | 16.865 | 11.653 | -5.212 | -30.91% |
| Spawn controller | 11.769 | 11.653 | -0.116 | -0.99% |
| Category policy | 10.810 | 11.653 | +0.843 | +7.79% |
| Per-mob rules | 11.084 | 11.653 | +0.569 | +5.13% |
| Item lifetime (1,600-item expiry) | 4.381 | 0.720 | -3.661 | -83.56% |
| Paper Bags (200-item spill) | 3.317 | 0.637 | -2.681 | -80.81% |
| Item merging | 12.094 | 11.653 | -0.441 | -3.65% |
| Entity pushing | 21.306 | 11.653 | -9.653 | -45.31% |

All rows except item lifetime and Paper Bags use a dense workload of 300 cows and 400 non-merging items, with the named feature disabled compared against the shared all-on profile. Item lifetime uses a matched expiry workload instead: 1,600 no-gravity items are spread two blocks apart, the enabled profile gives them a 400-tick lifetime, and measurement starts after a 500-tick warmup. Every disabled run retained all 1,600 items; every enabled run had zero remaining. The matched Paper Bag workload starts with 200 item entities; every off run retained all 200 in the world, while every on run finished with zero live items and exactly one bag containing all 200. Each profile is run three times in a fresh same-seed world, and the table reports the average of 600 measured ticks from those runs.

The dense workload directly stresses entity pushing and item merging. Spawn control, category policy, and per-mob rules primarily show their overhead under this particular workload; use the deterministic pathological-spawn scenarios for direct spawn-controller work-reduction evidence. Absolute Fabric and NeoForge values should not be compared as a loader performance ranking because their development runtimes differ.

## What WYML does

- Samples natural spawn attempts by chunk and mob category.
- Slows spawn processing in chunks receiving too many spawn requests.
- Temporarily pauses spawning in chunks with a high failed-spawn rate.
- Uses bounded chunk/category backoff so Minecraft does not repeatedly pay for pathological failing spawn work.
- Applies configurable limits per mob category and, optionally, per individual mob.
- Provides optional hard limits that also remove mobs added by non-natural spawning.
- Configures mob despawn distances.
- Deterministically spreads entity-pushing and dropped-item merge queries across ticks without consuming entity RNG.
- Can shorten dropped-item despawn time, with an item denylist.
- Can collect sufficiently large item spills into Paper Bags.
- Retains the old tick-normalization setting as a migration-only no-op because current Minecraft already performs deadline-based tick waiting.

## Configuration

WYML creates its main configuration on first launch:

```text
config/wyml.json
```

It also creates a restart-only mixin profile:

```text
config/wyml-mixins.properties
```

Runtime settings and thresholds live in `wyml.json`. The mixin profile controls which WYML transformations are installed and is read before normal mod initialization, so changes to it require a restart.

The file is JSON with comments. Boolean options are enabled with `true` and disabled with `false`:

```json5
{
  "ALLOW_PAUSE": false,
  "ALLOW_SLOW": true,
  "ENABLE_PER_MOD_CONFIGS": true,
  "HARD_MOB_LIMITS": false,
  "DISABLE_COUNTING_CHUNK_GENERATED_MOBS": false,
  "ALLOW_PAPER_BAGS": false,
  "DEBUG_PRINT": false
}
```

Edit the generated file rather than replacing it with this shortened example. WYML checks the file for changes approximately every 10 seconds. A server restart is still recommended after editing because some values are copied or cached during startup. Paper Bag and per-mob rule changes require a restart to apply reliably.

If the file cannot be parsed, WYML uses its defaults for that session. Keep a backup before making large changes.

### Main feature switches

`ENABLE_WYML` is the authoritative runtime switch. To obtain the all-off vanilla-behaviour baseline, set it to `false`, set `master_enabled=false` in `wyml-mixins.properties`, and restart. WYML keeps its registered blocks available so existing worlds can still load, but it does not start feature managers or apply WYML mixins.

The remaining feature switches are:

| Setting | Default | Effect |
| --- | ---: | --- |
| `ENABLE_WYML` | `true` | Master runtime switch. Pair with the restart-only mixin master switch for a complete all-off profile. |
| `ENABLE_CATEGORY_CAP_POLICY` | `true` | Independently enables WYML category maximums, despawn distances, and cap scaling. Restart after changing. |
| `ALLOW_PAUSE` | `true` | Enables temporary spawn pauses in chunks with a high failed-spawn rate. |
| `ALLOW_SLOW` | `true` | Enables spawn-rate limiting in busy chunks. |
| `ALLOW_SLOW_CLAIMED` | `true` | Allows slow-mode throttling in FTB Chunks claims, preserving historical behavior. |
| `ALLOW_SLOW_FORCED` | `true` | Allows slow-mode throttling in force-loaded chunks, preserving historical behavior. |
| `ENABLE_PER_MOD_CONFIGS` | `true` | Enables the generated per-mod and per-mob spawn rules. |
| `HARD_MOB_LIMITS` | `false` | Enforces per-mob limits against non-natural spawns too. Leave off for natural-spawn-only limits. |
| `DISABLE_COUNTING_CHUNK_GENERATED_MOBS` | `false` | Legacy escape hatch. When `true`, WYML leaves chunk-generation creature spawning to vanilla without its soft per-mob check or spawn-controller handling. The separate hard-limit admission check still applies when enabled. |
| `ALLOW_PAPER_BAGS` | `false` | Enables Paper Bags for large dropped-item spills. Restart after changing this. |
| `NORMALIZE_TICKS` | `true` | Deprecated migration value. It is a no-op on 26.1.2; Minecraft already waits against its tick deadline. |
| `NORMALIZE_PUSHING` | `true` | Gates ordinary server-side living-entity collision queries before the neighborhood lookup. Players, riders, and vehicles are exempt. |
| `ENTITY_PUSH_INTERVAL` | `4` | Number of ticks between scheduled collision queries for a non-exempt living entity. Values below 1 behave as 1. |
| `NORMALIZE_ITEM_STACK_MERGING` | `true` | Replaces synchronized merge-query ticks with deterministic entity phases while retaining future opportunities. |
| `ITEM_MERGE_MOVING_INTERVAL` | `2` | Merge-query interval after crossing a block-coordinate boundary, matching vanilla's frequency. |
| `ITEM_MERGE_STATIONARY_INTERVAL` | `40` | Merge-query interval while remaining in one block, matching vanilla's frequency. |
| `ENABLE_GARBAGE_COLLECTION_LOAD` | `false` | Advanced one-shot JVM GC request after initial level loading. It can be ignored by the JVM or pause the server; WYML logs its duration and heap measurements. |
| `DOWNSCALE_MAGIC_NUM` | `true` | Reduces the category-cap scaling radius with online player count, raising the configured cap. |
| `MULTIPLY_BY_PLAYERS` | `true` | Legacy slow-budget scaling, used only while `ATTEMPT_BUDGET_PLAYER_SCALING = "legacy"`. |
| `CLEAN_PRINT` | `true` | Prints cache and spawn-manager counts every 10 seconds. Turn this off after tuning. |
| `DEBUG_PRINT` | `false` | Enables very verbose spawn diagnostics. This can hurt performance. |
| `SHOW_NUMERIC_PING` | `true` | Displays numeric latency in the client player list. |

### Restart-only mixin switches

Every property in `wyml-mixins.properties` is a Boolean. `master_enabled=false` prevents all WYML mixins from applying. Individual switches are `spawn_controller`, `category_policy`, `per_mob_rules`, `paper_bags`, `item_lifetime`, `item_merging`, `entity_pushing`, `tick_pacing`, `post_load_gc`, and `numeric_ping`. `tick_pacing` is retained only so old profiles still parse; it no longer owns a mixin.

Some Minecraft targets are shared by multiple modules, so a shared mixin remains applied while any module that needs it is enabled. Disabling a boot module prevents its dedicated mixin from applying where the target is not shared. These switches cover WYML's own mixins; PolyLib can have loader bridge mixins of its own.

If the boot file does not exist, WYML currently generates a legacy-compatible profile with the modules enabled. This preserves an administrator's established behavior. A different default for verifiably new installations remains an explicit release-policy decision.

For a mostly vanilla setup with WYML's spawn throttling disabled, start with:

```json5
{
  "ALLOW_PAUSE": false,
  "ALLOW_SLOW": false,
  "ENABLE_PER_MOD_CONFIGS": false,
  "HARD_MOB_LIMITS": false,
  "ALLOW_PAPER_BAGS": false,
  "NORMALIZE_TICKS": false,
  "NORMALIZE_PUSHING": false,
  "NORMALIZE_ITEM_STACK_MERGING": false,
  "ENABLE_GARBAGE_COLLECTION_LOAD": false
}
```

For the complete no-hook baseline, also use `ENABLE_WYML=false` and `master_enabled=false` as described above.

### Spawn pausing

| Setting | Default | Meaning |
| --- | ---: | --- |
| `PAUSE_RATE` | `65` | Failed-spawn percentage that can trigger a pause. |
| `PAUSE_TICKS` | `1800` | Normal pause duration in ticks (90 seconds at 20 TPS). |
| `RESUME_RATE` | `10` | Successful-spawn percentage used when deciding whether to resume. |
| `PROBE_ATTEMPTS` | `8` | Candidate outcomes allowed in the bounded probe window after a backoff timeout. |
| `PAUSE_MIN` | `256` | Minimum attempted spawns before pausing is allowed. |
| `PAUSE_MIN_PLAYERS` | `-1` | Inclusive player minimum. `-1` migrates the legacy threshold, preserving the effective default of 3 players. |
| `MINIMUM_PAUSE_PLAYERS` | `2` | Legacy exclusive threshold retained for migration. Prefer `PAUSE_MIN_PLAYERS` for new configuration. |
| `ALLOW_PAUSE_FORCED` | `false` | Allows pausing in force-loaded chunks. |
| `ALLOW_PAUSE_CLAIMED` | `false` | Allows pausing in FTB Chunks claimed chunks. |
| `PAUSE_CLAIMED_RATE` | `65` | Failed-spawn threshold for claimed chunks. |
| `PAUSE_CLAIMED_TICKS` | `1800` | Pause duration for claimed chunks. |
| `RESUME_CLAIMED_RATE` | `10` | Resume threshold for claimed chunks. |

When FTB Chunks 26.1.2.7 or newer is installed, WYML detects claims through the FTB Chunks API. Claimed-chunk pause settings update immediately when chunks are claimed or unclaimed; FTB Chunks remains optional.

### Spawn slowing and sampling

| Setting | Default | Meaning |
| --- | ---: | --- |
| `ATTEMPT_BUDGET_PER_WINDOW` | `-1` | Candidate budget per `SAMPLE_TICKS` window while throttled. `-1` migrates `MOB_TRIES`. |
| `ATTEMPT_BUDGET_PLAYER_SCALING` | `legacy` | Budget scaling mode: `legacy`, `none`, or `linear`. |
| `MOB_TRIES` | `1` | Legacy budget value retained for configuration migration. |
| `MAX_CHUNK_SPAWN_REQ_TICK` | `12` | Maximum sampled spawn requests per chunk per tick. |
| `SLOW_TICKS` | `600` | How long slow mode remains active after rates return under control. |
| `SAMPLE_TICKS` | `5` | Number of ticks over which spawn activity is sampled and averaged. |
| `SPAWNLOC_CACHE_TICKS` | `600` | Deprecated no-op migration value. Unsafe reusable location caching is retired; bounded backoff saves repeated work without persisting transient or loader-dependent failures. |
| `MANAGER_CACHE_TICKS` | `600` | How long an inactive per-chunk spawn manager remains cached. |
| `MOJANG_MAGIC_NUM` | `17.0` | Base value used in WYML's spawn-cap calculation. |
| `DOWNSCALE_MAGIC_NUM_MIN` | `8.0` | Minimum value allowed when downscaling is enabled. |

Higher allowances are less restrictive but do more spawning work. Longer sampling and cache durations use information for longer, but can retain more state in memory.

### Mob limits and despawn distances

These category values are applied at startup:

| Category | Per-chunk limit | Despawn distance |
| --- | ---: | ---: |
| Monster | `MONSTER_PER_CHUNK = 70` | `MONSTER_DESPAWN_DISTANCE = 128` |
| Creature | `CREATURES_PER_CHUNK = 10` | `CREATURES_DESPAWN_DISTANCE = 128` |
| Ambient | `AMBIENT_CREATURES_PER_CHUNK = 15` | `AMBIENT_CREATURES_DESPAWN_DISTANCE = 128` |
| Water creature | `WATER_CREATURES_PER_CHUNK = 5` | `WATER_CREATURES_DESPAWN_DISTANCE = 128` |
| Water ambient | `WATER_AMBIENT_PER_CHUNK = 20` | `WATER_AMBIENT_DESPAWN_DISTANCE = 64` |
| Miscellaneous | `MISC_CREATURES_PER_CHUNK = -1` | `MISC_CREATURES_DESPAWN_DISTANCE = 128` |

Restart the server after changing these values.

When `ENABLE_PER_MOD_CONFIGS` is `true`, WYML generates one file per mod namespace in:

```text
config/wyml-SpawnRules/
```

Each generated mob entry contains a `name` and `limit`. Change only `limit`; the `name` field identifies the registry entry and should not be edited. `limit` is the maximum population of that exact entity type in one chunk. It does not change the biome spawn pack's minimum or maximum group size. A negative value, including `-1`, disables the population limit for that entry. For example:

```json5
"zombie": {
  "name": "zombie",
  "limit": 24
}
```

With `HARD_MOB_LIMITS = false`, these rules limit matching natural spawn attempts and, unless `DISABLE_COUNTING_CHUNK_GENERATED_MOBS` is enabled, chunk-generation creature spawns. With `HARD_MOB_LIMITS = true`, WYML also removes a newly admitted mob when that addition takes the chunk over its configured population limit. The current hard-limit path applies to admissions from spawners, commands, breeding, structures, conversions, and passenger trees as well as normal spawning; it does not scan for or reconcile mobs that were already over the limit. Persistent, named, tamed, and boss mobs are not exempt. Use hard limits carefully around farms, pets, bosses, spawners, and other mods.

Generated provider files are published atomically so a server or tool should see either the previous complete file or the replacement, not a partially written rule set. Existing `limit` values are retained when WYML adds newly registered mobs.

### Dropped items and Paper Bags

| Setting | Default | Meaning |
| --- | ---: | --- |
| `ITEM_DESPAWN_TIME` | `6000` | Dropped-item lifetime in ticks. WYML only uses this to shorten the vanilla lifetime. `6000` is 5 minutes at 20 TPS. |
| `ITEM_DESPAWN_DENYLIST` | `[]` | Registry IDs excluded from WYML's shortened despawn time. |
| `MIN_ITEM_AGE` | `60` | Minimum dropped-item age before it can be collected into a Paper Bag. |
| `MIN_ITEM_COUNT` | `20` | Minimum number of items in a spill before a Paper Bag is created. |
| `PAPER_BAG_DESPAWN_TIME` | `300` | Paper Bag lifetime in seconds. |
| `PAPER_BAG_CANDIDATES_PER_TICK` | `4` | Maximum queued spill locations examined per server tick. Values below 1 behave as 1. |
| `PAPER_BAG_COLLECTION_BUDGET` | `128` | Maximum eligible item entities visited for one spill in one tick. Partial work is re-queued. |
| `PAPER_BAG_SCAN_RADIUS` | `4` | Spill and safe-placement search radius, clamped to 1-16 blocks. |
| `PAPER_BAG_EXPIRY_POLICY` | `legacy_void_with_warning` | `legacy_void_with_warning` prevents the original spill from being recreated; `persist_while_non_empty` keeps a non-empty bag. |

Example denylist:

```json5
"ITEM_DESPAWN_DENYLIST": [
  "minecraft:nether_star",
  "minecraft:diamond"
]
```

Set `ALLOW_PAPER_BAGS` to `true` and restart the server to enable spill collection. Set it back to `false` and restart to disable it.

Paper Bag thresholds count eligible item entities, including the entity that triggered the spill—not the number of items inside their stacks. Collection runs later on the server thread with bounded work, waits for newly admitted entities to become spatially visible, never overwrites a non-air block, and leaves any uninserted remainder in the world. Its 180 inventory slots are exposed through a scrolling PolyLib menu.

The default expiry policy preserves the historical anti-lag behavior: an expired non-empty bag is voided after a warning that records its item and slot counts. Set `PAPER_BAG_EXPIRY_POLICY` to `persist_while_non_empty` if item preservation is more important for your server; the bag then renews its deadline until emptied. Breaking a bag normally drops its contents. WYML registers custom Paper Bag content on both loaders, so clients connecting to a server with WYML currently need the mod installed as well, even when automatic spill collection is disabled.

## Units and tuning advice

- Minecraft normally runs at 20 ticks per second, so divide tick values by 20 for an approximate duration in seconds.
- Change one group of settings at a time and observe server performance before making further changes.
- Keep `DEBUG_PRINT` off unless actively diagnosing spawning.
- Turn `CLEAN_PRINT` off once cache tuning is complete if you do not want periodic log output.
- Be conservative with `HARD_MOB_LIMITS`; it affects more than natural spawning.

## License

See [LICENSE](LICENSE).
