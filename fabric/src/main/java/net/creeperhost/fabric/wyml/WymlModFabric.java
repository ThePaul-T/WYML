package net.creeperhost.fabric.wyml;

import net.creeperhost.WhyYouMakeLag;
import net.fabricmc.api.ModInitializer;

public class WymlModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WhyYouMakeLag.init();
    }
}
