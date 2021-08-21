package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.WymlConfig;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer
{

    @Inject(at = @At("HEAD"), method = "stopServer", cancellable = true)
    private void serverStopped(CallbackInfo ci)
    {
        WhyYouMakeLag.serverStopping();
    }

    @Inject(at = @At("HEAD"), method = "tickServer", cancellable = true)
    private void tickServerPre(CallbackInfo ci)
    {
        if(WymlConfig.cached().NORMALIZE_TICKS)
        {
            WhyYouMakeLag.tickStartNano = Util.getNanos();
        }
    }

    @Inject(at = @At("TAIL"), method = "tickServer", cancellable = true)
    private void tickServerPost(CallbackInfo ci)
    {
        if(WymlConfig.cached().NORMALIZE_TICKS)
        {
            WhyYouMakeLag.tickStopNano = Util.getNanos();
            long dif = WhyYouMakeLag.tickStopNano - WhyYouMakeLag.tickStartNano;
            long l = 18000000 - dif;
            if (l > 0)
            {
                try
                {
                    TimeUnit.NANOSECONDS.sleep(l);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "loadLevel")
    private void loadLevel(CallbackInfo ci)
    {
        if(WymlConfig.cached().ENABLE_GARBAGE_COLLECTION_LOAD) System.gc();
    }

    @Inject(at = @At("RETURN"), method = "spin")
    private static void spin(Function<Thread, CallbackI.S> function, CallbackInfoReturnable<CallbackI.S> cir)
    {
        MinecraftServer minecraftServer = (MinecraftServer) cir.getReturnValue();
        WhyYouMakeLag.serverStarted(minecraftServer);
    }
}
