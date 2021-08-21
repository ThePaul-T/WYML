package net.creeperhost.wyml.mixins;

import com.mojang.datafixers.DataFixerBuilder;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.WymlConfig;
import net.creeperhost.wyml.WymlExpectPlatform;
import net.creeperhost.wyml.data.BlankDataFixer;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DataFixers.class)
public abstract class MixinDataFixers
{
    @Redirect(at = @At(value = "NEW", target = "com/mojang/datafixers/DataFixerBuilder"), method = "createFixerUpper")
    private static DataFixerBuilder create(int dataversion)
    {
        WymlConfig.init(WymlExpectPlatform.getConfigDirectory().resolve(WhyYouMakeLag.MOD_ID + ".json").toFile());
        return WymlConfig.cached().ENABLE_DFU ? new DataFixerBuilder(dataversion) : new BlankDataFixer(dataversion);
    }
}
