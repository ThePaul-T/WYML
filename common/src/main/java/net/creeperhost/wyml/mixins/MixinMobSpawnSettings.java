package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.MobManager;
import net.creeperhost.wyml.config.ModSpawnConfig;
import net.creeperhost.wyml.data.MobSpawnData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(MobSpawnSettings.Builder.class)
public class MixinMobSpawnSettings
{
    @Shadow @Final private Map<MobCategory, WeightedList.Builder<MobSpawnSettings.SpawnerData>> spawners;

    @Inject(at = @At("HEAD"), method = "addSpawn", cancellable = true)
    private void addSpawn(MobCategory mobCategory, int weight, MobSpawnSettings.SpawnerData spawnerData, CallbackInfoReturnable<MobSpawnSettings.Builder> cir)
    {
        Identifier resourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(spawnerData.type());
        ModSpawnConfig modSpawnConfig = MobManager.getMod(resourceLocation.getNamespace());
        if(modSpawnConfig != null)
        {
            MobSpawnData mobSpawnData = modSpawnConfig.getMob(resourceLocation.getPath());
            if (mobSpawnData != null)
            {
                int limit = mobSpawnData.limit;
                if(spawnerData.maxCount() > limit)
                {
                    MobSpawnSettings.SpawnerData fixed = new MobSpawnSettings.SpawnerData(spawnerData.type(), limit, limit);
                    this.spawners.get(mobCategory).add(fixed, weight);
                    cir.setReturnValue((MobSpawnSettings.Builder) (Object) this);
                    return;
                }
            }
        }
    }
}
