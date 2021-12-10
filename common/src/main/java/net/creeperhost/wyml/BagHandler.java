package net.creeperhost.wyml;

import net.creeperhost.wyml.tiles.TilePaperBag;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BagHandler
{
    public static HashMap<Long, ItemEntity> MAP = new HashMap<>();
    public static List<Long> LIST_TO_REMOVE = new ArrayList<>();
    public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    public static boolean updating = false;
    public static int MIN_AGE = WymlConfig.cached().MIN_ITEM_AGE;
    public static int MIN_ITEMS = WymlConfig.cached().MIN_ITEM_COUNT;

    public static void create()
    {
        if (MAP == null) MAP = new HashMap<>();
        if (scheduledExecutorService.isShutdown()) scheduledExecutorService = Executors.newScheduledThreadPool(1);

        Runnable runnable = BagHandler::clean;

        scheduledExecutorService.scheduleAtFixedRate(runnable, 0, 10, TimeUnit.SECONDS);
    }

    public static void itemEntityAdded(ItemEntity itemEntity)
    {
        if (itemEntity == null) return;
        if (itemEntity.level.isClientSide) return;
        ChunkPos chunk = itemEntity.level.getChunkAt(itemEntity.blockPosition()).getPos();
        if (chunk == null) return;
        if (MAP.containsKey(chunk.toLong()))
        {
            if (!updating) update(chunk.toLong());
            return;
        }

        if (!MAP.containsKey(chunk.toLong()) && getOtherItemsEntities(itemEntity).size() > MIN_ITEMS && itemEntity.isAlive() && !itemEntity.getItem().isEmpty())
        {
            MAP.put(chunk.toLong(), itemEntity);
            WhyYouMakeLag.LOGGER.info("added " + itemEntity.getItem().getDisplayName().getString() + " To MAP " + chunk.toString());
            if (!updating) update(chunk.toLong());
        }
    }

    public static void clean()
    {
        if (LIST_TO_REMOVE.isEmpty()) return;

        WhyYouMakeLag.LOGGER.info("Cleaned up caches for BagHandler");

        for (Long aLong : LIST_TO_REMOVE)
        {
            MAP.remove(aLong);
        }
    }

    public static void update(long chunkLong)
    {
        updating = true;
        try
        {
            if (MAP.isEmpty()) return;

            ItemEntity itemEntity = MAP.get(chunkLong);

            if (itemEntity == null)
            {
                updating = false;
                MAP.remove(chunkLong);
                return;
            }
            if (!itemEntity.isAlive())
            {
                updating = false;
                return;
            }

            WhyYouMakeLag.LOGGER.info("Running update for BagHandler " + itemEntity.getItem().getDisplayName().getString() + " Size " + getOtherItemsEntities(itemEntity).size());

            if (shouldSpawnBag(itemEntity))
            {
                WhyYouMakeLag.LOGGER.info("More than " + MIN_ITEMS + " entities in chunk " + chunkLong);
                createBag(itemEntity);
            }
            LIST_TO_REMOVE.add(chunkLong);
        } catch (Exception e)
        {
            updating = false;
            e.printStackTrace();
        }
        updating = false;
    }

    public static boolean shouldSpawnBag(ItemEntity itemEntity)
    {
        List<ItemEntity> itemEntityList = getOtherItemsEntities(itemEntity);
        int maxAge = 0;
        if (itemEntityList.size() > MIN_ITEMS)
        {
            for (ItemEntity entity : itemEntityList)
            {
                if (entity.getAge() > maxAge) maxAge = entity.getAge();
            }
            return maxAge > MIN_AGE;
        }
        return false;
    }

    public static boolean createBag(ItemEntity itemEntity)
    {
        ServerLevel serverLevel = (ServerLevel) itemEntity.level;
        BlockPos paperBagPos = itemEntity.blockPosition();

        BlockPos first = getOtherItemsEntities(itemEntity).get(0).blockPosition();
        paperBagPos = first;

        if (paperBagPos == null)
        {
            WhyYouMakeLag.LOGGER.error("Unable to find location to spawn bag");
            return false;
        }

        if (serverLevel.getBlockEntity(paperBagPos) == null || serverLevel.getBlockEntity(paperBagPos) != null)
        {
            if (!(serverLevel.getBlockEntity(paperBagPos) instanceof TilePaperBag))
                serverLevel.removeBlockEntity(paperBagPos);
            if (serverLevel.getBlockState(paperBagPos) != WYMLBlocks.PAPER_BAG.get().defaultBlockState())
            {
                serverLevel.setBlock(paperBagPos, WYMLBlocks.PAPER_BAG.get().defaultBlockState(), 3);
                TilePaperBag tilePaperBag = (TilePaperBag) serverLevel.getBlockEntity(paperBagPos);
                if (tilePaperBag != null) tilePaperBag.collectItems();
            }
        }
        return true;
    }

    public static List<ItemEntity> getOtherItemsEntities(ItemEntity itemEntity)
    {
        return itemEntity.level.getEntitiesOfClass(ItemEntity.class, itemEntity.getBoundingBox().inflate(4.0D, 4.0D, 4.0D));
    }
}
