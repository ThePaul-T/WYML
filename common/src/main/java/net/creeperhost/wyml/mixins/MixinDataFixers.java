package net.creeperhost.wyml.mixins;

import com.mojang.datafixers.DataFixerBuilder;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DataFixers.class)
public abstract class MixinDataFixers
{
    @Shadow
    private static void addFixers(DataFixerBuilder dataFixerBuilder)
    {
    }

    //    /**
    //     * @author CreeperHost
    //     * @reason Because I can...
    //     */
    //    @Overwrite
    //    private static DataFixer createFixerUpper()
    //    {
    //        WhyYouMakeLag.LOGGER.info("Disabled DFU!");
    //        int version = SharedConstants.getCurrentVersion().getWorldVersion();
    //        DataFixerBuilder dataFixerBuilder = new BlankDataFixer(version);
    //
    //        addFixers(dataFixerBuilder);
    //        return dataFixerBuilder.build(Util.bootstrapExecutor());
    //    }
}
