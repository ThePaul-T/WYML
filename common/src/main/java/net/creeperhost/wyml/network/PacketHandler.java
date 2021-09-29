package net.creeperhost.wyml.network;

import me.shedaniel.architectury.networking.NetworkChannel;
import me.shedaniel.architectury.networking.NetworkManager;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler
{
    public static final NetworkChannel HANDLER = NetworkChannel.create(new ResourceLocation(WhyYouMakeLag.MOD_ID, "main_channel"));

    public static void init()
    {
        HANDLER.register(MessageUpdatePaperbag.class, MessageUpdatePaperbag::write, MessageUpdatePaperbag::new, MessageUpdatePaperbag::handle);
    }
}
