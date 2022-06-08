package net.creeperhost.wyml.tiles;

import net.creeperhost.wyml.WYMLReimplementedHooks;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.containers.ContainerPaperBag;
import net.creeperhost.wyml.containers.InventoryPaperBag;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.creeperhost.wyml.network.MessageUpdatePaperbag;
import net.creeperhost.wyml.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class TilePaperBag extends BaseContainerBlockEntity implements WorldlyContainer
{
    private final InventoryPaperBag inventory;
    private long DESPAWN_TIME_STAMP;
    private final int DESPAWN_TIME = WymlConfig.cached().PAPER_BAG_DESPAWN_TIME;
    private int USED_COUNT;

    public TilePaperBag(BlockPos blockPos, BlockState blockState)
    {
        super(WYMLBlocks.PAPER_BAG_TILE.get(), blockPos, blockState);
        this.inventory = new InventoryPaperBag(180);
        DESPAWN_TIME_STAMP = (Instant.now().getEpochSecond() + DESPAWN_TIME);
        USED_COUNT = getUsedSlots();
    }

    @Override
    public Component getDefaultName()
    {
        return Component.literal("container." + WhyYouMakeLag.MOD_ID + ".paper_bag");
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory)
    {
        return new ContainerPaperBag(i, inventory, this);
    }

    @Override
    public int getContainerSize()
    {
        return inventory.getContainerSize();
    }

    @Override
    public boolean isEmpty()
    {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getItem(int i)
    {
        return inventory.getItem(i);
    }

    @Override
    public ItemStack removeItem(int i, int j)
    {
        return inventory.removeItem(i, j);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i)
    {
        return inventory.removeItemNoUpdate(i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack)
    {
        inventory.setItem(i, itemStack);
    }

    @Override
    public boolean stillValid(Player player)
    {
        return inventory.stillValid(player);
    }

    @Override
    public void clearContent()
    {
        inventory.clearContent();
    }

    @Override
    public void load(CompoundTag compoundTag)
    {
        super.load(compoundTag);
        inventory.deserializeNBT(compoundTag);
        DESPAWN_TIME_STAMP = compoundTag.getLong("despawn");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag)
    {
        super.saveAdditional(compoundTag);
        compoundTag.merge(inventory.serializeNBT());
        compoundTag.putLong("despawn", DESPAWN_TIME_STAMP);
    }

    public InventoryPaperBag getInventory()
    {
        return inventory;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity)
    {
        if (!level.isClientSide())
        {
            TilePaperBag tilePaperBag = (TilePaperBag) level.getBlockEntity(blockPos);
            tilePaperBag.updateUsedCount();

            if (Instant.now().getEpochSecond() >= tilePaperBag.getDespawnTime())
            {
                WhyYouMakeLag.LOGGER.info("Removing PaperBag from location " + tilePaperBag.getBlockPos() + " Reason: Age");
                tilePaperBag.remove();
            }
            if (tilePaperBag.inventory.isEmpty())
            {
                WhyYouMakeLag.LOGGER.info("Removing PaperBag from location " + tilePaperBag.getBlockPos() + " Reason: Empty");
                tilePaperBag.remove();
            }
        }
    }

    public void remove()
    {
        inventory.clearContent();
        if (level.getBlockEntity(getBlockPos()) != null) level.removeBlockEntity(getBlockPos());
        level.removeBlock(getBlockPos(), false);
    }

    public long getDespawnTime()
    {
        return DESPAWN_TIME_STAMP;
    }

    public void setDespawnTime(long value)
    {
        this.DESPAWN_TIME_STAMP = value;
    }

    public void resetDespawnTime(ServerPlayer player)
    {
        DESPAWN_TIME_STAMP = (Instant.now().getEpochSecond() + DESPAWN_TIME);
        PacketHandler.HANDLER.sendToPlayer(player, new MessageUpdatePaperbag(getBlockPos(), getUsedSlots(), DESPAWN_TIME_STAMP));
    }

    public void updateUsedCount()
    {
        this.USED_COUNT = (int) inventory.getItems().stream().filter(itemStack -> !itemStack.isEmpty()).count();
    }

    public int getUsedSlots()
    {
        return USED_COUNT;
    }

    public void setUsedCount(int value)
    {
        this.USED_COUNT = value;
    }

    public void collectItems()
    {
        if (level.isClientSide()) return;
        //4.0D == 4 Blocks around the paper bag
        AABB searchArea = new AABB(getBlockPos()).inflate(4.0D, 4.0D, 4.0D);
        if (!level.getEntitiesOfClass(ItemEntity.class, searchArea).isEmpty())
        {
            for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, searchArea))
            {
                ItemStack itemStack = itemEntity.getItem();
                if (itemEntity.isAlive() && WYMLReimplementedHooks.isValidPickup(itemStack, level))
                {
                    ItemStack inserted = inventory.addItem(itemStack);
                    if (inserted.isEmpty())
                    {
                        itemEntity.kill();
                    }
                    else
                    {
                        itemEntity.setItem(inserted);
                    }
                }
            }
        }
    }

    //IInventory Stop interactions via pipes/hoppers etc
    @Override
    public int[] getSlotsForFace(Direction direction)
    {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction)
    {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction)
    {
        return false;
    }
}
