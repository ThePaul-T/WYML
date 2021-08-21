package net.creeperhost.wyml.mixins;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import net.creeperhost.wyml.WymlConfig;
import net.creeperhost.wyml.data.BlankDataFixer;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DataFixers.class)
public abstract class MixinDataFixers
{
    @Shadow
    private static void addFixers(DataFixerBuilder dataFixerBuilder) {}

    @Inject(at = @At("HEAD"), method = "createFixerUpper", cancellable = true)
    private static void create(CallbackInfoReturnable<DataFixer> cir)
    {
        int version = SharedConstants.getCurrentVersion().getWorldVersion();
        DataFixerBuilder dataFixerBuilder = new DataFixerBuilder(version);

        if(!WymlConfig.cached().ENABLE_DFU) dataFixerBuilder = new BlankDataFixer(version);

        addFixers(dataFixerBuilder);
        cir.setReturnValue(dataFixerBuilder.build(Util.bootstrapExecutor()));
    }
}
