package net.creeperhost.wyml.fabric;

import net.creeperhost.polylib.registry.PolyScreens;
import net.fabricmc.api.ClientModInitializer;

public class WymlModFabricClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        PolyScreens.flush();
    }
}
