package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobCategory.class)
public class MixinMobCategory
{
    @Mutable
    @Shadow @Final private int max;

    @Mutable
    @Shadow @Final private int despawnDistance;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(String string2, int j, String bl, int bl2, boolean k, boolean string, int i, CallbackInfo ci)
    {
        if(string2.equalsIgnoreCase("monster"))
        {
            this.max = WymlConfig.cached().MONSTER_PER_CHUNK;
            this.despawnDistance = WymlConfig.cached().MONSTER_DESPAWN_DISTANCE;
        }
        if(string2.equalsIgnoreCase("creature"))
        {
            this.max = WymlConfig.cached().CREATURES_PER_CHUNK;
            this.despawnDistance = WymlConfig.cached().CREATURES_DESPAWN_DISTANCE;
        }
        if(string2.equalsIgnoreCase("ambient"))
        {
            this.max = WymlConfig.cached().AMBIENT_CREATURES_PER_CHUNK;
            this.despawnDistance = WymlConfig.cached().AMBIENT_CREATURES_DESPAWN_DISTANCE;
        }
        if(string2.equalsIgnoreCase("water_creature"))
        {
            this.max = WymlConfig.cached().WATER_CREATURES_PER_CHUNK;
            this.despawnDistance = WymlConfig.cached().WATER_CREATURES_DESPAWN_DISTANCE;
        }
        if(string2.equalsIgnoreCase("water_ambient"))
        {
            this.max = WymlConfig.cached().WATER_AMBIENT_PER_CHUNK;
            this.despawnDistance = WymlConfig.cached().WATER_AMBIENT_DESPAWN_DISTANCE;
        }
        if(string2.equalsIgnoreCase("misc"))
        {
            this.max = WymlConfig.cached().MISC_CREATURES_PER_CHUNK;
            this.despawnDistance = WymlConfig.cached().MISC_CREATURES_DESPAWN_DISTANCE;
        }
    }
}
