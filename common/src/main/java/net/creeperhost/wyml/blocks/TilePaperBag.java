package net.creeperhost.wyml.blocks;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class TilePaperBag extends BaseContainerBlockEntity implements TickableBlockEntity
{
    private InventoryPaperBag inventory;

    public TilePaperBag()
    {
        super(WYMLBlocks.PAPER_BAG_TILE.get());
        this.inventory = new InventoryPaperBag(27);
    }

    @Override
    public Component getDefaultName()
    {
        return new TranslatableComponent("container." + WhyYouMakeLag.MOD_ID + ".paper_bag");
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
    public void load(BlockState blockState, CompoundTag compoundTag)
    {
        super.load(blockState, compoundTag);
        inventory.deserializeNBT(compoundTag);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag)
    {
        CompoundTag compoundTag1 = super.save(compoundTag);
        compoundTag1.merge(inventory.serializeNBT());
        return compoundTag1;
    }

    public InventoryPaperBag getInventory()
    {
        return inventory;
    }

    @Override
    public void tick()
    {
        if(level.isClientSide()) return;
        //4.0D == 4 Blocks around the paper bag
        AABB searchArea = new AABB(getBlockPos()).inflate(4.0D, 4.0D, 4.0D);
        List<ItemEntity> itemEntities = level.getLoadedEntitiesOfClass(ItemEntity.class, searchArea);
        if(!itemEntities.isEmpty())
        {
            for (ItemEntity itemEntity : itemEntities)
            {
                if(itemEntity.getAge() > 20)
                {
                    ItemStack itemStack = itemEntity.getItem();
                    ItemStack inserted = inventory.addItem(itemStack);
                    if(inserted.isEmpty()) itemEntity.remove();
                    else itemEntity.setItem(inserted);
                }
            }
        }
    }
}
