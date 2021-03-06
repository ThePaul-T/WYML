package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends MixinEntity
{
    @Shadow
    private int age;

    @Shadow
    public abstract ItemStack getItem();

    @Inject(at = @At("TAIL"), method = "tick", cancellable = true)
    private void tick(CallbackInfo ci)
    {
        if ((tickCount + getTickOffset()) % 20 == 0) {
            if (!getThis().level.isClientSide && age >= WymlConfig.cached().ITEM_DESPAWN_TIME) {
                String name = Registry.ITEM.getKey(this.getItem().getItem()).toString();
                if (!WymlConfig.cached().ITEM_DESPAWN_DENYLIST.contains(name)) getThis().remove();
            }
        }
    }

    @Inject(method = "mergeWithNeighbours", at = @At(value = "HEAD"), cancellable = true)
    private void onMergeWithNeighbours(CallbackInfo ci) {
        if (WymlConfig.cached().NORMALIZE_ITEM_STACK_MERGING) {
            if ((tickCount + getTickOffset()) % 20 != 0) {
                ci.cancel();
                return;
            }
        }
    }
}
