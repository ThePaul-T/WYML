package net.creeperhost.wyml.multiblocks;

import net.creeperhost.mutliblockapi.IMultiblockPart;
import net.creeperhost.mutliblockapi.MultiblockControllerBase;
import net.creeperhost.mutliblockapi.rectangular.RectangularMultiblockControllerBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class MultiBlockFence extends RectangularMultiblockControllerBase
{
    public MultiBlockFence(Level world)
    {
        super(world);
    }

    @Override
    public void onAttachedPartWithMultiblockData(IMultiblockPart part, CompoundTag data)
    {
    }

    @Override
    protected void onBlockAdded(IMultiblockPart newPart)
    {
        System.out.println("Part attached " + newPart.getBlockState().getBlock().getName());
    }

    @Override
    protected void onBlockRemoved(IMultiblockPart oldPart)
    {
        System.out.println("Part renived " + oldPart.getBlockState().getBlock().getName());
    }

    @Override
    protected void onMachineAssembled()
    {
        System.out.println("Multiblock Assembled");
    }

    @Override
    protected void onMachineRestored()
    {
        System.out.println("Multiblock Restored");
    }

    @Override
    protected void onMachinePaused()
    {

    }

    @Override
    protected void onMachineDisassembled()
    {

    }

    @Override
    protected int getMinimumNumberOfBlocksForAssembledMachine()
    {
        return 16;
    }

    @Override
    protected int getMaximumXSize()
    {
        return 32;
    }

    @Override
    protected int getMaximumZSize()
    {
        return 32;
    }

    @Override
    protected int getMaximumYSize()
    {
        return 1;
    }

    @Override
    protected int getMinimumYSize()
    {
        return 1;
    }

    @Override
    protected void onAssimilate(MultiblockControllerBase assimilated)
    {

    }

    @Override
    protected void onAssimilated(MultiblockControllerBase assimilator)
    {

    }

    @Override
    protected boolean updateServer()
    {
        return true;
    }

    @Override
    protected void updateClient()
    {

    }

    @Override
    public void write(CompoundTag data)
    {

    }

    @Override
    public void read(CompoundTag data)
    {

    }

    @Override
    public void formatDescriptionPacket(CompoundTag data)
    {

    }

    @Override
    public void decodeDescriptionPacket(CompoundTag data)
    {

    }
}
