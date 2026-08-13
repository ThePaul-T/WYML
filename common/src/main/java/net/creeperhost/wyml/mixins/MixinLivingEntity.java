package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.scheduling.DeterministicTickScheduler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (LivingEntity.class)
public class MixinLivingEntity
{
    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void schedulePushQuery(CallbackInfo ci)
    {
        if (!WymlConfig.cached().NORMALIZE_PUSHING) return;
        Entity entity = (Entity) (Object) this;
        if (entity.level.isClientSide
                || entity instanceof Player
                || entity.isPassenger()
                || entity.isVehicle())
        {
            return;
        }
        if (!DeterministicTickScheduler.shouldRun(
                entity.tickCount, entity.getId(), WymlConfig.cached().ENTITY_PUSH_INTERVAL))
        {
            ci.cancel();
        }
    }
}
