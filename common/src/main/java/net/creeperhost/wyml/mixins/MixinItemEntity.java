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
public abstract class MixinItemEntity extends Entity
{
    @Shadow
    private int age;

    @Shadow public abstract ItemStack getItem();

    public MixinItemEntity(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "tick", cancellable = true)
    private void tick(CallbackInfo ci)
    {
        if (!this.level.isClientSide && this.age >= WymlConfig.cached().ITEM_DESPAWN_TIME)
        {
            String name = Registry.ITEM.getKey(this.getItem().getItem()).toString();
            if(!WymlConfig.cached().ITEM_DESPAWN_DENYLIST.contains(name)) this.remove();
        }
    }
}
