package net.creeperhost.wyml.neoforge;

import net.creeperhost.polylib.neoforge.registry.NeoPolyRegistry;
import net.creeperhost.polylib.neoforge.registry.NeoPolyScreens;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod(WhyYouMakeLag.MOD_ID)
public class WymlModNeoForge
{
    public WymlModNeoForge(IEventBus modEventBus)
    {
        WhyYouMakeLag.init();
        NeoPolyRegistry.registerToBus(modEventBus, WhyYouMakeLag.MOD_ID);
        if (FMLLoader.getCurrent().getDist().isClient())
        {
            NeoPolyScreens.registerToBus(modEventBus);
        }
    }
}
