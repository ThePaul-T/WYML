package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.WymlConfig;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

@Mixin(BlockableEventLoop.class)
public abstract class MixinBlockableEventLoop
{
    /**
     * @author CreeperHost
     * @reason Increase the sleep to help reduce cpu load
     */
    @Overwrite
    protected void waitForTasks()
    {
        Thread.yield();
        LockSupport.parkNanos("waiting for tasks", WymlConfig.cached().TASK_WAIT_NANOS);
    }
}
