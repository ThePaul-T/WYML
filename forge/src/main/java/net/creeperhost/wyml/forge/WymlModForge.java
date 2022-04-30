package net.creeperhost.wyml.forge;

import dev.architectury.platform.forge.EventBuses;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(WhyYouMakeLag.MOD_ID)
public class WymlModForge
{
    public WymlModForge()
    {
        System.out.println("Hi I'm a mod, Please let me load");
        EventBuses.registerModEventBus(WhyYouMakeLag.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        WhyYouMakeLag.init();
    }
}
