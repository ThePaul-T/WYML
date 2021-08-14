package net.creeperhost.wyml.mixins;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.creeperhost.wyml.WYMLSpawnManager;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.WymlConfig;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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

    /**
     * @author
     * @reason
     */
    @Overwrite
    private boolean canSpawnForCategory(EntityClassification p_234991_1_)
    {
        return WhyYouMakeLag.shouldSpawn(p_234991_1_, mobCategoryCounts, spawnableChunkCount);
    }
    @Inject(at = @At("HEAD"), method = "canSpawn", cancellable = true)
    private void canSpawn(EntityType<?> p_234989_1_, BlockPos p_234989_2_, IChunk p_234989_3_, CallbackInfoReturnable<Boolean> cir)
    {
        WYMLSpawnManager spawnManager = WhyYouMakeLag.getSpawnManager(p_234989_3_.getPos(), p_234989_1_.getCategory());
        if(spawnManager.isPaused())
        {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }
    }
    @Inject(at = @At("HEAD"), method = "afterSpawn", cancellable = true)
    private void afterSpawn(MobEntity mobEntity, IChunk chunk, CallbackInfo ci)
    {
        ChunkPos chunkPos = chunk.getPos();
        if (mobEntity != null && mobEntity.isAlive() && mobEntity.level != null)
        {
            if(WhyYouMakeLag.hasSpawnManager(chunkPos, mobEntity.getClassification(true))) {
                WYMLSpawnManager spawnManager = WhyYouMakeLag.getSpawnManager(chunkPos, mobEntity.getClassification(true));
                spawnManager.decreaseSpawningCount(mobEntity.blockPosition());
                WhyYouMakeLag.updateSpawnManager(spawnManager);
                if(WymlConfig.DEBUG_PRINT.get()) System.out.println("Completed spawn for " + spawnManager.getClassification().getName() + " " + spawnManager.getChunk() + " - " + (100d - spawnManager.getFailRate()) + "% success rate (" + spawnManager.getFinishRate() + "/" + spawnManager.getStartRate() + ")");
            }
        }
    }
}
