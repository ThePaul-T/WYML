package net.creeperhost.wyml.tiles;

import net.creeperhost.polylib.blocks.PolyBlockEntity;
import net.creeperhost.polylib.data.serializable.IntData;
import net.creeperhost.polylib.data.serializable.LongData;
import net.creeperhost.polylib.inventory.items.BlockInventory;
import net.creeperhost.polylib.inventory.items.PolyInventoryBlock;
import net.creeperhost.wyml.WYMLReimplementedHooks;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.containers.ContainerPaperBag;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public class TilePaperBag extends PolyBlockEntity implements PolyInventoryBlock, MenuProvider
{
    private final BlockInventory inventory = new BlockInventory(this, 180);
    private final int despawnDuration = WymlConfig.cached().PAPER_BAG_DESPAWN_TIME;
    private final LongData despawnTime;
    private final IntData usedSlots;

    public TilePaperBag(BlockPos pos, BlockState state)
    {
        super(WYMLBlocks.PAPER_BAG_TILE.get(), pos, state);
        despawnTime = register("despawn", new LongData(Instant.now().getEpochSecond() + despawnDuration), SAVE, SYNC);
        usedSlots = register("used_slots", new IntData(0), SYNC);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("container." + WhyYouMakeLag.MOD_ID + ".paper_bag");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player)
    {
        return new ContainerPaperBag(id, playerInventory, this);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (level == null || level.isClientSide())
        {
            return;
        }

        updateUsedCount();
        if (Instant.now().getEpochSecond() >= getDespawnTime())
        {
            WhyYouMakeLag.LOGGER.info("Removing Paper Bag from {} because it expired", getBlockPos());
            remove();
        }
        else if (inventory.isEmpty())
        {
            WhyYouMakeLag.LOGGER.info("Removing empty Paper Bag from {}", getBlockPos());
            remove();
        }
    }

    @Override
    public void writeExtraData(ValueOutput output)
    {
        inventory.serialize(output);
    }

    @Override
    public void readExtraData(ValueInput input)
    {
        inventory.deserialize(input);
    }

    @Override
    public @Nullable Container getContainer(@Nullable Direction side)
    {
        return null;
    }

    public BlockInventory getInventory()
    {
        return inventory;
    }

    public void remove()
    {
        if (level == null)
        {
            return;
        }
        inventory.clearContent();
        level.removeBlock(getBlockPos(), false);
    }

    public long getDespawnTime()
    {
        return despawnTime.get();
    }

    public void resetDespawnTime()
    {
        despawnTime.set(Instant.now().getEpochSecond() + despawnDuration);
    }

    private void updateUsedCount()
    {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        {
            if (!inventory.getItem(slot).isEmpty())
            {
                count++;
            }
        }
        usedSlots.set(count);
    }

    public int getUsedSlots()
    {
        return usedSlots.get();
    }

    public void collectItems()
    {
        if (level == null || level.isClientSide())
        {
            return;
        }

        AABB searchArea = new AABB(getBlockPos()).inflate(4.0D);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, searchArea))
        {
            ItemStack stack = itemEntity.getItem();
            if (!itemEntity.isAlive() || !WYMLReimplementedHooks.isValidPickup(stack, level))
            {
                continue;
            }

            int remaining = inventory.insertStack(stack, false);
            if (remaining == 0)
            {
                itemEntity.discard();
            }
            else
            {
                stack.setCount(remaining);
                itemEntity.setItem(stack);
            }
        }
    }
}
