package net.creeperhost.wyml.containers;

import net.creeperhost.polylib.client.modulargui.lib.container.SlotGroup;
import net.creeperhost.polylib.containers.PolyBlockContainerMenu;
import net.creeperhost.polylib.containers.slots.PolySlot;
import net.creeperhost.wyml.init.WYMLContainers;
import net.creeperhost.wyml.tiles.TilePaperBag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerPaperBag extends PolyBlockContainerMenu<TilePaperBag>
{
    public final SlotGroup playerMain = createSlotGroup(0, 1);
    public final SlotGroup playerHotbar = createSlotGroup(0, 1);
    public final SlotGroup paperBag = createSlotGroup(1, 0);

    public ContainerPaperBag(int id, Inventory playerInventory, FriendlyByteBuf extraData)
    {
        super(WYMLContainers.PAPER_BAG.get(), id, playerInventory, extraData);
        addSlots(playerInventory);
    }

    public ContainerPaperBag(int id, Inventory playerInventory, TilePaperBag paperBag)
    {
        super(WYMLContainers.PAPER_BAG.get(), id, playerInventory, paperBag);
        addSlots(playerInventory);
    }

    private void addSlots(Inventory playerInventory)
    {
        playerMain.addPlayerMain(playerInventory);
        playerHotbar.addPlayerBar(playerInventory);
        paperBag.addAllSlots(tile.getInventory(), (container, index) -> new PolySlot(container, index).output());
    }

    @Override
    public void removed(Player player)
    {
        if (!player.level().isClientSide())
        {
            tile.resetDespawnTime();
        }
        super.removed(player);
    }
}
