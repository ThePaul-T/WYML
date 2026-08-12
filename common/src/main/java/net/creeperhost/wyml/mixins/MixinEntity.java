package net.creeperhost.wyml.mixins;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin (Entity.class)
public abstract class MixinEntity
{

    private int tickOffset = -1;

    protected int getTickOffset() {
        if (tickOffset == -1)
        {
            tickOffset = Math.floorMod(getThis().getId(), 20);
        }
        return tickOffset;
    }

    protected Entity getThis()
    {
        return (Entity) (Object) this;
    }
}
