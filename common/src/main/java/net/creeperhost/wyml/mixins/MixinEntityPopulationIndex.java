package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.spawn.MobPopulationIndex;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntityPopulationIndex
{
    @Inject(method = "setPos(DDD)V", at = @At("RETURN"))
    private void wyml$trackChunkMove(double x, double y, double z, CallbackInfo ci)
    {
        MobPopulationIndex.moved((Entity) (Object) this);
    }

    @Inject(method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At("HEAD"))
    private void wyml$trackRemoval(Entity.RemovalReason reason, CallbackInfo ci)
    {
        MobPopulationIndex.removed((Entity) (Object) this);
    }
}
