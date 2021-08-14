package net.creeperhost.fabric.wyml;

import net.creeperhost.WymlExpectPlatform;
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
