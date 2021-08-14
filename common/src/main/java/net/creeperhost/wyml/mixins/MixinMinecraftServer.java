package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.minecraft.server.MinecraftServer;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer
{
    @Inject(at = @At("HEAD"), method = "stopServer", cancellable = true)
    private void serverStopped(CallbackInfo ci)
    {
        WhyYouMakeLag.serverStopping();
    }

    @Inject(at = @At("RETURN"), method = "spin")
    private static void spin(Function<Thread, CallbackI.S> function, CallbackInfoReturnable<CallbackI.S> cir)
    {
        MinecraftServer minecraftServer = (MinecraftServer) cir.getReturnValue();
        WhyYouMakeLag.serverStarted(minecraftServer);
    }
}
