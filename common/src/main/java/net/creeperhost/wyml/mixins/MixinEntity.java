package net.creeperhost.wyml.mixins;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin (Entity.class)
public abstract class MixinEntity
{

    @Final
    @Shadow
    protected Random random;

    private int tickOffset = -1;

    protected int getTickOffset() {
        if (tickOffset == -1)
        {
            // No Constructor mixins, because (valid)'reasons'. /shrug
            tickOffset = random.nextInt(20);
        }
        return tickOffset;
    }

    protected Entity getThis()
    {
        return (Entity) (Object) this;
    }
}
