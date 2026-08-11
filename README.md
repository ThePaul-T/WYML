# Why You Make Lag (WYML)

WYML is a server-oriented performance mod that reduces work caused by mob spawning, crowded entities, and large item spills. It monitors spawn attempts per chunk and mob category, then slows or temporarily pauses spawning where repeated attempts are producing little or no useful result.

It also provides configurable mob limits and despawn distances, spreads expensive entity work across ticks, can shorten dropped-item lifetimes, and includes an optional Paper Bag system for collecting large item spills.

This branch targets Minecraft 26.1.2 on Fabric and NeoForge. The old Forge and Architectury layers have been replaced with [PolyLib](https://creeperhost.github.io/PolyLib/latest/), which provides the shared platform, registration, screen, and inventory APIs used by both loaders.

## Requirements

- Minecraft 26.1.2
- Java 25
- Fabric Loader 0.19.3 or newer with Fabric API, or NeoForge 26.1.2.94 or newer
- PolyLib 2.0.11 or newer for the matching loader (Maven artifact `26.1.2-2.0.11`)

## What WYML does

- Samples natural spawn attempts by chunk and mob category.
- Slows spawn processing in chunks receiving too many spawn requests.
- Temporarily pauses spawning in chunks with a high failed-spawn rate.
- Remembers failed spawn positions so Minecraft does not immediately retry known bad locations.
- Applies configurable limits per mob category and, optionally, per individual mob.
- Provides optional hard limits that also remove mobs added by non-natural spawning.
- Configures mob despawn distances.
- Spreads entity pushing and dropped-item merging work across ticks.
- Can shorten dropped-item despawn time, with an item denylist.
- Can collect sufficiently large item spills into Paper Bags.
- Can normalize the server tick loop and request garbage collection after the initial level load.

## Configuration

WYML creates its main configuration on first launch:

```text
config/wyml.json
```

The file is JSON with comments. Boolean options are enabled with `true` and disabled with `false`:

```json5
{
  "ALLOW_PAUSE": false,
  "ALLOW_SLOW": true,
  "ENABLE_PER_MOD_CONFIGS": true,
  "HARD_MOB_LIMITS": false,
  "ALLOW_PAPER_BAGS": false,
  "DEBUG_PRINT": false
}
```

Edit the generated file rather than replacing it with this shortened example. WYML checks the file for changes approximately every 10 seconds. A server restart is still recommended after editing because some values are copied or cached during startup. Paper Bag and per-mob rule changes require a restart to apply reliably.

If the file cannot be parsed, WYML uses its defaults for that session. Keep a backup before making large changes.

### Main feature switches

There is no single global on/off switch. Disable the feature groups you do not want:

| Setting | Default | Effect |
| --- | ---: | --- |
| `ALLOW_PAUSE` | `true` | Enables temporary spawn pauses in chunks with a high failed-spawn rate. |
| `ALLOW_SLOW` | `true` | Enables spawn-rate limiting in busy chunks. |
| `ENABLE_PER_MOD_CONFIGS` | `true` | Enables the generated per-mod and per-mob spawn rules. |
| `HARD_MOB_LIMITS` | `false` | Enforces per-mob limits against non-natural spawns too. Leave off for natural-spawn-only limits. |
| `ALLOW_PAPER_BAGS` | `false` | Enables Paper Bags for large dropped-item spills. Restart after changing this. |
| `NORMALIZE_TICKS` | `true` | Makes the server tick loop wait until the next tick is due, reducing unnecessary CPU use. |
| `NORMALIZE_PUSHING` | `true` | Spreads non-player entity pushing updates across ticks. |
| `NORMALIZE_ITEM_STACK_MERGING` | `true` | Spreads dropped-item stack merging across ticks. |
| `ENABLE_GARBAGE_COLLECTION_LOAD` | `true` | Requests one garbage-collection pass after levels initially load. |
| `DOWNSCALE_MAGIC_NUM` | `true` | Scales WYML's spawning calculation with online player count. |
| `MULTIPLY_BY_PLAYERS` | `true` | Multiplies the slow-mode spawn-attempt allowance by online players. |
| `CLEAN_PRINT` | `true` | Prints cache and spawn-manager counts every 10 seconds. Turn this off after tuning. |
| `DEBUG_PRINT` | `false` | Enables very verbose spawn diagnostics. This can hurt performance. |

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

WYML still hooks the spawning system when these options are off; these switches disable the optional throttling and normalization features rather than unloading the mod.

### Spawn pausing

| Setting | Default | Meaning |
| --- | ---: | --- |
| `PAUSE_RATE` | `65` | Failed-spawn percentage that can trigger a pause. |
| `PAUSE_TICKS` | `1800` | Normal pause duration in ticks (90 seconds at 20 TPS). |
| `RESUME_RATE` | `10` | Successful-spawn percentage used when deciding whether to resume. |
| `PAUSE_MIN` | `256` | Minimum attempted spawns before pausing is allowed. |
| `MINIMUM_PAUSE_PLAYERS` | `2` | Pausing is considered only when the online player count is greater than this value. The default therefore requires at least 3 players. |
| `ALLOW_PAUSE_FORCED` | `false` | Allows pausing in force-loaded chunks. |
| `ALLOW_PAUSE_CLAIMED` | `false` | Allows pausing in FTB Chunks claimed chunks. |
| `PAUSE_CLAIMED_RATE` | `65` | Failed-spawn threshold for claimed chunks. |
| `PAUSE_CLAIMED_TICKS` | `1800` | Pause duration for claimed chunks. |
| `RESUME_CLAIMED_RATE` | `10` | Resume threshold for claimed chunks. |

When FTB Chunks 26.1.2.7 or newer is installed, WYML detects claims through the FTB Chunks API. Claimed-chunk pause settings update immediately when chunks are claimed or unclaimed; FTB Chunks remains optional.

### Spawn slowing and sampling

| Setting | Default | Meaning |
| --- | ---: | --- |
| `MOB_TRIES` | `1` | Base spawn-attempt allowance used while a chunk is in slow mode. |
| `MAX_CHUNK_SPAWN_REQ_TICK` | `12` | Maximum sampled spawn requests per chunk per tick. |
| `SLOW_TICKS` | `600` | How long slow mode remains active after rates return under control. |
| `SAMPLE_TICKS` | `5` | Number of ticks over which spawn activity is sampled and averaged. |
| `SPAWNLOC_CACHE_TICKS` | `600` | How long a failed spawn position remains cached. |
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

Each generated mob entry contains a `name` and `limit`. Change only `limit`; the `name` field identifies the registry entry and should not be edited. For example:

```json5
"zombie": {
  "name": "zombie",
  "limit": 24
}
```

With `HARD_MOB_LIMITS = false`, these rules limit matching natural spawn attempts. With `HARD_MOB_LIMITS = true`, WYML also removes newly added mobs that exceed the configured limit, including mobs created through other spawn paths. Use hard limits carefully around farms, spawners, and other mods.

### Dropped items and Paper Bags

| Setting | Default | Meaning |
| --- | ---: | --- |
| `ITEM_DESPAWN_TIME` | `6000` | Dropped-item lifetime in ticks. WYML only uses this to shorten the vanilla lifetime. `6000` is 5 minutes at 20 TPS. |
| `ITEM_DESPAWN_DENYLIST` | `[]` | Registry IDs excluded from WYML's shortened despawn time. |
| `MIN_ITEM_AGE` | `60` | Minimum dropped-item age before it can be collected into a Paper Bag. |
| `MIN_ITEM_COUNT` | `20` | Minimum number of items in a spill before a Paper Bag is created. |
| `PAPER_BAG_DESPAWN_TIME` | `300` | Paper Bag lifetime in seconds. |

Example denylist:

```json5
"ITEM_DESPAWN_DENYLIST": [
  "minecraft:nether_star",
  "minecraft:diamond"
]
```

Set `ALLOW_PAPER_BAGS` to `true` and restart the server to enable spill collection. Set it back to `false` and restart to disable it.

## Units and tuning advice

- Minecraft normally runs at 20 ticks per second, so divide tick values by 20 for an approximate duration in seconds.
- Change one group of settings at a time and observe server performance before making further changes.
- Keep `DEBUG_PRINT` off unless actively diagnosing spawning.
- Turn `CLEAN_PRINT` off once cache tuning is complete if you do not want periodic log output.
- Be conservative with `HARD_MOB_LIMITS`; it affects more than natural spawning.

## License

See [LICENSE](LICENSE).
