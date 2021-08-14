package net.creeperhost.wyml.fabric.wyml;

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
}
