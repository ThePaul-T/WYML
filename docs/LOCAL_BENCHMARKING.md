# Local before/after benchmarks

## Full Minecraft tick-time comparison

Run the complete profile matrix with Java 25:

```powershell
$env:JAVA_HOME = 'C:\Users\thegi\.jdks\openjdk-25.0.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew runtimeBenchmark
```

This command runs every dedicated-server sample sequentially; no loaders, profiles, or repetitions overlap. It completes all Fabric samples before starting NeoForge. Each profile runs three times by default.

The profile matrix is:

| Profile | Purpose |
| --- | --- |
| `all_off` | `ENABLE_WYML=false` and no WYML mixins; full baseline. |
| `all_on` | Normal benchmarked server features enabled. |
| `without_spawn_controller` | Same as `all_on`, with spawn throttling/backoff disabled. |
| `without_category_policy` | Same as `all_on`, with category cap/despawn policy disabled. |
| `without_per_mob_rules` | Same as `all_on`, with generated per-mob rules disabled. |
| `without_item_lifetime` | Dedicated expiry workload with the item-lifetime hook disabled; all 1,600 spread items remain. |
| `item_lifetime_on` | Matching expiry workload with a 400-tick WYML lifetime; items expire before measurement. |
| `without_item_merging` | Same as `all_on`, with merge staggering disabled. |
| `without_entity_pushing` | Same as `all_on`, with push-query staggering disabled. |
| `paper_bag_off` | Matched 200-item spill with automatic Paper Bags disabled; all item entities must remain. |
| `paper_bag_on` | Same spill with Paper Bags enabled; exactly one bag must conserve all 200 items. |

Each feature comparison uses its `without_...` average as "off." Most use the shared `all_on` average as "on." Item lifetime and Paper Bags instead use matching dedicated off/on profiles so their results measure useful entity removal rather than a cheap branch hidden beneath unrelated dense-item work. Post-load GC is a one-shot startup action rather than steady tick work, and numeric ping is client-only.

Before every repetition, its disposable world and configuration are deleted and recreated; loader-managed launch caches and old logs may remain in the ignored run directory. Every generated `server.properties` uses `level-name=world`, an ephemeral local server port, and the same default seed, `8675309`. Thus every measurement starts from a newly generated same-seed world rather than reusing chunks from another sample or colliding with a separately running development server.

The all-off profile sets both `ENABLE_WYML=false` and `master_enabled=false`. The small benchmark controller remains loaded in every profile so it can create the identical workload, measure ticks, write JSON, and stop the server; it runs the same position reset outside the timed region in every profile.

After world preparation, the controller force-loads the required chunks and normally creates a dense workload of 300 no-AI cows and 400 item entities that cannot merge with one another. The dedicated lifetime pair instead uses one cow and 1,600 no-gravity items spaced two blocks apart. Its 400-tick configured lifetime removes the items only in the on profile; both profiles warm up for 500 ticks so measurement begins after expiry. The Paper Bag pair uses one cow and 200 collocated, non-merging items, warming up for 100 ticks. Other profiles warm up for 200 ticks. Every profile records 600 complete server tick durations using PolyLib's shared tick start/end events and reports mean, median, p95, p99, minimum, maximum, standard deviation, and ticks over 50 ms. A run fails instead of publishing misleading data unless its expected live workload, continuous entity ticking, Paper Bag count, and stored-item conservation all match the profile. Each server then shuts down cleanly.

Each raw sample is retained at:

- `fabric/build/reports/wyml-runtime/raw/<profile>/run-<n>.json`
- `neoforge/build/reports/wyml-runtime/raw/<profile>/run-<n>.json`

Aggregated JSON is written to:

- `fabric/build/reports/wyml-runtime/summary.json`
- `neoforge/build/reports/wyml-runtime/summary.json`
- `build/reports/wyml-runtime/comparison.json`

To regenerate those summaries from existing raw samples without launching either server, run:

```powershell
.\gradlew runtimeBenchmark -PruntimeBenchmarkReportOnly=true
```

The summaries retain all three parsed samples under each profile and calculate the arithmetic mean of each run's mean, median, p95, p99, minimum, maximum, standard deviation, and over-50-ms count. Comparisons define difference as `on - off`; a negative tick-time difference means the on profile was faster. Compare feature off/on within one loader. Do not treat the absolute Fabric-versus-NeoForge difference as a WYML result because their development runtimes and loader overhead differ.

The run can be tuned without editing files:

```powershell
.\gradlew runtimeBenchmark `
  -PruntimeBenchmarkSeed=8675309 `
  -PruntimeBenchmarkRepetitions=5 `
  -PruntimeBenchmarkWarmupTicks=400 `
  -PruntimeBenchmarkMeasureTicks=1200 `
  -PruntimeBenchmarkCows=500 `
  -PruntimeBenchmarkItems=750 `
  -PruntimeBenchmarkLifetimeItems=2400 `
  -PruntimeBenchmarkPaperBagItems=400
```

Three repetitions is the default and minimum recommended comparison. Longer measurement windows and additional repetitions reduce noise. Close games and other heavy applications while comparing results. Generated worlds remain available for inspection after a run but are deleted automatically when that exact profile/repetition runs again.

## Deterministic policy comparison

Run both loader benchmarks with Java 25:

```powershell
$env:JAVA_HOME = 'C:\Users\thegi\.jdks\openjdk-25.0.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew :fabric:wymlBenchmark :neoforge:wymlBenchmark
```

The tasks write separate reports under:

- `fabric/build/reports/wyml-benchmark/`
- `neoforge/build/reports/wyml-benchmark/`

Each directory contains a single canonical `latest.json` report. Every deterministic scenario runs three times; the JSON stores each run under `runs` and the arithmetic result under `average_metrics`. Build directories are ignored by Git.

To keep named snapshots around while changing code, supply a label:

```powershell
.\gradlew :fabric:wymlBenchmark :neoforge:wymlBenchmark -PbenchmarkLabel=before
.\gradlew :fabric:wymlBenchmark :neoforge:wymlBenchmark -PbenchmarkLabel=after
```

Each loader's normal `check` task runs its benchmark, so deterministic invariant failures fail a local verification build. The shared scenario source lives in `common/src/benchmark/java` to prevent Fabric and NeoForge from drifting, but it is compiled and executed separately against each loader's runtime classpath.

## Current scenarios

- `stationary_item_merging` reproduces the historical item-starvation bug and compares it with the current deterministic 40-tick phases.
- `crowded_entity_pushing` compares the legacy post-query gate with the current pre-query four-tick schedule.
- `pathological_spawn_failures` compares an all-off expensive-failure workload with bounded backoff and probes.
- `healthy_spawn_workload` verifies that an active controller does not reduce healthy predicate calls.

These reports deliberately count expensive operations and scheduling fairness. They are stable across machines and suitable for regression checks, but they are not substitutes for Minecraft runtime measurements such as MSPT, entity tick time, packet counts, or bytes per second. Runtime benchmark scenarios should be added separately once their test worlds and start/stop controls are reproducible.
