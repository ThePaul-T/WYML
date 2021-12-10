package net.creeperhost.wyml.network;

import dev.architectury.networking.NetworkChannel;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.minecraft.resources.ResourceLocation;

public class PacketHandler
{
    public static final NetworkChannel HANDLER = NetworkChannel.create(new ResourceLocation(WhyYouMakeLag.MOD_ID, "main_channel"));

    public static void init()
    {
        HANDLER.register(MessageUpdatePaperbag.class, MessageUpdatePaperbag::write, MessageUpdatePaperbag::new, MessageUpdatePaperbag::handle);
        HANDLER.register(MessageUpdateFence.class, MessageUpdateFence::write, MessageUpdateFence::new, MessageUpdateFence::handle);
        HANDLER.register(MessagePatricle.class, MessagePatricle::write, MessagePatricle::new, MessagePatricle::handle);
    }
}
