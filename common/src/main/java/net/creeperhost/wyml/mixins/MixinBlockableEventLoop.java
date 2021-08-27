package net.creeperhost.wyml.mixins;

import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

@Mixin(BlockableEventLoop.class)
public abstract class MixinBlockableEventLoop
{
    @Shadow @Final private String name;

    @Shadow public abstract int getPendingTasksCount();

    @Shadow protected abstract Thread getRunningThread();

    @Inject(at = @At("TAIL"), method = "<init>")
    protected void init(String string, CallbackInfo ci)
    {
        CompletableFuture.runAsync(() ->
        {
            while (true)
            {
                try
                {
                    if(getPendingTasksCount() == 0)
                    {
                        LockSupport.unpark(getRunningThread());
                        Thread.sleep(1000);
                    }
                    Thread.sleep(50);
                } catch (Exception ignored) {}
            }
        });
    }

    /**
     * @author CreeperHost
     * @reason Increase the sleep to help reduce cpu load
     */
    @Overwrite
    public void waitForTasks()
    {
        Thread.yield();
        LockSupport.park(name);
    }
}
