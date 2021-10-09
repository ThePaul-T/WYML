package net.creeperhost.wyml.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class FencePart
{
    private Block block;
    private boolean isCorner;
    private BlockPos blockPos;

    public FencePart(Block block, BlockPos blockPos, boolean isCorner)
    {
        this.block = block;
        this.isCorner = isCorner;
        this.blockPos = blockPos;
    }

    public Block getBlock()
    {
        return block;
    }

    public boolean isCorner()
    {
        return isCorner;
    }

    public BlockPos getBlockPos()
    {
        return blockPos;
    }
}
