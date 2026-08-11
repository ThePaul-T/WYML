package net.creeperhost.wyml.init;

import net.creeperhost.polylib.registry.PolyScreens;
import net.creeperhost.wyml.client.ScreenPaperBag;

public final class WYMLScreens
{
    private WYMLScreens()
    {
    }

    public static void init()
    {
        PolyScreens.register(WYMLContainers.PAPER_BAG, ScreenPaperBag::create);
    }
}
