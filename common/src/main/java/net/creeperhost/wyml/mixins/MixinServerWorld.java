package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.BagHandler;
import net.creeperhost.wyml.ChunkManager;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlBootConfig;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
        if (!WymlConfig.isEnabled()) return;
        if (WymlBootConfig.moduleEnabled("paper_bags")
                && entity instanceof ItemEntity && WymlConfig.cached().ALLOW_PAPER_BAGS)
        {
            BagHandler.itemEntityAdded((ItemEntity) entity);
        }
    }

    @Inject(at = @At("RETURN"), method = "addEntity", cancellable = true)
    public void addEntity2(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if (!WymlConfig.isEnabled()) return;
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;
        if (WymlBootConfig.moduleEnabled("per_mob_rules")
                && entity instanceof Mob && WymlConfig.cached().HARD_MOB_LIMITS)
        {
            ChunkPos pos = entity.chunkPosition();
            ServerLevel level = (ServerLevel) (Object) this;
            ChunkManager cm = WymlBootConfig.moduleEnabled("spawn_controller")
                    ? WhyYouMakeLag.getChunkManager(level, pos, entity.getType().getCategory())
                    : new ChunkManager(level, pos, entity.getType().getCategory());
            Identifier location = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (cm.exceedsMobLimit(location))
            {
                if(WymlConfig.cached().DEBUG_PRINT) System.out.println("Set entity at " + pos.x() + "," + pos.z() + " to removed as past spawn limits; " + entity.getType().toString());
                if(entity.isAlive())
                {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }
}
