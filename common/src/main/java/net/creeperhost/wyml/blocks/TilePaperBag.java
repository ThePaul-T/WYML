package net.creeperhost.wyml.blocks;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class TilePaperBag extends BaseContainerBlockEntity implements TickableBlockEntity, WorldlyContainer
{
    private final InventoryPaperBag inventory;
    private long DESPAWN_TIME_STAMP;
    private final int DESPAWN_TIME = WymlConfig.cached().PAPER_BAG_DESPAWN_TIME;
    private int USED_SLOTS;

    public TilePaperBag()
    {
        super(WYMLBlocks.PAPER_BAG_TILE.get());
        this.inventory = new InventoryPaperBag(180);
        DESPAWN_TIME_STAMP = (Instant.now().getEpochSecond() + DESPAWN_TIME);
        USED_SLOTS = updateUsedSlots();
    }

    @Override
    public Component getDefaultName()
    {
        return new TranslatableComponent("container." + WhyYouMakeLag.MOD_ID + ".paper_bag");
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory)
    {
        return new ContainerPaperBag(i, inventory, this, containerData);
    }

    public final ContainerData containerData = new ContainerData()
    {
        @Override
        public int get(int index)
        {
            if (index == 0)
            {
                return TilePaperBag.this.updateUsedSlots();
            }
            throw new IllegalArgumentException("Invalid index: " + index);
        }

        @Override
        public void set(int index, int value)
        {
            throw new IllegalStateException("Cannot set values through IIntArray");
        }
        
        @Override
        public int getCount()
        {
            return 1;
        }
    };

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

    public int updateUsedSlots()
    {
        long count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++)
        {
            if (!inventory.getItem(i).isEmpty()) count++;
        }
        return (int) count;
    }

    @Override
    public void load(BlockState blockState, CompoundTag compoundTag)
    {
        super.load(blockState, compoundTag);
        inventory.deserializeNBT(compoundTag);
        DESPAWN_TIME_STAMP = compoundTag.getLong("despawn");
        USED_SLOTS = compoundTag.getInt("usedslots");
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag)
    {
        CompoundTag compoundTag1 = super.save(compoundTag);
        compoundTag1.merge(inventory.serializeNBT());
        compoundTag1.putLong("despawn", DESPAWN_TIME_STAMP);
        compoundTag1.putInt("usedslots", USED_SLOTS);
        return compoundTag1;
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return save(new CompoundTag());
    }

    public InventoryPaperBag getInventory()
    {
        return inventory;
    }

    @Override
    public void tick()
    {
        if(!level.isClientSide())
        {
            collectItems();

            if (Instant.now().getEpochSecond() >= getDespawnTime())
            {
                WhyYouMakeLag.LOGGER.info("Removing PaperBag from location " + getBlockPos() + " Reason: Age");
                inventory.clearContent();
                if (level.getBlockEntity(getBlockPos()) != null) level.removeBlockEntity(getBlockPos());
                level.removeBlock(getBlockPos(), false);
            }
        }
    }

    public long getDespawnTime()
    {
        return DESPAWN_TIME_STAMP;
    }

    public void resetDespawnTime()
    {
        DESPAWN_TIME_STAMP = (Instant.now().getEpochSecond() + DESPAWN_TIME);
    }

    public void collectItems()
    {
        if (level.isClientSide()) return;
        //4.0D == 4 Blocks around the paper bag
        AABB searchArea = new AABB(getBlockPos()).inflate(4.0D, 4.0D, 4.0D);
        if (!level.getLoadedEntitiesOfClass(ItemEntity.class, searchArea).isEmpty())
        {
            for (ItemEntity itemEntity : level.getLoadedEntitiesOfClass(ItemEntity.class, searchArea))
            {
                ItemStack itemStack = itemEntity.getItem();
                if (itemEntity.isAlive())
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
