package net.creeperhost.wyml.mixins;

import com.mojang.datafixers.DataFixer;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtUtils.class)
public class NbtUtilsMixin
{
    @Inject(method = "update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/util/datafix/DataFixTypes;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "HEAD"), cancellable = true)
    private static void onUpdate(DataFixer dataFixer, DataFixTypes dataFixTypes, CompoundTag compoundTag, int version, int newVersion, CallbackInfoReturnable<CompoundTag> cir)
    {
        if (WymlConfig.cached().ENABLE_DATA_FIXER_UPPER_NBT_PATCH && version == newVersion)
        {
            cir.setReturnValue(compoundTag);
        }
    }
}
