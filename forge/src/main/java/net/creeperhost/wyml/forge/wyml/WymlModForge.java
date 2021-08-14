package net.creeperhost.wyml.forge.wyml;

import me.shedaniel.architectury.platform.forge.EventBuses;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WhyYouMakeLag.MOD_ID)
public class WymlModForge
{
    public WymlModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(WhyYouMakeLag.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        WhyYouMakeLag.init();
    }
}
