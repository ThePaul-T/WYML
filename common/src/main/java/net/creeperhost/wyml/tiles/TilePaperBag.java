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
import net.creeperhost.wyml.paperbag.PaperBagExpiryPolicy;
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
import org.jspecify.annotations.Nullable;

import java.util.List;

public class TilePaperBag extends PolyBlockEntity implements PolyInventoryBlock, MenuProvider
{
    private final BlockInventory inventory = new BlockInventory(this, 180);
    private final long despawnDurationTicks = Math.max(1L, WymlConfig.cached().PAPER_BAG_DESPAWN_TIME) * 20L;
    private final LongData despawnTime;
    private final IntData usedSlots;
    private boolean deadlineChecked;

    public TilePaperBag(BlockPos pos, BlockState state)
    {
        super(WYMLBlocks.PAPER_BAG_TILE.get(), pos, state);
        despawnTime = register("despawn", new LongData(0), SAVE, SYNC);
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

        ensureGameTimeDeadline();
        updateUsedCount();
        if (inventory.isEmpty())
        {
            WhyYouMakeLag.LOGGER.info("Removing empty Paper Bag from {}", getBlockPos());
            remove();
        }
        else if (level.getGameTime() >= getDespawnTime())
        {
            PaperBagExpiryPolicy policy = PaperBagExpiryPolicy.parse(WymlConfig.cached().PAPER_BAG_EXPIRY_POLICY);
            if (policy == PaperBagExpiryPolicy.PERSIST_WHILE_NON_EMPTY)
            {
                WhyYouMakeLag.LOGGER.info(
                        "Paper Bag at {} reached its expiry while non-empty; preserving it for another {} seconds",
                        getBlockPos(), Math.max(1, WymlConfig.cached().PAPER_BAG_DESPAWN_TIME));
                resetDespawnTime();
            }
            else
            {
                WhyYouMakeLag.LOGGER.warn(
                        "Paper Bag at {} expired under legacy_void_with_warning; voiding {} item(s) in {} occupied slot(s) "
                                + "rather than recreating the original spill",
                        getBlockPos(), getStoredItemCount(), getUsedSlots());
                remove();
            }
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
        if (level == null)
        {
            despawnTime.set(0L);
            deadlineChecked = false;
            return;
        }
        despawnTime.set(level.getGameTime() + despawnDurationTicks);
        deadlineChecked = true;
    }

    public long getRemainingSeconds()
    {
        if (level == null) return Math.max(0, despawnDurationTicks / 20L);
        return Math.max(0, getDespawnTime() - level.getGameTime()) / 20L;
    }

    private void ensureGameTimeDeadline()
    {
        if (deadlineChecked || level == null) return;
        long deadline = despawnTime.get();
        long current = level.getGameTime();
        // Old saves stored an epoch-second value under this key. A deadline
        // implausibly far beyond the configured tick duration is migrated.
        if (deadline <= 0 || deadline > current + despawnDurationTicks * 4L)
        {
            despawnTime.set(current + despawnDurationTicks);
        }
        deadlineChecked = true;
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

    public CollectionResult collectItems(List<ItemEntity> itemEntities, int budget)
    {
        if (level == null || level.isClientSide())
        {
            return new CollectionResult(0, false);
        }

        int visited = 0;
        boolean remaining = false;
        for (ItemEntity itemEntity : itemEntities)
        {
            if (visited >= Math.max(1, budget)) break;
            visited++;
            ItemStack stack = itemEntity.getItem();
            if (!itemEntity.isAlive() || !WYMLReimplementedHooks.isValidPickup(stack, level))
            {
                continue;
            }

            int remainder = inventory.insertStack(stack.copy(), false);
            if (remainder == 0)
            {
                itemEntity.discard();
            }
            else
            {
                stack.setCount(remainder);
                itemEntity.setItem(stack);
                remaining = true;
            }
        }
        updateUsedCount();
        return new CollectionResult(visited, remaining);
    }

    private int getStoredItemCount()
    {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        {
            count += inventory.getItem(slot).getCount();
        }
        return count;
    }

    public record CollectionResult(int visited, boolean remaining)
    {
    }
}
