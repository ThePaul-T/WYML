package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlBootConfig;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer
{
    @Unique
    private boolean wyml$postLoadGcRequested;

    @Inject(at = @At("HEAD"), method = "loadLevel")
    private void captureServerBeforeWorldGeneration(CallbackInfo ci)
    {
        if (!WymlConfig.isEnabled()) return;
        WhyYouMakeLag.minecraftServer = (MinecraftServer) (Object) this;
    }

    @Inject(at = @At("TAIL"), method = "loadLevel")
    private void loadLevel(CallbackInfo ci)
    {
        if (!WymlConfig.isEnabled()) return;
        if (!WymlBootConfig.moduleEnabled("post_load_gc")) return;
        if (!WymlConfig.cached().ENABLE_GARBAGE_COLLECTION_LOAD || wyml$postLoadGcRequested) return;

        wyml$postLoadGcRequested = true;
        Runtime runtime = Runtime.getRuntime();
        long usedBefore = runtime.totalMemory() - runtime.freeMemory();
        long started = System.nanoTime();
        WhyYouMakeLag.LOGGER.warn("Requesting the advanced one-shot post-load JVM garbage collection. "
                + "The JVM may ignore this request or pause the server.");
        System.gc();
        long elapsedNanos = System.nanoTime() - started;
        long usedAfter = runtime.totalMemory() - runtime.freeMemory();
        WhyYouMakeLag.LOGGER.warn(
                "Post-load JVM garbage-collection request returned after {} ms; used heap before={} MiB, after={} MiB. "
                        + "This measurement does not prove that the JVM performed a collection.",
                elapsedNanos / 1_000_000.0D,
                usedBefore / (1024L * 1024L),
                usedAfter / (1024L * 1024L));
    }
}
