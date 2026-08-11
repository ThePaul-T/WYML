package net.creeperhost.wyml.init;

import net.creeperhost.polylib.registry.PolyRegistry;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.containers.ContainerPaperBag;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public final class WYMLContainers
{
    public static final PolyRegistry<MenuType<?>> MENUS = PolyRegistry.create(Registries.MENU, WhyYouMakeLag.MOD_ID);
    public static final Supplier<MenuType<ContainerPaperBag>> PAPER_BAG = MENUS.registerMenu("paper_bag", ContainerPaperBag::new);

    private WYMLContainers()
    {
    }

    public static void init()
    {
        MENUS.init();
    }
}
