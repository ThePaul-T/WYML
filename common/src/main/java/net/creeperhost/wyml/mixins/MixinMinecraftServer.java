package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer
{
    @Inject(at = @At("HEAD"), method = "stopServer", cancellable = true)
    private void serverStopped(CallbackInfo ci)
    {
        WhyYouMakeLag.serverStopping();
    }

    @Inject(at = @At("TAIL"), method = "loadLevel")
    private void loadLevel(CallbackInfo ci)
    {
        if (WymlConfig.cached().ENABLE_GARBAGE_COLLECTION_LOAD)
        {
            long started = System.nanoTime();
            System.gc();
            WhyYouMakeLag.LOGGER.warn("WYML requested a one-shot full GC after level load; call returned after {} ms",
                    (System.nanoTime() - started) / 1_000_000L);
        }
    }

    @Inject(at = @At("RETURN"), method = "spin")
    private static void spin(Function<Thread, MinecraftServer> function, CallbackInfoReturnable<MinecraftServer> cir)
    {
        MinecraftServer minecraftServer = (MinecraftServer) cir.getReturnValue();
        WhyYouMakeLag.serverStarted(minecraftServer);
    }
}
