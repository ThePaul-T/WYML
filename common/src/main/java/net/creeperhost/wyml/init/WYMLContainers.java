package net.creeperhost.wyml.init;

import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.MenuRegistry;
import me.shedaniel.architectury.registry.RegistrySupplier;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.blocks.ContainerPaperBag;
import net.creeperhost.wyml.blocks.TilePaperBag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WYMLContainers
{
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(WhyYouMakeLag.MOD_ID, Registry.MENU_REGISTRY);

    public static final RegistrySupplier<MenuType<ContainerPaperBag>> PAPER_BAG = MENUS.register("paper_bag",
            () -> MenuRegistry.ofExtended((id, inventory, data) -> {
                BlockPos pos = data.readBlockPos();
                BlockEntity tileEntity = inventory.player.getCommandSenderWorld().getBlockEntity(pos);
                TilePaperBag tileQuantumStorageUnit = (TilePaperBag) tileEntity;
                if (!(tileEntity instanceof TilePaperBag))
                {
                    return null;
                }
                return new ContainerPaperBag(id, inventory, tileQuantumStorageUnit);
            }));
}