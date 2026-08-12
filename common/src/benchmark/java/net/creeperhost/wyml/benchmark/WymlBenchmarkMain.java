package net.creeperhost.wyml.benchmark;

import net.creeperhost.wyml.scheduling.DeterministicTickScheduler;
import net.creeperhost.wyml.spawn.ControllerState;
import net.creeperhost.wyml.spawn.SpawnControllerState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic policy benchmark. It compares work counts and fairness rather
 * than treating machine-dependent wall-clock measurements as correctness.
 */
public final class WymlBenchmarkMain
{
    private static final int ENTITY_COUNT = 1_000;
    private static final int SCHEDULING_TICKS = 400;
    private static final int SPAWN_TICKS = 20_000;
    private static final int CANDIDATES_PER_TICK = 12;

    private WymlBenchmarkMain()
    {
    }

    public static void main(String[] args) throws IOException
    {
        Path outputDirectory = Path.of(args.length > 0 ? args[0] : "build/reports/wyml-benchmark");
        String label = sanitizeLabel(args.length > 1 ? args[1] : "latest");
        String loader = sanitizeLabel(args.length > 2 ? args[2] : "unknown");
        List<Scenario> scenarios = List.of(
                benchmarkStationaryItemMerging(),
                benchmarkEntityPushing(),
                benchmarkPathologicalSpawning(),
                benchmarkHealthySpawning());

        Files.createDirectories(outputDirectory);
        Path json = outputDirectory.resolve(label + ".json");
        Files.writeString(json, toJson(label, loader, scenarios), StandardCharsets.UTF_8);

        System.out.println("WYML deterministic benchmark passed for " + loader + ".");
        System.out.println("JSON report: " + json.toAbsolutePath());
    }

    private static Scenario benchmarkStationaryItemMerging()
    {
        int[] legacyOpportunities = new int[ENTITY_COUNT];
        int[] modernOpportunities = new int[ENTITY_COUNT];
        long legacyQueries = 0;
        long modernQueries = 0;
        int legacyPeak = 0;
        int modernPeak = 0;

        for (int tick = 1; tick <= SCHEDULING_TICKS; tick++)
        {
            int legacyThisTick = 0;
            int modernThisTick = 0;
            for (int entityId = 0; entityId < ENTITY_COUNT; entityId++)
            {
                int legacyOffset = entityId % 20;
                if (tick % 40 == 0 && (tick + legacyOffset) % 20 == 0)
                {
                    legacyQueries++;
                    legacyThisTick++;
                    legacyOpportunities[entityId]++;
                }
                if (DeterministicTickScheduler.shouldRun(tick, entityId, 40))
                {
                    modernQueries++;
                    modernThisTick++;
                    modernOpportunities[entityId]++;
                }
            }
            legacyPeak = Math.max(legacyPeak, legacyThisTick);
            modernPeak = Math.max(modernPeak, modernThisTick);
        }

        long legacyStarved = countZeroes(legacyOpportunities);
        long modernStarved = countZeroes(modernOpportunities);
        require(legacyStarved > 0, "Legacy item scheduler model must reproduce starvation");
        require(modernStarved == 0, "Modern item scheduler starved an eligible item");
        require(modernQueries == (long) ENTITY_COUNT * SCHEDULING_TICKS / 40,
                "Modern item scheduler did not preserve configured frequency");
        require(modernPeak <= divideCeil(ENTITY_COUNT, 40),
                "Modern item scheduler did not spread the cohort across phases");

        return new Scenario(
                "stationary_item_merging",
                "1,000 stationary same-cohort items over 400 ticks; legacy WYML gate versus the deterministic 40-tick scheduler.",
                List.of(
                        new Metric("merge_query_opportunities", legacyQueries, modernQueries, "queries"),
                        new Metric("peak_queries_in_one_tick", legacyPeak, modernPeak, "queries/tick"),
                        new Metric("entities_with_no_opportunity", legacyStarved, modernStarved, "entities")));
    }

    private static Scenario benchmarkEntityPushing()
    {
        int[] modernOpportunities = new int[ENTITY_COUNT];
        long legacyNeighbourQueries = 0;
        long modernNeighbourQueries = 0;
        int legacyPeak = 0;
        int modernPeak = 0;

        for (int tick = 1; tick <= SCHEDULING_TICKS; tick++)
        {
            int legacyThisTick = 0;
            int modernThisTick = 0;
            for (int entityId = 0; entityId < ENTITY_COUNT; entityId++)
            {
                // The 1.18-era doPush gate ran only after this query had already happened.
                legacyNeighbourQueries++;
                legacyThisTick++;
                if (DeterministicTickScheduler.shouldRun(tick, entityId, 4))
                {
                    modernNeighbourQueries++;
                    modernThisTick++;
                    modernOpportunities[entityId]++;
                }
            }
            legacyPeak = Math.max(legacyPeak, legacyThisTick);
            modernPeak = Math.max(modernPeak, modernThisTick);
        }

        require(countZeroes(modernOpportunities) == 0,
                "Modern push scheduler starved an ordinary living entity");
        require(modernNeighbourQueries * 4 == legacyNeighbourQueries,
                "Four-tick push scheduling did not reduce pre-query work by the expected amount");
        require(modernPeak <= divideCeil(ENTITY_COUNT, 4),
                "Modern push scheduler did not spread the cohort across phases");

        return new Scenario(
                "crowded_entity_pushing",
                "1,000 ordinary living entities over 400 ticks; legacy post-query gate versus the modern four-tick pre-query scheduler.",
                List.of(
                        new Metric("neighbourhood_queries", legacyNeighbourQueries, modernNeighbourQueries, "queries"),
                        new Metric("peak_queries_in_one_tick", legacyPeak, modernPeak, "queries/tick"),
                        new Metric("entities_with_no_opportunity", 0, countZeroes(modernOpportunities), "entities")));
    }

    private static Scenario benchmarkPathologicalSpawning()
    {
        long baselinePredicateCalls = (long) SPAWN_TICKS * CANDIDATES_PER_TICK;
        long modernPredicateCalls = 0;
        long blockedCandidates = 0;
        int observedFailures = 0;
        int probes = 0;
        SpawnControllerState controller = new SpawnControllerState();
        controller.throttle(0);

        for (int tick = 0; tick < SPAWN_TICKS; tick++)
        {
            for (int candidate = 0; candidate < CANDIDATES_PER_TICK; candidate++)
            {
                ControllerState before = controller.current(tick);
                if (!controller.tryAcquireAttempt(tick))
                {
                    blockedCandidates++;
                    continue;
                }

                if (before == ControllerState.PROBE) probes++;
                modernPredicateCalls++;
                observedFailures++;
                controller.recordOutcome(false, tick);
                if (observedFailures == 256)
                {
                    controller.backoff(tick, 1_800, 8, 10);
                }
            }
        }

        require(probes > 0, "Pathological controller benchmark never reached a recovery probe");
        require(blockedCandidates + modernPredicateCalls == baselinePredicateCalls,
                "Pathological controller benchmark lost candidate accounting");
        require(modernPredicateCalls * 20 < baselinePredicateCalls,
                "Pathological controller did not reduce expensive predicate calls by at least 95%");

        return new Scenario(
                "pathological_spawn_failures",
                "Twelve expensive failing candidates per tick for 20,000 ticks; all-off baseline versus bounded backoff with eight probes per 1,800 ticks.",
                List.of(
                        new Metric("expensive_predicate_calls", baselinePredicateCalls, modernPredicateCalls, "calls"),
                        new Metric("candidates_stopped_before_predicate", 0, blockedCandidates, "candidates"),
                        new Metric("bounded_probe_calls", 0, probes, "calls")));
    }

    private static Scenario benchmarkHealthySpawning()
    {
        long baselinePredicateCalls = (long) SPAWN_TICKS * CANDIDATES_PER_TICK;
        long modernPredicateCalls = 0;
        SpawnControllerState controller = new SpawnControllerState();

        for (int tick = 0; tick < SPAWN_TICKS; tick++)
        {
            for (int candidate = 0; candidate < CANDIDATES_PER_TICK; candidate++)
            {
                require(controller.tryAcquireAttempt(tick),
                        "Healthy active controller unexpectedly blocked a candidate");
                modernPredicateCalls++;
                controller.recordOutcome(true, tick);
            }
        }

        require(modernPredicateCalls == baselinePredicateCalls,
                "Healthy active controller changed predicate-call count");
        return new Scenario(
                "healthy_spawn_workload",
                "Twelve successful candidates per tick for 20,000 ticks with a controller that remains ACTIVE.",
                List.of(new Metric(
                        "predicate_calls", baselinePredicateCalls, modernPredicateCalls, "calls")));
    }

    private static String toJson(String label, String loader, List<Scenario> scenarios)
    {
        StringBuilder json = new StringBuilder();
        json.append("{\n")
                .append("  \"schema\": 1,\n")
                .append("  \"label\": \"").append(escape(label)).append("\",\n")
                .append("  \"loader\": \"").append(escape(loader)).append("\",\n")
                .append("  \"generated_at\": \"").append(Instant.now()).append("\",\n")
                .append("  \"java_version\": \"").append(escape(System.getProperty("java.version"))).append("\",\n")
                .append("  \"scenarios\": [\n");
        for (int scenarioIndex = 0; scenarioIndex < scenarios.size(); scenarioIndex++)
        {
            Scenario scenario = scenarios.get(scenarioIndex);
            json.append("    {\n")
                    .append("      \"id\": \"").append(escape(scenario.id())).append("\",\n")
                    .append("      \"description\": \"").append(escape(scenario.description())).append("\",\n")
                    .append("      \"metrics\": [\n");
            for (int metricIndex = 0; metricIndex < scenario.metrics().size(); metricIndex++)
            {
                Metric metric = scenario.metrics().get(metricIndex);
                json.append("        {\"name\": \"").append(escape(metric.name()))
                        .append("\", \"before\": ").append(metric.before())
                        .append(", \"after\": ").append(metric.after())
                        .append(", \"unit\": \"").append(escape(metric.unit())).append("\"}");
                if (metricIndex + 1 < scenario.metrics().size()) json.append(',');
                json.append('\n');
            }
            json.append("      ]\n    }");
            if (scenarioIndex + 1 < scenarios.size()) json.append(',');
            json.append('\n');
        }
        return json.append("  ]\n}\n").toString();
    }

    private static long countZeroes(int[] values)
    {
        long count = 0;
        for (int value : values)
        {
            if (value == 0) count++;
        }
        return count;
    }

    private static int divideCeil(int value, int divisor)
    {
        return (value + divisor - 1) / divisor;
    }

    private static String sanitizeLabel(String label)
    {
        String sanitized = label.replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.isBlank() ? "latest" : sanitized;
    }

    private static String escape(String value)
    {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message)
    {
        if (!condition) throw new IllegalStateException(message);
    }

    private record Scenario(String id, String description, List<Metric> metrics)
    {
        private Scenario
        {
            metrics = new ArrayList<>(metrics);
        }
    }

    private record Metric(String name, long before, long after, String unit)
    {
    }
}
