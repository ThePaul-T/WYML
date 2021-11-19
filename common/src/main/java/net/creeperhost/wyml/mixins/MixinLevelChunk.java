package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.ChunkManager;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class MixinLevelChunk {
    @Inject(at = @At("HEAD"), method = "addEntity", cancellable = true)
    private void addEntity(Entity entity, CallbackInfo ci)
    {
        if(entity instanceof Mob && WymlConfig.cached().HARD_MOB_LIMITS) {
            ChunkPos pos = new ChunkPos(entity.xChunk, entity.zChunk);
            ChunkManager cm = WhyYouMakeLag.getChunkManager(pos, entity.getType().getCategory());
            ResourceLocation location = Registry.ENTITY_TYPE.getKey(entity.getType());
            if (cm.reachedMobLimit(location)) {
                System.out.println("Set entity at " + pos.x + "," + pos.z + " to removed as past spawn limits; " + entity.getType().toString());
                entity.removed = true;
            }
        }
    }

}
