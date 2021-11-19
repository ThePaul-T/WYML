package net.creeperhost.wyml.mixins;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceBlock.class)
public class MixinFenceBlock
{
    @Inject(at = @At("HEAD"), method = "connectsTo", cancellable = true)
    private void connectsTo(BlockState blockState, boolean bl, Direction direction, CallbackInfoReturnable<Boolean> cir)
    {
//        if(blockState.getBlock() instanceof BlockMultiBlockFenceGate && BlockMultiBlockFenceGate.connectsToDirection(blockState, direction)) cir.setReturnValue(true);
    }
}
