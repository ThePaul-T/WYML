package net.creeperhost.wyml.mixins;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.SpawnState.class)
public class MixinSpawnStateCategoryPolicy
{
    @Shadow @Final private int spawnableChunkCount;
    @Shadow @Final private Object2IntOpenHashMap<MobCategory> mobCategoryCounts;

    @Inject(at = @At("HEAD"), method = "canSpawnForCategoryGlobal", cancellable = true)
    private void canSpawnForCategoryGlobal(MobCategory category, CallbackInfoReturnable<Boolean> cir)
    {
        if (!WymlConfig.isEnabled() || !WymlConfig.cached().ENABLE_CATEGORY_CAP_POLICY) return;
        cir.setReturnValue(WhyYouMakeLag.shouldSpawn(category, mobCategoryCounts, spawnableChunkCount));
    }
}
