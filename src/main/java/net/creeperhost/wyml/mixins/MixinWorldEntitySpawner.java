package net.creeperhost.wyml.mixins;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.EntityClassification;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldEntitySpawner.EntityDensityManager.class)
public class MixinWorldEntitySpawner
{
    private int realMax = 0;
    private int MOB_TRIES = 5;
    @Shadow @Final private int spawnableChunkCount;
    @Shadow @Final private Object2IntOpenHashMap<EntityClassification> mobCategoryCounts;

    @Inject(at = @At("HEAD"), method = "canSpawnForCategory", cancellable = true)
    private void canSpawnForCategory(EntityClassification entityClassification, CallbackInfoReturnable<Boolean> cir)
    {
        int i = entityClassification.getMaxInstancesPerChunk() * this.spawnableChunkCount / (int)Math.pow(17.0D, 2.0D);
        realMax = i;
        int retVal = 0;
        int curMobs = this.mobCategoryCounts.getInt(entityClassification);
        if(curMobs < i)
        {
            retVal = curMobs + MOB_TRIES;
        }
        if(retVal > realMax) retVal = realMax;
        System.out.println("Current: " + curMobs + " " + retVal);
        boolean value = curMobs < retVal;
        cir.setReturnValue(value);
    }
}
