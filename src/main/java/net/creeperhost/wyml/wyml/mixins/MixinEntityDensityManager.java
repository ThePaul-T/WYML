package net.creeperhost.wyml.wyml.mixins;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.creeperhost.wyml.wyml.WhyYouMakeLag;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldEntitySpawner.EntityDensityManager.class)
public class MixinEntityDensityManager
{
    @Shadow @Final private int spawnableChunkCount;
    @Shadow @Final private Object2IntOpenHashMap<EntityClassification> mobCategoryCounts;

    @Inject(at = @At("HEAD"), method = "canSpawnForCategory", cancellable = true)
    private void canSpawnForCategory(EntityClassification entityClassification, CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(WhyYouMakeLag.shouldSpawn(entityClassification, mobCategoryCounts, spawnableChunkCount));
    }

//    @Inject(at = @At("HEAD"), method = "afterSpawn", cancellable = true)
//    public void afterSpawn(MobEntity mobEntity, IChunk iChunk, CallbackInfo ci)
//    {
//        if(mobEntity != null && mobEntity.isAlive() && mobEntity.level != null)
//        {
//            EntityClassification entityClassification = mobEntity.getClassification(true);
//            ChunkPos chunkPos = iChunk.getPos();
//            String id = chunkPos + entityClassification.getName();
//
//            if (WhyYouMakeLag.FAIL_COUNT.containsKey(id))
//                WhyYouMakeLag.FAIL_COUNT.put(id, WhyYouMakeLag.FAIL_COUNT.get(id) - 1);
//        }
//    }
}
