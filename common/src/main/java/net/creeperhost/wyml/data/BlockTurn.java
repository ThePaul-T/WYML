package net.creeperhost.wyml.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockTurn
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
