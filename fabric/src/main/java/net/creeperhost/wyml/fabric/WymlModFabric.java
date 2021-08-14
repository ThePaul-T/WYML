package net.creeperhost.wyml.fabric;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.fabricmc.api.ModInitializer;

public class WymlModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WhyYouMakeLag.init();
    }
}
