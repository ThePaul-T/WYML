package net.creeperhost.wyml.forge;

import net.creeperhost.wyml.WhyYouMakeLag;
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
        WhyYouMakeLag.init();
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, ()-> Pair.of(()-> FMLNetworkConstants.IGNORESERVERONLY, (net, save)->true));
    }
}
