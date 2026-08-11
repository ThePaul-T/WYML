package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.util.Util;
import net.minecraft.server.MinecraftServer;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.TimeUnit;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer
{

    @Shadow
    public abstract Thread getRunningThread();

    @Inject(at = @At("HEAD"), method = "loadLevel")
    private void captureServerBeforeWorldGeneration(CallbackInfo ci)
    {
        WhyYouMakeLag.minecraftServer = (MinecraftServer) (Object) this;
    }

    @Inject(at = @At("HEAD"), method = "tickServer", cancellable = true)
    private void tickServerPre(CallbackInfo ci)
    {
        if (WymlConfig.cached().NORMALIZE_TICKS)
        {
            WhyYouMakeLag.tickStartNano = Util.getNanos();
        }
    }

    @Inject(at = @At("TAIL"), method = "tickServer", cancellable = true)
    private void tickServerPost(CallbackInfo ci)
    {
        if (WymlConfig.cached().NORMALIZE_TICKS)
        {
            WhyYouMakeLag.tickStopNano = Util.getNanos();
            long dif = WhyYouMakeLag.tickStopNano - WhyYouMakeLag.tickStartNano;
            long l = 48000000 - dif;
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
        if (WymlConfig.cached().ENABLE_GARBAGE_COLLECTION_LOAD) System.gc();
    }
}
