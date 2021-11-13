package net.creeperhost.wyml.init;

import me.shedaniel.architectury.registry.MenuRegistry;
import net.creeperhost.wyml.client.ScreenFence;
import net.creeperhost.wyml.client.ScreenPaperBag;

public class WYMLScreens
{
    public static void init()
    {
        MenuRegistry.registerScreenFactory(WYMLContainers.PAPER_BAG.get(), ScreenPaperBag::new);
        MenuRegistry.registerScreenFactory(WYMLContainers.FENCE.get(), ScreenFence::new);
    }
}
