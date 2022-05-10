package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.BagHandler;
import net.creeperhost.wyml.ChunkManager;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class MixinServerWorld
{
    @Inject(at = @At("HEAD"), method = "addEntity", cancellable = true)
    public void addEntity(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if (entity instanceof ItemEntity && WymlConfig.cached().ALLOW_PAPER_BAGS)
        {
            BagHandler.itemEntityAdded((ItemEntity) entity);
        }
    }

    @Inject(at = @At("RETURN"), method = "addEntity", cancellable = true)
    public void addEntity2(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if(entity instanceof Mob && WymlConfig.cached().HARD_MOB_LIMITS)
        {
            ChunkPos pos = entity.chunkPosition();
            ChunkManager cm = WhyYouMakeLag.getChunkManager(pos, entity.level.dimensionType(), entity.getType().getCategory());
            ResourceLocation location = Registry.ENTITY_TYPE.getKey(entity.getType());
            if (cm.reachedMobLimit(location))
            {
                System.out.println("Set entity at " + pos.x + "," + pos.z + " to removed as past spawn limits; " + entity.getType().toString());
                if(entity.isAlive())
                {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }
}
