package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.ChunkManager;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.SpawnState.class)
public class MixinSpawnState
{
    @Inject(at = @At("HEAD"), method = "afterSpawn", cancellable = true)
    private void afterSpawn(Mob mob, ChunkAccess chunkAccess, CallbackInfo ci)
    {
        if (!WymlConfig.isEnabled()) return;
        ChunkPos chunkPos = chunkAccess.getPos();
        if (mob != null && mob.isAlive() && mob.level() instanceof ServerLevel serverLevel)
        {
            if (WhyYouMakeLag.hasChunkManager(serverLevel, chunkPos, mob.getType().getCategory()))
            {
                ChunkManager spawnManager = WhyYouMakeLag.getChunkManager(serverLevel, chunkPos, mob.getType().getCategory());
                spawnManager.decreaseSpawningCount();
                WhyYouMakeLag.updateChunkManager(spawnManager);
                if (WymlConfig.cached().DEBUG_PRINT)
                    System.out.println("Completed spawn for " + spawnManager.getClassification().getName() + " " + spawnManager.getChunk() + " - " + (100d - spawnManager.getFailRate()) + "% success rate (" + spawnManager.getFinishRate() + "/" + spawnManager.getStartRate() + ")");
            }
        }
    }
}
