package net.creeperhost.wyml.forge;

import me.shedaniel.architectury.platform.forge.EventBuses;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod(WhyYouMakeLag.MOD_ID)
public class WymlModForge
{
    public WymlModForge()
    {
        EventBuses.registerModEventBus(WhyYouMakeLag.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        WhyYouMakeLag.init();
    }
}
