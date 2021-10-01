package net.creeperhost.mutliblockapi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MultiblockTileEntityBase extends IMultiblockPart implements TickableBlockEntity
{
	private MultiblockControllerBase controller;
	private boolean visited;

	private boolean saveMultiblockData;
	private CompoundTag cachedMultiblockData;
	private boolean paused;

	public MultiblockTileEntityBase(BlockEntityType<?> tileEntityType)
	{
		super(tileEntityType);
		controller = null;
		visited = false;
		saveMultiblockData = false;
		//paused = false;
		cachedMultiblockData = null;
	}


	@Override
	public Set<MultiblockControllerBase> attachToNeighbors()
	{
		Set<MultiblockControllerBase> controllers = null;
		MultiblockControllerBase bestController = null;

		IMultiblockPart[] partsToCheck = getNeighboringParts();
		for (IMultiblockPart neighborPart : partsToCheck)
		{
			if (neighborPart.isConnected())
			{
				MultiblockControllerBase candidate = neighborPart.getMultiblockController();
				if (!candidate.getClass().equals(this.getMultiblockControllerType()))
				{
					continue;
				}

				if (controllers == null)
				{
					controllers = new HashSet<MultiblockControllerBase>();
					bestController = candidate;
				}
				else if (!controllers.contains(candidate) && candidate.shouldConsume(bestController))
				{
					bestController = candidate;
				}
				controllers.add(candidate);
			}
		}
		if (bestController != null)
		{
			this.controller = bestController;
			bestController.attachBlock(this);
		}
		return controllers;
	}

	@Override
	public void assertDetached()
	{
		if (this.controller != null) this.controller = null;
	}

	@Override
	public void load(BlockState blockState, CompoundTag compoundNBT)
	{
		super.load(blockState, compoundNBT);
		if (compoundNBT.contains("multiblockData")) this.cachedMultiblockData = compoundNBT.getCompound("multiblockData");
	}

	@Override
	public CompoundTag save(CompoundTag compoundNBT)
	{
		super.save(compoundNBT);

		if (isMultiblockSaveDelegate() && isConnected())
		{
			CompoundTag multiblockData = new CompoundTag();
			this.controller.write(multiblockData);
			compoundNBT.put("multiblockData", multiblockData);
		}
		return compoundNBT;
	}

//	@Override
//	public void onChunkUnloaded()
//	{
//		super.onChunkUnloaded();
//		detachSelf(true);
//	}

//	@Override
//	public void load(BlockState blockState, CompoundTag compoundTag)
//	{
//		super.load(blockState, compoundTag);
//		MultiblockRegistry.onPartAdded(this.getLevel(), this);
//	}

	public void onLoad()
	{
		MultiblockRegistry.onPartAdded(this.getLevel(), this);
	}


	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		CompoundTag packetData = new CompoundTag();
		encodeDescriptionPacket(packetData);
		return new ClientboundBlockEntityDataPacket(getBlockPos(), 0, packetData);
	}

//	@Override
//	public void onDataPacket(NetworkManager net, ClientboundBlockEntityDataPacket packet)
//	{
//		decodeDescriptionPacket(packet.getTag());
//	}

	protected void encodeDescriptionPacket(CompoundTag packetData)
	{
		if (this.isMultiblockSaveDelegate() && isConnected())
		{
			CompoundTag tag = new CompoundTag();
			getMultiblockController().formatDescriptionPacket(tag);
			packetData.put("multiblockData", tag);
		}
	}

	protected void decodeDescriptionPacket(CompoundTag packetData)
	{
		if (packetData.contains("multiblockData"))
		{
			CompoundTag tag = packetData.getCompound("multiblockData");
			if (isConnected())
			{
				getMultiblockController().decodeDescriptionPacket(tag);
			}
			else
			{
				this.cachedMultiblockData = tag;
			}
		}
	}

	@Override
	public boolean hasMultiblockSaveData()
	{
		return this.cachedMultiblockData != null;
	}

	@Override
	public CompoundTag getMultiblockSaveData()
	{
		return this.cachedMultiblockData;
	}

	@Override
	public void onMultiblockDataAssimilated()
	{
		this.cachedMultiblockData = null;
		controller.read(cachedMultiblockData);
	}

	@Override
	public abstract void onMachineAssembled(MultiblockControllerBase multiblockControllerBase);

	@Override
	public abstract void onMachineBroken();

	@Override
	public abstract void onMachineActivated();

	@Override
	public abstract void onMachineDeactivated();

	@Override
	public boolean isConnected()
	{
		return (controller != null);
	}

	@Override
	public MultiblockControllerBase getMultiblockController()
	{
		return controller;
	}

	@Override
	public BlockPos getWorldLocation()
	{
		return this.getBlockPos();
	}

	@Override
	public void becomeMultiblockSaveDelegate()
	{
		this.saveMultiblockData = true;
	}

	@Override
	public void forfeitMultiblockSaveDelegate()
	{
		this.saveMultiblockData = false;
	}

	@Override
	public boolean isMultiblockSaveDelegate()
	{
		return this.saveMultiblockData;
	}

	@Override
	public void setUnvisited()
	{
		this.visited = false;
	}

	@Override
	public void setVisited()
	{
		this.visited = true;
	}

	@Override
	public boolean isVisited()
	{
		return this.visited;
	}

	@Override
	public void onAssimilated(MultiblockControllerBase newController)
	{
		assert (this.controller != newController);
		this.controller = newController;
	}

	@Override
	public void onAttached(MultiblockControllerBase newController)
	{
		this.controller = newController;
	}

	@Override
	public void onDetached(MultiblockControllerBase oldController)
	{
		this.controller = null;
	}

	@Override
	public abstract MultiblockControllerBase createNewMultiblock();

	@Override
	public IMultiblockPart[] getNeighboringParts() {
		BlockEntity te;
		List<IMultiblockPart> neighborParts = new ArrayList<IMultiblockPart>();
		BlockPos neighborPosition, partPosition = this.getWorldLocation();

		for (Direction facing : Direction.values())
		{
			neighborPosition = partPosition.offset(facing.getNormal());
			te = this.level.getBlockEntity(neighborPosition);

			if (te instanceof IMultiblockPart) neighborParts.add((IMultiblockPart)te);
		}
		return neighborParts.toArray(new IMultiblockPart[neighborParts.size()]);
	}

	@Override
	public void onOrphaned(MultiblockControllerBase controller, int oldSize, int newSize)
	{
		onLoad();
	}

	protected void detachSelf(boolean chunkUnloading)
	{
		if (this.controller != null)
		{
			this.controller.detachBlock(this, chunkUnloading);
			this.controller = null;
		}
		MultiblockRegistry.onPartRemovedFromWorld(getLevel(), this);
	}

	@Override
	public void tick()
	{
		System.out.println("TICK");
	}

	@Override
	public BlockState getBlockState()
	{
		return level.getBlockState(getBlockPos());
	}
}
