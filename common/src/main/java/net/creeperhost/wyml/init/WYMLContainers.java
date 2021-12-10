package net.creeperhost.wyml.init;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.containers.ContainerPaperBag;
import net.creeperhost.wyml.tiles.TilePaperBag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WYMLContainers
{
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(WhyYouMakeLag.MOD_ID, Registry.MENU_REGISTRY);

    public static final RegistrySupplier<MenuType<ContainerPaperBag>> PAPER_BAG = MENUS.register("paper_bag", () -> MenuRegistry.ofExtended((id, inventory, data) ->
    {
        BlockPos pos = data.readBlockPos();
        BlockEntity tileEntity = inventory.player.getCommandSenderWorld().getBlockEntity(pos);
        TilePaperBag tileQuantumStorageUnit = (TilePaperBag) tileEntity;
        if (!(tileEntity instanceof TilePaperBag))
        {
            return null;
        }
        return new ContainerPaperBag(id, inventory, tileQuantumStorageUnit);
    }));

//    public static final RegistrySupplier<MenuType<ContainerFence>> FENCE = MENUS.register("fence", () -> MenuRegistry.ofExtended((id, inventory, data) ->
//    {
//        BlockPos pos = data.readBlockPos();
//        BlockEntity tileEntity = inventory.player.getCommandSenderWorld().getBlockEntity(pos);
//        TileMultiBlockFenceGate tileMultiBlockFenceGate = (TileMultiBlockFenceGate) tileEntity;
//        if (!(tileEntity instanceof TileMultiBlockFenceGate))
//        {
//
//            return null;
//        }
//        return new ContainerFence(id, inventory, tileMultiBlockFenceGate);
//    }));
}
