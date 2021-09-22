package net.creeperhost.wyml.forge;

import net.creeperhost.wyml.WymlExpectPlatform;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class WymlExpectPlatformImpl
{
    /**
     * This is our actual method to {@link WymlExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

	public static boolean isModLoaded(String modid) {
    	return ModList.get().isLoaded(modid);
	}
}
