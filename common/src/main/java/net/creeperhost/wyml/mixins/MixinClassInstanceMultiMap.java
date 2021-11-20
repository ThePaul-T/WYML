package net.creeperhost.wyml.mixins;

import net.minecraft.util.ClassInstanceMultiMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin (ClassInstanceMultiMap.class)
public abstract class MixinClassInstanceMultiMap<T>
{

    @Final
    @Shadow
    private List<T> allInstances;
    private List<T> allInstancesImmutable;

    /**
     * By default, vanilla will copy the 'allInstances' List using ImmutableList before returning it.
     * Replace with a singleton 'Collections.unmodifiableList' instance.
     */
    @Inject (
            method = "getAllInstances",
            at = @At (
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void getAllInstances(CallbackInfoReturnable<List<T>> cir)
    {
        if (allInstancesImmutable == null)
        {
            allInstancesImmutable = Collections.unmodifiableList(allInstances);
        }
        cir.setReturnValue(allInstancesImmutable);
        cir.cancel();
    }

}
