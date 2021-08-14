package net.creeperhost.forge.wyml;

import net.creeperhost.WymlExpectPlatform;
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
}
