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
    @Shadow
    private int blockingCount;

    @Shadow
    protected abstract boolean pollTask();

    /**
     * @author CreeperHost
     * @reason because you need to sleep
     */
    @Overwrite
    public void managedBlock(BooleanSupplier booleanSupplier)
    {
        ++this.blockingCount;

        try
        {
            while (!booleanSupplier.getAsBoolean())
            {
                if (!this.pollTask())
                {
                    this.waitForTasks();
                }
                else
                {
                    try
                    {
                        long value = WymlConfig.cached().TASK_WAIT_NANOS / 100;
                        if(value > 0)
                        {
                            TimeUnit.NANOSECONDS.sleep(value);
                        }
                    } catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        } finally
        {
            --this.blockingCount;
        }
    }

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
