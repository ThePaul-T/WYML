package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.MobManager;
import net.creeperhost.wyml.config.ModSpawnConfig;
import net.creeperhost.wyml.data.MobSpawnData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(MobSpawnSettings.Builder.class)
public class MixinMobSpawnSettings
{
    @Shadow @Final private Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners;

    @Inject(at = @At("HEAD"), method = "addSpawn", cancellable = true)
    private void addSpawn(MobCategory mobCategory, MobSpawnSettings.SpawnerData spawnerData, CallbackInfoReturnable<MobSpawnSettings.Builder> cir)
    {
        ResourceLocation resourceLocation = Registry.ENTITY_TYPE.getKey(spawnerData.type);
        ModSpawnConfig modSpawnConfig = MobManager.getMod(resourceLocation.getNamespace());
        if(modSpawnConfig != null)
        {
            MobSpawnData mobSpawnData = modSpawnConfig.getMob(resourceLocation.getPath());
            if (mobSpawnData != null)
            {
                int limit = mobSpawnData.limit;
                if(spawnerData.maxCount > limit)
                {
                    MobSpawnSettings.SpawnerData fixed = new MobSpawnSettings.SpawnerData(spawnerData.type, spawnerData.getWeight(), limit, limit);
                    this.spawners.get(mobCategory).add(fixed);
                    MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
                    cir.setReturnValue(builder);
                    return;
                }
            }
        }
    }
}
