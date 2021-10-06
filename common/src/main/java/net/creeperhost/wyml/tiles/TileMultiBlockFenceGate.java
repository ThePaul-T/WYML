package net.creeperhost.wyml.tiles;

import net.creeperhost.wyml.blocks.BlockMultiBlockFenceGate;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TileMultiBlockFenceGate extends BlockEntity implements TickableBlockEntity
{
    public Map<BlockPos, Block> connectedBlocks = new HashMap<>();
    public List<BlockPos> dirtyList = new ArrayList<>();

    public TileMultiBlockFenceGate()
    {
        super(WYMLBlocks.FENCE_GATE_TILE.get());
    }

    public boolean isWalking = false;
    public boolean isAssembled = false;

    public void walkFence()
    {
        if(isWalking) return;

        isWalking = true;
        Level level = getLevel();
        if(level == null) return;
        BlockPos gatePos = getBlockPos();
        BlockPos searchPos = gatePos;
        List<BlockTurn> blockTurnList = new ArrayList<>();
        int count = 1;

        while (isWalking)
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
                spawnParticle(level, searchPos, ParticleTypes.SMOKE);

                int i = blockTurnList.size() - count;
                if(i > 0 && blockTurnList.get(i) != null)
                {
                    BlockTurn blockTurn = blockTurnList.get(i);
                    dirtyList.add(searchPos);

                    for (int j = 0; j < 5; j++)
                    {
                        BlockPos blockPos1 = blockTurn.getBlockPos().relative(blockTurn.direction, j);
                        if(!blockPosMatches(blockTurn.getBlockPos(), blockPos1)) dirtyList.add(blockPos1);
                    }

                    count++;
                    searchPos = blockTurn.getBlockPos();
                    direction = getNextDirection(level, searchPos, blockTurn.getDirection());

                    if (!level.isClientSide) System.out.println("Attempting to turn " + direction + " blockTurnList " + blockTurnList.size() + " count " + count);
                }

                if(direction == null && count >= blockTurnList.size())
                {
                    isWalking = false;
                    isAssembled = false;
                    if(!level.isClientSide) System.out.println("direction is null, breaking loop");
                    break;
                }
            }
            if(!level.isClientSide && direction != null) System.out.println(searchPos.relative(direction) + " Start: " + gatePos);
            if(direction != null && canConnect(level, searchPos.relative(direction)))
            {
                connectedBlocks.put(searchPos.relative(direction), level.getBlockState(searchPos.relative(direction)).getBlock());
                spawnParticle(level, searchPos.relative(direction), ParticleTypes.CRIT);
                if(blockPosMatches(searchPos.relative(direction), getBlockPos()))
                {
                    isAssembled = true;
                    isWalking = false;
                    //Remove the old "dirty" blocks
                    if(!dirtyList.isEmpty())
                    {
                        for (BlockPos blockPos : dirtyList)
                        {
                            connectedBlocks.remove(blockPos);
                        }
                    }
                    if(!level.isClientSide) System.out.println("Loop finished, We have found our gate again");
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
        if(connectedBlocks.containsKey(blockPos)) return false;
        if(level.getBlockState(blockPos).getBlock() instanceof FenceBlock) return true;
        if(level.getBlockState(blockPos).getBlock() instanceof BlockMultiBlockFenceGate) return true;

        return false;
    }

    @Override
    public void tick()
    {
        if(isAssembled)
        {
            connectedBlocks.forEach((blockPos, block) -> spawnParticle(level, blockPos, ParticleTypes.HEART));
        }
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
