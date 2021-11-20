package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (LivingEntity.class)
public class MixinLivingEntity extends MixinEntity
{

    protected LivingEntity getThis()
    {
        return (LivingEntity) (Object) this;
    }

    @Inject (
            method = "pushEntities",
            at = @At (
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void onPushEntities(CallbackInfo ci)
    {
        if (!WymlConfig.cached().NORMALIZE_PUSHING) return;
        // Rate limit entity pushing.
        if (getThis().getType() != EntityType.PLAYER && (tickCount + getTickOffset()) % 20 != 0)
        {
            ci.cancel();
        }
    }
}
