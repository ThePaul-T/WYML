package net.creeperhost.wyml;

import net.creeperhost.polylib.event.events.server.PolyServerLifecycleEvents;
import net.creeperhost.polylib.event.events.server.PolyServerTickEvents;
import net.creeperhost.wyml.config.WymlBootConfig;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.creeperhost.wyml.paperbag.PaperBagSpillPolicy;
import net.creeperhost.wyml.tiles.TilePaperBag;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Deferred, server-thread-only Paper Bag spill processing.
 *
 * <p>The loader add-entity hook only enqueues a successfully admitted item.
 * World scans, placement, and inventory mutation happen later at tick end and
 * are bounded by the configured candidate and collection budgets.</p>
 */
public final class BagHandler
{
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final int ENTITY_VISIBILITY_RETRIES = 2;
    private static final Map<MinecraftServer, LinkedHashMap<SpillKey, PendingSpill>> PENDING =
            new IdentityHashMap<>();

    private BagHandler()
    {
    }

    public static void create()
    {
        if (!REGISTERED.compareAndSet(false, true)) return;
        PolyServerTickEvents.TICK_END.register(BagHandler::tickEnd);
        PolyServerLifecycleEvents.SERVER_STOPPING.register(BagHandler::serverStopping);
    }

    /** Called only after the item was successfully added to a ServerLevel. */
    public static void itemEntityAdded(ItemEntity itemEntity)
    {
        if (!(itemEntity.level() instanceof ServerLevel level)
                || !itemEntity.isAlive()
                || itemEntity.getItem().isEmpty())
        {
            return;
        }

        SpillKey key = new SpillKey(level.dimension(), itemEntity.chunkPosition().pack());
        synchronized (PENDING)
        {
            LinkedHashMap<SpillKey, PendingSpill> queue =
                    PENDING.computeIfAbsent(level.getServer(), ignored -> new LinkedHashMap<>());
            queue.putIfAbsent(key,
                    new PendingSpill(itemEntity.blockPosition().immutable(), ENTITY_VISIBILITY_RETRIES));
        }
    }

    private static void tickEnd(MinecraftServer server)
    {
        if (!WymlConfig.isEnabled()
                || !WymlBootConfig.moduleEnabled("paper_bags")
                || !WymlConfig.cached().ALLOW_PAPER_BAGS)
        {
            clear(server);
            return;
        }

        int budget = PaperBagSpillPolicy.positiveBudget(WymlConfig.cached().PAPER_BAG_CANDIDATES_PER_TICK);
        List<Map.Entry<SpillKey, PendingSpill>> work = take(server, budget);
        for (Map.Entry<SpillKey, PendingSpill> candidate : work)
        {
            process(server, candidate.getKey(), candidate.getValue());
        }
    }

    private static List<Map.Entry<SpillKey, PendingSpill>> take(MinecraftServer server, int budget)
    {
        List<Map.Entry<SpillKey, PendingSpill>> result = new ArrayList<>(budget);
        synchronized (PENDING)
        {
            LinkedHashMap<SpillKey, PendingSpill> queue = PENDING.get(server);
            if (queue == null) return result;
            var iterator = queue.entrySet().iterator();
            while (iterator.hasNext() && result.size() < budget)
            {
                Map.Entry<SpillKey, PendingSpill> entry = iterator.next();
                result.add(Map.entry(entry.getKey(), entry.getValue()));
                iterator.remove();
            }
            if (queue.isEmpty()) PENDING.remove(server);
        }
        return result;
    }

    private static void process(MinecraftServer server, SpillKey key, PendingSpill pending)
    {
        BlockPos anchor = pending.anchor();
        ServerLevel level = server.getLevel(key.dimension());
        if (level == null || !level.hasChunkAt(anchor)) return;

        int radius = PaperBagSpillPolicy.radius(WymlConfig.cached().PAPER_BAG_SCAN_RADIUS);
        AABB searchArea = new AABB(anchor).inflate(radius);
        List<ItemEntity> eligible = level.getEntitiesOfClass(
                ItemEntity.class,
                searchArea,
                BagHandler::isEligible);
        if (eligible.isEmpty())
        {
            if (pending.emptyVisibilityRetries() > 0)
            {
                enqueue(server, key,
                        new PendingSpill(anchor, pending.emptyVisibilityRetries() - 1));
            }
            return;
        }

        TilePaperBag existing = findExistingBag(level, searchArea);
        if (existing != null)
        {
            collectAndRequeue(existing, eligible, level, key, anchor);
            return;
        }

        int oldestAge = eligible.stream().mapToInt(ItemEntity::getAge).max().orElse(0);
        if (!PaperBagSpillPolicy.qualifies(
                eligible.size(),
                oldestAge,
                WymlConfig.cached().MIN_ITEM_COUNT,
                WymlConfig.cached().MIN_ITEM_AGE))
        {
            if (PaperBagSpillPolicy.shouldRetryAwaitingAge(
                    eligible.size(),
                    oldestAge,
                    WymlConfig.cached().MIN_ITEM_COUNT,
                    WymlConfig.cached().MIN_ITEM_AGE))
            {
                enqueue(server, key, pending);
            }
            return;
        }

        BlockPos placement = findSafePlacement(level, anchor, radius);
        if (placement == null) return;
        if (!level.setBlock(placement, WYMLBlocks.PAPER_BAG.get().defaultBlockState(), 3)) return;

        if (!(level.getBlockEntity(placement) instanceof TilePaperBag paperBag))
        {
            // Placement produced no usable inventory. No item was touched.
            level.removeBlock(placement, false);
            WhyYouMakeLag.LOGGER.error("Paper Bag placement at {} produced no Paper Bag block entity", placement);
            return;
        }

        collectAndRequeue(paperBag, eligible, level, key, placement);
    }

    private static void collectAndRequeue(
            TilePaperBag paperBag,
            List<ItemEntity> eligible,
            ServerLevel level,
            SpillKey key,
            BlockPos anchor)
    {
        int collectionBudget = PaperBagSpillPolicy.positiveBudget(
                WymlConfig.cached().PAPER_BAG_COLLECTION_BUDGET);
        TilePaperBag.CollectionResult result = paperBag.collectItems(eligible, collectionBudget);
        if (result.remaining() || result.visited() < eligible.size())
        {
            enqueue(level.getServer(), key, new PendingSpill(anchor, ENTITY_VISIBILITY_RETRIES));
        }
    }

    private static boolean isEligible(ItemEntity itemEntity)
    {
        return itemEntity.isAlive()
                && !itemEntity.isRemoved()
                && !itemEntity.getItem().isEmpty()
                && WYMLReimplementedHooks.isValidPickup(itemEntity.getItem(), itemEntity.level());
    }

    private static TilePaperBag findExistingBag(ServerLevel level, AABB searchArea)
    {
        int minX = (int) Math.floor(searchArea.minX);
        int minY = (int) Math.floor(searchArea.minY);
        int minZ = (int) Math.floor(searchArea.minZ);
        int maxX = (int) Math.ceil(searchArea.maxX);
        int maxY = (int) Math.ceil(searchArea.maxY);
        int maxZ = (int) Math.ceil(searchArea.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ))
        {
            if (level.getBlockEntity(pos) instanceof TilePaperBag paperBag)
            {
                return paperBag;
            }
        }
        return null;
    }

    private static BlockPos findSafePlacement(ServerLevel level, BlockPos anchor, int radius)
    {
        for (int distance = 0; distance <= radius; distance++)
        {
            for (int yOffset = 0; yOffset <= 1; yOffset++)
            {
                for (int xOffset = -distance; xOffset <= distance; xOffset++)
                {
                    for (int zOffset = -distance; zOffset <= distance; zOffset++)
                    {
                        if (distance > 0 && Math.max(Math.abs(xOffset), Math.abs(zOffset)) != distance) continue;
                        BlockPos pos = anchor.offset(xOffset, yOffset, zOffset);
                        if (level.hasChunkAt(pos)
                                && level.getBlockState(pos).isAir()
                                && level.getBlockEntity(pos) == null)
                        {
                            return pos.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void enqueue(MinecraftServer server, SpillKey key, PendingSpill pending)
    {
        synchronized (PENDING)
        {
            PENDING.computeIfAbsent(server, ignored -> new LinkedHashMap<>())
                    .putIfAbsent(key, pending);
        }
    }

    private static void serverStopping(MinecraftServer server)
    {
        clear(server);
    }

    private static void clear(MinecraftServer server)
    {
        synchronized (PENDING)
        {
            PENDING.remove(server);
        }
    }

    private record SpillKey(ResourceKey<Level> dimension, long chunk)
    {
    }

    private record PendingSpill(BlockPos anchor, int emptyVisibilityRetries)
    {
    }
}
