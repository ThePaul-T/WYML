package net.creeperhost.wyml.fabric;

import net.creeperhost.wyml.WymlExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class WymlExpectPlatformImpl
{
    /**
     * This is our actual method to {@link WymlExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }
}
