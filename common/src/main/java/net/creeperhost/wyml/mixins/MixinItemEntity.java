package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.scheduling.DeterministicTickScheduler;
import net.minecraft.util.Mth;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity
{
    @Shadow
    private int age;

    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    private void mergeWithNeighbours()
    {
        throw new AssertionError();
    }

    @Shadow
    private boolean isMergable()
    {
        throw new AssertionError();
    }

    @Inject(at = @At("TAIL"), method = "tick", cancellable = true)
    private void tick(CallbackInfo ci)
    {
        ItemEntity item = (ItemEntity) (Object) this;
        if (!item.level.isClientSide && age >= WymlConfig.cached().ITEM_DESPAWN_TIME) {
            String name = Registry.ITEM.getKey(this.getItem().getItem()).toString();
            if (!WymlConfig.cached().ITEM_DESPAWN_DENYLIST.contains(name)) item.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;mergeWithNeighbours()V"))
    private void replaceVanillaMergeSchedule(ItemEntity itemEntity)
    {
        if (!WymlConfig.cached().NORMALIZE_ITEM_STACK_MERGING)
        {
            mergeWithNeighbours();
        }
    }

    @Inject(method = "tick", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;age:I",
            opcode = Opcodes.GETFIELD,
            ordinal = 0))
    private void runScheduledMerge(CallbackInfo ci)
    {
        ItemEntity item = (ItemEntity) (Object) this;
        if (!WymlConfig.cached().NORMALIZE_ITEM_STACK_MERGING
                || item.level.isClientSide
                || item.isRemoved()
                || !isMergable()) return;

        boolean crossedBlockBoundary = Mth.floor(item.xo) != Mth.floor(item.getX())
                || Mth.floor(item.yo) != Mth.floor(item.getY())
                || Mth.floor(item.zo) != Mth.floor(item.getZ());
        int interval = crossedBlockBoundary
                ? WymlConfig.cached().ITEM_MERGE_MOVING_INTERVAL
                : WymlConfig.cached().ITEM_MERGE_STATIONARY_INTERVAL;
        if (DeterministicTickScheduler.shouldRun(item.tickCount, item.getId(), interval))
        {
            mergeWithNeighbours();
        }
    }
}
