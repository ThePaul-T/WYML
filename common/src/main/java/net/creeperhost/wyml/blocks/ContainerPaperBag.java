package net.creeperhost.wyml.blocks;

import net.creeperhost.wyml.init.WYMLContainers;
import net.creeperhost.wyml.network.MessageUpdatePaperbag;
import net.creeperhost.wyml.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerPaperBag extends AbstractContainerMenu
{
    private final TilePaperBag tilePaperBag;
    private final Player player;

    public ContainerPaperBag(int id, Inventory playerInventory, TilePaperBag tilePaperBag)
    {
        super(WYMLContainers.PAPER_BAG.get(), id);
        this.tilePaperBag = tilePaperBag;
        this.player = playerInventory.player;

        int i;
        int j;

        //Paper bags Inventory
        for(i = 0; i < 3; ++i) {
            for(j = 0; j < 9; ++j) {
                int slotID = j + i * 9;
                this.addSlot(new SlotOutput(tilePaperBag.getInventory(), slotID, 8 + j * 18, 18 + i * 18));
            }
        }

        //Player Inventory
        for(i = 0; i < 3; ++i) {
            for(j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        //Hotbar
        for(i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public TilePaperBag getTilePaperBag()
    {
        return tilePaperBag;
    }


    @Override
    public void slotsChanged(Container container)
    {
        super.slotsChanged(container);
    }

    public void sortInv(InventoryPaperBag inv)
    {
        for (int i = 0; i < tilePaperBag.getInventory().getContainerSize(); i++)
        {
            if(!inv.getItem(i).isEmpty())
            {
                if(getFirstFreeSlot(inv) != -1)
                {
                    ItemStack stack = inv.getItem(i);
                    inv.setItem(i, ItemStack.EMPTY);
                    inv.setItem(getFirstFreeSlot(inv), stack);
                }
            }
        }
        PacketHandler.HANDLER.sendToPlayer((ServerPlayer) player, new MessageUpdatePaperbag(tilePaperBag.getBlockPos(), tilePaperBag.getUsedSlots(), tilePaperBag.getDespawnTime()));
    }

    public int getFirstFreeSlot(InventoryPaperBag inventoryPaperBag)
    {
        for(int i = 0; i < 27; i++)
        {
            if(inventoryPaperBag.getItem(i).isEmpty()) return i;
        }

        return -1;
    }

    //Shift-Clicking....
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = (Slot) slots.get(slotIndex);
        int numSlots = slots.size();
        if (slot != null && slot.hasItem())
        {
            ItemStack stackInSlot = slot.getItem();
            originalStack = stackInSlot.copy();
            if (slotIndex >= numSlots - 9 * 4 && tryShiftItem(stackInSlot, numSlots))
            {
                // NOOP
            } else if (slotIndex >= numSlots - 9 * 4 && slotIndex < numSlots - 9)
            {
                if (!shiftItemStack(stackInSlot, numSlots - 9, numSlots))
                {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex >= numSlots - 9 && slotIndex < numSlots)
            {
                if (!shiftItemStack(stackInSlot, numSlots - 9 * 4, numSlots - 9))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!shiftItemStack(stackInSlot, numSlots - 9 * 4, numSlots))
            {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stackInSlot, originalStack);
            if (stackInSlot.getCount() <= 0)
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
            if (stackInSlot.getCount() == originalStack.getCount())
            {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stackInSlot);
        }
        return originalStack;
    }

    protected boolean shiftItemStack(ItemStack stackToShift, int start, int end)
    {
        boolean changed = false;
        if (stackToShift.isStackable())
        {
            for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++)
            {
                Slot slot = (Slot) slots.get(slotIndex);
                ItemStack stackInSlot = slot.getItem();
                if (!stackInSlot.isEmpty() && canStacksMerge(stackInSlot, stackToShift))
                {
                    int resultingStackSize = stackInSlot.getCount() + stackToShift.getCount();
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getMaxStackSize());
                    if (resultingStackSize <= max)
                    {
                        stackToShift.setCount(0);
                        stackInSlot.setCount(resultingStackSize);
                        slot.setChanged();
                        changed = true;
                    } else if (stackInSlot.getCount() < max)
                    {
                        stackToShift.setCount(stackToShift.getCount()-(max-stackInSlot.getCount()));
                        stackInSlot.setCount(max);
                        slot.setChanged();
                        changed = true;
                    }
                }
            }
        }
        if (stackToShift.getCount() > 0)
        {
            for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++)
            {
                Slot slot = (Slot) slots.get(slotIndex);
                ItemStack stackInSlot = slot.getItem();
                if (stackInSlot.isEmpty())
                {
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getMaxStackSize());
                    stackInSlot = stackToShift.copy();
                    stackInSlot.setCount(Math.min(stackToShift.getCount(), max));
                    stackToShift.setCount(stackToShift.getCount()-stackInSlot.getCount());
                    slot.set(stackInSlot);
                    slot.setChanged();
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean tryShiftItem(ItemStack stackToShift, int numSlots)
    {
        for (int machineIndex = 0; machineIndex < numSlots - 9 * 4; machineIndex++)
        {
            Slot slot = (Slot) slots.get(machineIndex);
            if (!slot.mayPlace(stackToShift)) continue;
            if (shiftItemStack(stackToShift, machineIndex, machineIndex + 1)) return true;
        }
        return false;
    }

    public static boolean canStacksMerge(ItemStack stack1, ItemStack stack2)
    {
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        if (!stack1.sameItem(stack2)) return false;
        if (!ItemStack.isSame(stack1, stack2)) return false;
        return true;
    }

    @Override
    public boolean stillValid(Player player)
    {
        sortInv(tilePaperBag.getInventory());

        if(player.level.getBlockEntity(tilePaperBag.getBlockPos()) == null) return false;
        return tilePaperBag.getInventory().stillValid(player);
    }
}
