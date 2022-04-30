package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (LivingEntity.class)
public class MixinLivingEntity extends MixinEntity
{
    @Inject (method = "doPush", at = @At ("HEAD"), cancellable = true)
    private void doPush(Entity entity, CallbackInfo ci)
    {
        if (!WymlConfig.cached().NORMALIZE_PUSHING) return;
        // Rate limit entity pushing.
        //if (entity.getType() != EntityType.PLAYER && (tickCount + getTickOffset()) % 20 != 0)
        if (entity.getType() != EntityType.PLAYER && (WhyYouMakeLag.getTicks() + getTickOffset()) % 20 != 0)
        {
            ci.cancel();
        }
    }
}
