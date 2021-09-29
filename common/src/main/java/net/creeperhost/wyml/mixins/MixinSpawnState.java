package net.creeperhost.wyml.mixins;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.creeperhost.wyml.WYMLSpawnManager;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.SpawnState.class)
public class MixinSpawnState
{
    @Shadow
    @Final
    private int spawnableChunkCount;
    @Shadow
    @Final
    private Object2IntOpenHashMap<MobCategory> mobCategoryCounts;

    /**
     * @author
     * @reason
     */
    @Overwrite
    private boolean canSpawnForCategory(MobCategory mobCategory)
    {
        return WhyYouMakeLag.shouldSpawn(mobCategory, mobCategoryCounts, spawnableChunkCount);
    }

    @Inject(at = @At("HEAD"), method = "canSpawn", cancellable = true)
    private void canSpawn(EntityType<?> entityType, BlockPos blockPos, ChunkAccess chunkAccess, CallbackInfoReturnable<Boolean> cir)
    {
        WYMLSpawnManager spawnManager = WhyYouMakeLag.getSpawnManager(chunkAccess.getPos(), entityType.getCategory());
        if (spawnManager.isPaused())
        {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "afterSpawn", cancellable = true)
    private void afterSpawn(Mob mob, ChunkAccess chunkAccess, CallbackInfo ci)
    {
        ChunkPos chunkPos = chunkAccess.getPos();
        if (mob != null && mob.isAlive() && mob.level != null)
        {
            if (WhyYouMakeLag.hasSpawnManager(chunkPos, mob.getType().getCategory()))
            {
                WYMLSpawnManager spawnManager = WhyYouMakeLag.getSpawnManager(chunkPos, mob.getType().getCategory());
                spawnManager.decreaseSpawningCount(mob.blockPosition());
                WhyYouMakeLag.updateSpawnManager(spawnManager);
                if (WymlConfig.cached().DEBUG_PRINT)
                    System.out.println("Completed spawn for " + spawnManager.getClassification().getName() + " " + spawnManager.getChunk() + " - " + (100d - spawnManager.getFailRate()) + "% success rate (" + spawnManager.getFinishRate() + "/" + spawnManager.getStartRate() + ")");
            }
        }
    }
}
