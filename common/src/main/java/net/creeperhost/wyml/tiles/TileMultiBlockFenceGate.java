package net.creeperhost.wyml.tiles;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.blocks.BlockMultiBlockFenceGate;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.time.Instant;
import java.util.*;

public class TileMultiBlockFenceGate extends BlockEntity implements TickableBlockEntity
{
    public Map<BlockPos, Block> CONNECTED_BLOCKS = new HashMap<>();
    public List<BlockPos> DIRTY_BLOCKS = new ArrayList<>();
    public long LAST_UPDATED_TIME = -1;
    public boolean IS_WALKING = false;
    public boolean IS_ASSEMBLED = false;

    public TileMultiBlockFenceGate()
    {
        super(WYMLBlocks.FENCE_GATE_TILE.get());
    }

    //Don't ask me how this works I have no idea anymore
    public void walkFence()
    {
        if(IS_WALKING) return;

        IS_WALKING = true;
        Level level = getLevel();
        if(level == null) return;
        BlockPos gatePos = getBlockPos();
        BlockPos searchPos = gatePos;
        List<BlockTurn> blockTurnList = new ArrayList<>();
        int count = 1;

        while (IS_WALKING)
        {
            Direction direction = getNextDirection(level, searchPos, null);

            if(blockTurnList.isEmpty())
            {
                BlockTurn blockTurn = new BlockTurn(searchPos, direction);
                blockTurnList.add(blockTurn);
            }
            if(blockTurnList.get(blockTurnList.size() - 1).getDirection() != direction && direction != null)
            {
                BlockTurn blockTurn = new BlockTurn(searchPos, direction);
                blockTurnList.add(blockTurn);
            }

            if(direction == null)
            {
//                spawnParticle(level, searchPos, ParticleTypes.SMOKE);

                int i = blockTurnList.size() - count;
                if(i > 0 && blockTurnList.get(i) != null)
                {
                    BlockTurn blockTurn = blockTurnList.get(i);
                    DIRTY_BLOCKS.add(searchPos);

                    for (int j = 0; j < 5; j++)
                    {
                        BlockPos blockPos1 = blockTurn.getBlockPos().relative(blockTurn.direction, j);
                        if(!blockPosMatches(blockTurn.getBlockPos(), blockPos1)) DIRTY_BLOCKS.add(blockPos1);
                    }

                    count++;
                    searchPos = blockTurn.getBlockPos();
                    direction = getNextDirection(level, searchPos, blockTurn.getDirection());

//                    if (!level.isClientSide) System.out.println("Attempting to turn " + direction + " blockTurnList " + blockTurnList.size() + " count " + count);
                }

                if(direction == null && count >= blockTurnList.size())
                {
                    IS_WALKING = false;
                    IS_ASSEMBLED = false;
//                    if(!level.isClientSide) System.out.println("direction is null, breaking loop");
                    break;
                }
            }

//            if(!level.isClientSide && direction != null) System.out.println(searchPos.relative(direction) + " Start: " + gatePos);

            if(direction != null && canConnect(level, searchPos.relative(direction)))
            {
                CONNECTED_BLOCKS.put(searchPos.relative(direction), level.getBlockState(searchPos.relative(direction)).getBlock());
//                spawnParticle(level, searchPos.relative(direction), ParticleTypes.CRIT);
                if(blockPosMatches(searchPos.relative(direction), getBlockPos()))
                {
                    IS_ASSEMBLED = true;
                    IS_WALKING = false;
                    //Remove the old "dirty" blocks
                    if(!DIRTY_BLOCKS.isEmpty())
                    {
                        for (BlockPos blockPos : DIRTY_BLOCKS)
                        {
                            CONNECTED_BLOCKS.remove(blockPos);
                        }
                    }
                    if(!level.isClientSide()) onAssembled();
//                    if(!level.isClientSide) System.out.println("Loop finished, We have found our gate again");
                    break;
                }
                //Reset the counter when connection works
                count = 1;
            }
            if(direction != null) searchPos = searchPos.relative(direction);
        }
    }

    public boolean blockPosMatches(BlockPos blockPos1, BlockPos blockPos2)
    {
        return blockPos1.getX() == blockPos2.getX() && blockPos1.getY() == blockPos2.getY() && blockPos1.getZ() == blockPos2.getZ();
    }

    //Is used for debugging (Don't remove)
    @SuppressWarnings("unused")
    public void spawnParticle(Level level, BlockPos blockPos, ParticleOptions particleOptions)
    {
        Random random = level.random;
        double d = (double)blockPos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
        double e = (double)blockPos.getY() + 1.1D + (random.nextDouble() - 0.5D) * 0.2D;
        double f = (double)blockPos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
        level.addParticle(particleOptions, d, e, f, 0.0D, 0.0D, 0.0D);
    }

    public Direction getNextDirection(Level level, BlockPos blockPos, Direction skip)
    {
        for(Direction direction : Direction.values())
        {
            if(canConnect(level, blockPos.relative(direction)))
            {
                if(direction != null && direction != skip) return direction;
            }
        }
        return null;
    }

    public boolean canConnect(Level level, BlockPos blockPos)
    {
        if(CONNECTED_BLOCKS.containsKey(blockPos)) return false;
        if(level.getBlockState(blockPos).getBlock() instanceof FenceBlock) return true;
        if(level.getBlockState(blockPos).getBlock() instanceof BlockMultiBlockFenceGate) return true;

        return false;
    }

    public void onAssembled()
    {
        WhyYouMakeLag.LOGGER.info("New fence MultiBlock created at " + getBlockPos());
        LAST_UPDATED_TIME = Instant.now().getEpochSecond();
    }

    @Override
    public void tick()
    {
        if(IS_ASSEMBLED)
        {
//            connectedBlocks.forEach((blockPos, block) -> spawnParticle(level, blockPos, ParticleTypes.HEART));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag)
    {
        CompoundTag compoundTag1 = super.save(compoundTag);
        compoundTag1.putLong("lastupdated", LAST_UPDATED_TIME);
        return compoundTag1;
    }

    @Override
    public void load(BlockState blockState, CompoundTag compoundTag)
    {
        super.load(blockState, compoundTag);
        LAST_UPDATED_TIME = compoundTag.getLong("lastupdated");
    }

    public static class BlockTurn
    {
        private final BlockPos blockPos;
        private final Direction direction;

        public BlockTurn(BlockPos blockPos, Direction direction)
        {
            this.blockPos = blockPos;
            this.direction = direction;
        }

        public BlockPos getBlockPos()
        {
            return blockPos;
        }

        public Direction getDirection()
        {
            return direction;
        }
    }
}
