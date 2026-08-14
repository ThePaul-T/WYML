package net.creeperhost.wyml.benchmark;

import net.creeperhost.polylib.event.events.server.PolyServerLifecycleEvents;
import net.creeperhost.polylib.event.events.server.PolyServerTickEvents;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.tiles.TilePaperBag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/** Local-only, system-property activated Minecraft tick-time benchmark. */
public final class RuntimeBenchmarkController
{
    private static final String PREFIX = "wyml.runtimeBenchmark.";
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final List<Entity> DENSE_ENTITIES = new ArrayList<>();
    private static final List<Long> DURATIONS = new ArrayList<>();
    private static final Map<Entity, Integer> MEASUREMENT_START_TICKS = new IdentityHashMap<>();
    private static final Map<Entity, Vec3> WORKLOAD_POSITIONS = new IdentityHashMap<>();

    private static Config config;
    private static int ticks;
    private static long tickStarted;
    private static double workloadX;
    private static double workloadY;
    private static double workloadZ;

    private RuntimeBenchmarkController()
    {
    }

    public static void registerIfRequested()
    {
        if (!Boolean.getBoolean(PREFIX + "enabled") || !REGISTERED.compareAndSet(false, true)) return;
        config = Config.read();
        PolyServerLifecycleEvents.SERVER_STARTED.register(RuntimeBenchmarkController::serverStarted);
        PolyServerTickEvents.TICK_START.register(RuntimeBenchmarkController::tickStart);
        PolyServerTickEvents.TICK_END.register(RuntimeBenchmarkController::tickEnd);
        WhyYouMakeLag.LOGGER.warn("WYML local runtime benchmark enabled: loader={}, profile={}, repetition={}, seed={}",
                config.loader(), config.profile(), config.repetition(), config.seed());
    }

    private static void serverStarted(MinecraftServer server)
    {
        ServerLevel level = server.overworld();
        BlockPos spawn = server.getRespawnData().pos();
        workloadX = spawn.getX() + 0.5D;
        workloadY = spawn.getY() + 2.0D;
        workloadZ = spawn.getZ() + 0.5D;
        int chunkX = spawn.getX() >> 4;
        int chunkZ = spawn.getZ() >> 4;
        level.setChunkForced(chunkX, chunkZ, true);
        level.getChunk(chunkX, chunkZ);
        level.getGameRules().set(GameRules.MAX_ENTITY_CRAMMING, 0, server);

        for (int index = 0; index < config.cows(); index++)
        {
            Mob cow = (Mob) EntityType.COW.create(level, EntitySpawnReason.COMMAND);
            if (cow == null) throw new IllegalStateException("Could not create benchmark cow " + index);
            cow.setNoAi(true);
            cow.setPersistenceRequired();
            cow.setPos(workloadX, workloadY, workloadZ);
            if (!level.addFreshEntity(cow)) throw new IllegalStateException("Could not add benchmark cow " + index);
            DENSE_ENTITIES.add(cow);
            WORKLOAD_POSITIONS.put(cow, new Vec3(workloadX, workloadY, workloadZ));
        }

        int itemGridWidth = (int) Math.ceil(Math.sqrt(config.items()));
        for (int index = 0; index < config.items(); index++)
        {
            double itemX = config.spreadItems() ? workloadX + (index % itemGridWidth) * 2.0D : workloadX;
            double itemY = workloadY + 0.25D;
            double itemZ = config.spreadItems() ? workloadZ + (index / itemGridWidth) * 2.0D : workloadZ;
            if (config.spreadItems())
            {
                level.setChunkForced(((int) Math.floor(itemX)) >> 4, ((int) Math.floor(itemZ)) >> 4, true);
            }
            ItemEntity item = new ItemEntity(level, itemX, itemY, itemZ, new ItemStack(Items.STONE));
            item.setTarget(new UUID(0x57594D4C00000000L, index + 1L));
            item.setNoGravity(config.spreadItems());
            if (!config.workload().equals("item_lifetime_expiry")
                    && !config.workload().equals("paper_bag_spill"))
            {
                item.setUnlimitedLifetime();
            }
            if (!level.addFreshEntity(item)) throw new IllegalStateException("Could not add benchmark item " + index);
            DENSE_ENTITIES.add(item);
            WORKLOAD_POSITIONS.put(item, new Vec3(itemX, itemY, itemZ));
        }

        WhyYouMakeLag.LOGGER.warn(
                "WYML benchmark workload ready: {} cows and {} non-merging items; warming up for {} ticks then measuring {} ticks.",
                config.cows(), config.items(), config.warmupTicks(), config.measureTicks());
    }

    private static void tickStart(MinecraftServer server)
    {
        // Keep every run's dense workload equivalent. This reset is deliberately
        // outside the timed section and occurs identically in on/off profiles.
        for (Entity entity : DENSE_ENTITIES)
        {
            Vec3 position = WORKLOAD_POSITIONS.get(entity);
            if (!entity.isRemoved() && position != null) entity.setPos(position);
        }
        tickStarted = System.nanoTime();
    }

    private static void tickEnd(MinecraftServer server)
    {
        long duration = System.nanoTime() - tickStarted;
        ticks++;
        if (ticks == config.warmupTicks())
        {
            for (Entity entity : DENSE_ENTITIES) MEASUREMENT_START_TICKS.put(entity, entity.tickCount);
        }
        if (ticks > config.warmupTicks()) DURATIONS.add(duration);
        if (DURATIONS.size() < config.measureTicks()) return;

        try
        {
            writeResult();
        }
        catch (IOException exception)
        {
            WhyYouMakeLag.LOGGER.error("Could not write WYML runtime benchmark result", exception);
            throw new IllegalStateException("Could not write runtime benchmark result", exception);
        }
        finally
        {
            server.halt(false);
        }
    }

    private static void writeResult() throws IOException
    {
        int liveCows = 0;
        int liveItems = 0;
        int minimumMeasuredEntityTicks = Integer.MAX_VALUE;
        for (Entity entity : DENSE_ENTITIES)
        {
            if (entity.isRemoved()) continue;
            if (entity instanceof ItemEntity) liveItems++;
            else if (entity.getType() == EntityType.COW) liveCows++;
            Integer startedAt = MEASUREMENT_START_TICKS.get(entity);
            if (startedAt == null) throw new IllegalStateException("Missing measurement baseline for benchmark entity");
            minimumMeasuredEntityTicks = Math.min(minimumMeasuredEntityTicks, entity.tickCount - startedAt);
        }
        if (liveCows != config.cows() || liveItems != config.expectedLiveItems())
        {
            throw new IllegalStateException("Benchmark workload changed during measurement: expected "
                    + config.cows() + " cows/" + config.expectedLiveItems() + " live items, found "
                    + liveCows + " cows/" + liveItems + " items");
        }
        PaperBagStats paperBags = paperBagStats();
        if (paperBags.bags() != config.expectedPaperBags()
                || paperBags.items() != config.expectedPaperBagItems())
        {
            throw new IllegalStateException("Benchmark Paper Bag state changed: expected "
                    + config.expectedPaperBags() + " bag(s)/" + config.expectedPaperBagItems() + " item(s), found "
                    + paperBags.bags() + " bag(s)/" + paperBags.items() + " item(s)");
        }
        if (minimumMeasuredEntityTicks < config.measureTicks() - 2)
        {
            throw new IllegalStateException("Benchmark entities were not continuously ticking during measurement: minimum="
                    + minimumMeasuredEntityTicks + ", expected approximately " + config.measureTicks());
        }

        long[] sorted = DURATIONS.stream().mapToLong(Long::longValue).sorted().toArray();
        long sum = 0L;
        long over50ms = 0L;
        for (long duration : sorted)
        {
            sum += duration;
            if (duration > 50_000_000L) over50ms++;
        }
        double mean = (double) sum / sorted.length;
        double variance = 0.0D;
        for (long duration : sorted)
        {
            double delta = duration - mean;
            variance += delta * delta;
        }
        variance /= sorted.length;

        String json = "{\n"
                + "  \"schema\": 2,\n"
                + "  \"generated_at\": \"" + Instant.now() + "\",\n"
                + "  \"loader\": \"" + escape(config.loader()) + "\",\n"
                + "  \"profile\": \"" + escape(config.profile()) + "\",\n"
                + "  \"repetition\": " + config.repetition() + ",\n"
                + "  \"wyml_enabled\": " + config.wymlEnabled() + ",\n"
                + "  \"seed\": " + config.seed() + ",\n"
                + "  \"world_directory\": \"" + escape(config.worldDirectory()) + "\",\n"
                + "  \"workload\": {\"id\": \"" + escape(config.workload()) + "\", \"cows\": " + config.cows()
                + ", \"items\": " + config.items() + ", \"expected_live_items\": " + config.expectedLiveItems()
                 + ", \"live_cows_at_end\": " + liveCows + ", \"live_items_at_end\": " + liveItems
                + ", \"paper_bags_at_end\": " + paperBags.bags() + ", \"paper_bag_items_at_end\": " + paperBags.items()
                + ", \"minimum_measured_entity_ticks\": " + minimumMeasuredEntityTicks + "},\n"
                + "  \"warmup_ticks\": " + config.warmupTicks() + ",\n"
                + "  \"measured_ticks\": " + sorted.length + ",\n"
                + "  \"tick_time_ms\": {\n"
                + "    \"mean\": " + millis(mean) + ",\n"
                + "    \"median\": " + millis(percentile(sorted, 0.50D)) + ",\n"
                + "    \"p95\": " + millis(percentile(sorted, 0.95D)) + ",\n"
                + "    \"p99\": " + millis(percentile(sorted, 0.99D)) + ",\n"
                + "    \"min\": " + millis(sorted[0]) + ",\n"
                + "    \"max\": " + millis(sorted[sorted.length - 1]) + ",\n"
                + "    \"standard_deviation\": " + millis(Math.sqrt(variance)) + ",\n"
                + "    \"over_50ms_ticks\": " + over50ms + "\n"
                + "  }\n"
                + "}\n";

        Path output = config.output();
        Files.createDirectories(output.getParent());
        Path temporary = output.resolveSibling(output.getFileName() + ".tmp");
        Files.writeString(temporary, json, StandardCharsets.UTF_8);
        try
        {
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException ignored)
        {
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
        }
        WhyYouMakeLag.LOGGER.warn("WYML runtime benchmark complete: mean={} ms, p95={} ms, result={}",
                millis(mean), millis(percentile(sorted, 0.95D)), output.toAbsolutePath());
    }

    private static long percentile(long[] sorted, double quantile)
    {
        int index = (int) Math.ceil(quantile * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(sorted.length - 1, index))];
    }

    private static PaperBagStats paperBagStats()
    {
        ServerLevel level = DENSE_ENTITIES.stream()
                .filter(entity -> entity.level() instanceof ServerLevel)
                .map(entity -> (ServerLevel) entity.level())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Benchmark level is unavailable"));
        BlockPos center = BlockPos.containing(workloadX, workloadY, workloadZ);
        int bags = 0;
        int items = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -2, -8), center.offset(8, 2, 8)))
        {
            if (level.getBlockEntity(pos) instanceof TilePaperBag paperBag)
            {
                bags++;
                for (int slot = 0; slot < paperBag.getInventory().getContainerSize(); slot++)
                {
                    items += paperBag.getInventory().getItem(slot).getCount();
                }
            }
        }
        return new PaperBagStats(bags, items);
    }

    private static String millis(double nanos)
    {
        return String.format(Locale.ROOT, "%.6f", nanos / 1_000_000.0D);
    }

    private static String escape(String value)
    {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record Config(
            String loader,
            String profile,
            int repetition,
            String workload,
            boolean wymlEnabled,
            long seed,
            int warmupTicks,
            int measureTicks,
            int cows,
            int items,
            int expectedLiveItems,
            int expectedPaperBags,
            int expectedPaperBagItems,
            boolean spreadItems,
            Path output,
            String worldDirectory)
    {
        private static Config read()
        {
            return new Config(
                    required("loader"),
                    required("profile"),
                    positive("repetition", 1),
                    System.getProperty(PREFIX + "workload", "dense"),
                    Boolean.parseBoolean(required("wymlEnabled")),
                    Long.parseLong(required("seed")),
                    positive("warmupTicks", 200),
                    positive("measureTicks", 600),
                    positive("cows", 300),
                    positive("items", 400),
                    nonNegative("expectedLiveItems", positive("items", 400)),
                    nonNegative("expectedPaperBags", 0),
                    nonNegative("expectedPaperBagItems", 0),
                    Boolean.parseBoolean(System.getProperty(PREFIX + "spreadItems", "false")),
                    Path.of(required("output")),
                    Path.of(required("worldDirectory")).toAbsolutePath().normalize().toString());
        }

        private static int positive(String name, int fallback)
        {
            int value = Integer.parseInt(System.getProperty(PREFIX + name, Integer.toString(fallback)));
            if (value < 1) throw new IllegalArgumentException(PREFIX + name + " must be positive");
            return value;
        }

        private static int nonNegative(String name, int fallback)
        {
            int value = Integer.parseInt(System.getProperty(PREFIX + name, Integer.toString(fallback)));
            if (value < 0) throw new IllegalArgumentException(PREFIX + name + " must not be negative");
            return value;
        }

        private static String required(String name)
        {
            String value = System.getProperty(PREFIX + name);
            if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing -D" + PREFIX + name);
            return value;
        }
    }

    private record PaperBagStats(int bags, int items)
    {
    }
}
