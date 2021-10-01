package net.creeperhost.wyml.tiles;

import net.creeperhost.mutliblockapi.MultiblockControllerBase;
import net.creeperhost.mutliblockapi.MultiblockValidationException;
import net.creeperhost.mutliblockapi.rectangular.RectangularMultiblockTileEntityBase;
import net.creeperhost.wyml.blocks.BlockMultiBlockFenceGate;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.creeperhost.wyml.multiblocks.MultiBlockFence;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TileMultiBlockFenceGate extends RectangularMultiblockTileEntityBase
{
    public TileMultiBlockFenceGate()
    {
        super(WYMLBlocks.FENCE_GATE_TILE.get());
    }

    @Override
    public Class<? extends MultiblockControllerBase> getMultiblockControllerType()
    {
        return MultiBlockFence.class;
    }

    @Override
    public BlockState getCachedState()
    {
        return this.getBlockState();
    }

    public MultiBlockFence getMultiBlock()
    {
        return (MultiBlockFence) getMultiblockController();
    }


    @Override
    public void onMachineActivated()
    {
        System.out.println("Activated");

    }

    @Override
    public void onMachineDeactivated()
    {
        System.out.println("Deactivated");

    }

    @Override
    public MultiblockControllerBase createNewMultiblock()
    {
        return new MultiBlockFence(getLevel());
    }

    @Override
    public void isGoodForFrame() throws MultiblockValidationException
    {
        if(!(getBlock() instanceof BlockMultiBlockFenceGate))
        {
            throw new MultiblockValidationException(getBlock() + " is not valid for the frame of the block");
        }
    }

    @Override
    public void isGoodForSides() throws MultiblockValidationException
    {
        if(!(getBlock() instanceof BlockMultiBlockFenceGate) || !(getBlock() instanceof FenceBlock))
        {
            throw new MultiblockValidationException(getBlock() + " is not valid for the interior of the block");
        }
    }

    @Override
    public void isGoodForTop() throws MultiblockValidationException
    {
//        if(!(getBlock() instanceof BlockMultiBlockFenceGate) || !(getBlock() instanceof FenceBlock))
//        {
//            throw new MultiblockValidationException(getBlock() + " is not valid for the interior of the block");
//        }
    }

    @Override
    public void isGoodForBottom() throws MultiblockValidationException
    {
//        if(!(getBlock() instanceof BlockMultiBlockFenceGate) || !(getBlock() instanceof FenceBlock))
//        {
//            throw new MultiblockValidationException(getBlock() + " is not valid for the interior of the block");
//        }
    }

    @Override
    public void isGoodForInterior() throws MultiblockValidationException
    {
        if(!getLevel().getBlockState(getBlockPos()).isAir())
        {
            throw new MultiblockValidationException(getBlock() + " is not valid for the interior of the block");
        }
    }

    private Block getBlock()
    {
        return level.getBlockState(getBlockPos()).getBlock();
    }


    @Override
    public void tick()
    {
        super.tick();
    }
}
