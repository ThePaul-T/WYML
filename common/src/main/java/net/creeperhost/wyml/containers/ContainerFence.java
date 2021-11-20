//package net.creeperhost.wyml.containers;
//
//import net.creeperhost.wyml.init.WYMLContainers;
//import net.creeperhost.wyml.tiles.TileMultiBlockFenceGate;
//import net.minecraft.world.Container;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.Slot;
//
//public class ContainerFence extends WYMLContainer
//{
//    private final TileMultiBlockFenceGate tileMultiBlockFenceGate;
//    private final Player player;
//
//    public ContainerFence(int id, Inventory playerInventory, TileMultiBlockFenceGate tileMultiBlockFenceGate)
//    {
//        super(WYMLContainers.FENCE.get(), id);
//        this.tileMultiBlockFenceGate = tileMultiBlockFenceGate;
//        this.player = playerInventory.player;
//
//        int i;
//        int j;
//
//        //Player Inventory
//        for (i = 0; i < 3; ++i)
//        {
//            for (j = 0; j < 9; ++j)
//            {
//                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
//            }
//        }
//
//        //Hotbar
//        for (i = 0; i < 9; ++i)
//        {
//            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
//        }
//    }
//
//    public TileMultiBlockFenceGate getTileMultiBlockFenceGate()
//    {
//        return tileMultiBlockFenceGate;
//    }
//
//    public Player getPlayer()
//    {
//        return player;
//    }
//
//    @Override
//    public boolean stillValid(Player player)
//    {
//        return true;
//    }
//}
