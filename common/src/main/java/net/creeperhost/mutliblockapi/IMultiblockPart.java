package net.creeperhost.mutliblockapi;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public abstract class IMultiblockPart extends BlockEntity
{
	public static final int INVALID_DISTANCE = Integer.MAX_VALUE;

	public IMultiblockPart(BlockEntityType<?> type) {
		super(type);
	}

	public abstract boolean isConnected();

	public abstract MultiblockControllerBase getMultiblockController();

	public abstract BlockPos getWorldLocation();

	public abstract void onAttached(MultiblockControllerBase newController);

	public abstract void onDetached(MultiblockControllerBase multiblockController);

	public abstract void onOrphaned(MultiblockControllerBase oldController, int oldControllerSize, int newControllerSize);

	public abstract MultiblockControllerBase createNewMultiblock();

	public abstract Class<? extends MultiblockControllerBase> getMultiblockControllerType();

	public abstract void onAssimilated(MultiblockControllerBase newController);

	public abstract void setVisited();

	public abstract void setUnvisited();

	public abstract boolean isVisited();

	public abstract void becomeMultiblockSaveDelegate();

	public abstract void forfeitMultiblockSaveDelegate();

	public abstract boolean isMultiblockSaveDelegate();

	public abstract IMultiblockPart[] getNeighboringParts();

	public abstract void onMachineAssembled(MultiblockControllerBase multiblockControllerBase);

	public abstract void onMachineBroken();

	public abstract void onMachineActivated();

	public abstract void onMachineDeactivated();

	public abstract Set<MultiblockControllerBase> attachToNeighbors();

	public abstract void assertDetached();

	public abstract boolean hasMultiblockSaveData();

	public abstract CompoundTag getMultiblockSaveData();

	public abstract void onMultiblockDataAssimilated();

	public abstract BlockState getCachedState();

	public boolean isInvalid() {
		return false;
	}
}