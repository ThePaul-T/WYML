package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.scheduling.ItemMergeSchedulingPolicy;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntityMergeScheduling
{
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

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;mergeWithNeighbours()V"))
    private void replaceVanillaMergeSchedule(ItemEntity itemEntity)
    {
        if (!mergeSchedulingEnabled())
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
        if (!mergeSchedulingEnabled() || item.level().isClientSide() || item.isRemoved()) return;

        boolean crossedBlockBoundary = Mth.floor(item.xo) != Mth.floor(item.getX())
                || Mth.floor(item.yo) != Mth.floor(item.getY())
                || Mth.floor(item.zo) != Mth.floor(item.getZ());
        int interval = crossedBlockBoundary
                ? WymlConfig.cached().ITEM_MERGE_MOVING_INTERVAL
                : WymlConfig.cached().ITEM_MERGE_STATIONARY_INTERVAL;
        if (ItemMergeSchedulingPolicy.shouldInitiate(isMergable(), item.tickCount, item.getId(), interval))
        {
            mergeWithNeighbours();
        }
    }

    private boolean mergeSchedulingEnabled()
    {
        return WymlConfig.isEnabled() && WymlConfig.cached().NORMALIZE_ITEM_STACK_MERGING;
    }
}
