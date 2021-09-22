package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.blocks.TilePaperBag;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity
{
    @Shadow public abstract ItemStack getItem();
    @Shadow public abstract void setItem(ItemStack itemStack);
    @Shadow public abstract int getAge();

    public MixinItemEntity(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "tick", cancellable = true)
    private void tick(CallbackInfo ci)
    {
        boolean bl = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
        int i = bl ? 2 : 40;
        if (this.tickCount % i == 0)
        {
            if (getAge() > 60 && isOnGround())
            {
                List<ItemEntity> itemEntities = level.getLoadedEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(4.0D, 4.0D, 4.0D));
                if (!itemEntities.isEmpty() && itemEntities.size() > 10)
                {
                    if (canCreateBag(blockPosition()))
                    {
                        try
                        {
                            //Create the paper bag
                            BlockPos paperBagPos = blockPosition();
                            level.setBlock(paperBagPos, WYMLBlocks.PAPER_BAG.get().defaultBlockState(), 4);
                            if (level.getBlockEntity(paperBagPos) == null) level.setBlockEntity(paperBagPos, new TilePaperBag());
                        } catch (Exception ignored)
                        {
                        }
                    }
                }
            }
        }
    }

    private boolean canCreateBag(BlockPos blockPos)
    {
        if(!level.getBlockState(blockPos).isAir()) return false;
        if(level.getBlockState(blockPos.below()).isAir()) return false;

        for(Direction direction : Direction.values())
        {
            if (level.getBlockState(blockPosition().relative(direction, 3)) == WYMLBlocks.PAPER_BAG.get().defaultBlockState())
            {
                return false;
            }
        }
        return true;
    }
}
