package net.creeperhost.wyml.containers;

import net.creeperhost.wyml.containers.slots.SlotOutput;
import net.creeperhost.wyml.init.WYMLContainers;
import net.creeperhost.wyml.network.MessageUpdatePaperbag;
import net.creeperhost.wyml.network.PacketHandler;
import net.creeperhost.wyml.tiles.TilePaperBag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerPaperBag extends WYMLContainer
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
        for (i = 0; i < 3; ++i)
        {
            for (j = 0; j < 9; ++j)
            {
                int slotID = j + i * 9;
                this.addSlot(new SlotOutput(tilePaperBag.getInventory(), slotID, 8 + j * 18, 18 + i * 18));
            }
        }

        //Player Inventory
        for (i = 0; i < 3; ++i)
        {
            for (j = 0; j < 9; ++j)
            {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        //Hotbar
        for (i = 0; i < 9; ++i)
        {
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
            if (!inv.getItem(i).isEmpty())
            {
                if (getFirstFreeSlot(inv) != -1)
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
        for (int i = 0; i < 27; i++)
        {
            if (inventoryPaperBag.getItem(i).isEmpty()) return i;
        }

        return -1;
    }

    @Override
    public boolean stillValid(Player player)
    {
        sortInv(tilePaperBag.getInventory());

        if (player.level.getBlockEntity(tilePaperBag.getBlockPos()) == null) return false;
        return tilePaperBag.getInventory().stillValid(player);
    }
}
