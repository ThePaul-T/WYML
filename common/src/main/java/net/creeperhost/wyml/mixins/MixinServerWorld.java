package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.BagHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class MixinServerWorld
{
    @Inject(at = @At("HEAD"), method = "addFreshEntity", cancellable = true)
    public void addEntity(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if (entity instanceof ItemEntity)
        {
            BagHandler.itemEntityAdded((ItemEntity) entity);
        }
    }
}
