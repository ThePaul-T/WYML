package net.creeperhost.mutliblockapi;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public abstract class MultiblockControllerBase
{
	public static final short DIMENSION_UNBOUNDED = -1;
	protected Level worldObj;
	protected enum AssemblyState
	{
		Disassembled, Assembled, Paused
	}
	protected AssemblyState assemblyState;
	public HashSet<IMultiblockPart> connectedParts;
	private BlockPos referenceCoord;
	private BlockPos minimumCoord;
	private BlockPos maximumCoord;
	private boolean shouldCheckForDisconnections;
	private MultiblockValidationException lastValidationException;

	protected boolean debugMode;

	protected MultiblockControllerBase(Level world)
	{
		worldObj = world;
		connectedParts = new HashSet<IMultiblockPart>();

		referenceCoord = null;
		assemblyState = AssemblyState.Disassembled;

		minimumCoord = null;
		maximumCoord = null;

		shouldCheckForDisconnections = true;
		lastValidationException = null;

		debugMode = false;
	}

	public void setDebugMode(boolean active)
	{
		debugMode = active;
	}

	public boolean isDebugMode()
	{
		return debugMode;
	}

	public abstract void onAttachedPartWithMultiblockData(IMultiblockPart part, CompoundTag data);

	public boolean hasBlock(BlockPos blockCoord) {
		return connectedParts.contains(blockCoord);
	}

	public void attachBlock(IMultiblockPart part)
	{
		BlockPos coord = part.getWorldLocation();

		if (!connectedParts.add(part))
		{
			System.out.println(String.format("[%s] Controller %s is double-adding part %d @ %s. This is unusual. If you encounter odd behavior, please tear down the machine and rebuild it.",
					(worldObj.isClientSide ? "CLIENT" : "SERVER"), hashCode(), part.hashCode(), coord));
		}

		part.onAttached(this);
		this.onBlockAdded(part);

		if (part.hasMultiblockSaveData())
		{
			CompoundTag savedData = part.getMultiblockSaveData();
			onAttachedPartWithMultiblockData(part, savedData);
			part.onMultiblockDataAssimilated();
		}

		if (this.referenceCoord == null)
		{
			referenceCoord = coord;
			part.becomeMultiblockSaveDelegate();
		}
		else if (coord.compareTo(referenceCoord) < 0)
		{
			BlockEntity te = this.worldObj.getBlockEntity(referenceCoord);
			((IMultiblockPart) te).forfeitMultiblockSaveDelegate();

			referenceCoord = coord;
			part.becomeMultiblockSaveDelegate();
		}
		else
		{
			part.forfeitMultiblockSaveDelegate();
		}

		boolean updateRequired = false;
		BlockPos partPos = part.getBlockPos();

		if (minimumCoord != null)
		{
			if (partPos.getX() < minimumCoord.getX())
			{
				updateRequired = true;
			}
			if (partPos.getY() < minimumCoord.getY())
			{
				updateRequired = true;
			}
			if (partPos.getZ() < minimumCoord.getZ())
			{
				updateRequired = true;
			}
			if (updateRequired)
			{
				this.minimumCoord = new BlockPos(partPos.getX(), partPos.getY(), partPos.getZ());
			}
		}

		if (maximumCoord != null)
		{
			if (partPos.getX() > maximumCoord.getX())
			{
				updateRequired = true;
			}
			if (partPos.getY() > maximumCoord.getY())
			{
				updateRequired = true;
			}
			if (partPos.getZ() > maximumCoord.getZ())
			{
				updateRequired = true;
			}
			if (updateRequired)
			{
				this.maximumCoord = new BlockPos(partPos.getX(), partPos.getY(), partPos.getZ());
			}
		}
		MultiblockRegistry.addDirtyController(worldObj, this);
	}

	protected abstract void onBlockAdded(IMultiblockPart newPart);

	protected abstract void onBlockRemoved(IMultiblockPart oldPart);

	protected abstract void onMachineAssembled();

	protected abstract void onMachineRestored();

	protected abstract void onMachinePaused();

	protected abstract void onMachineDisassembled();

	private void onDetachBlock(IMultiblockPart part)
	{
		part.onDetached(this);
		this.onBlockRemoved(part);
		part.forfeitMultiblockSaveDelegate();

		minimumCoord = maximumCoord = null;

		if (referenceCoord != null && referenceCoord.equals(part.getBlockPos()))
		{
			referenceCoord = null;
		}
		shouldCheckForDisconnections = true;
	}

	public void detachBlock(IMultiblockPart part, boolean chunkUnloading)
	{
		if (chunkUnloading && this.assemblyState == AssemblyState.Assembled)
		{
			this.assemblyState = AssemblyState.Paused;
			this.onMachinePaused();
		}

		onDetachBlock(part);
		if (!connectedParts.remove(part))
		{
			System.out.println(
					String.format("[%s] Double-removing part (%d) @ %d, %d, %d, this is unexpected and may cause problems. If you encounter anomalies, please tear down the reactor and rebuild it.",
							worldObj.isClientSide ? "CLIENT" : "SERVER", part.hashCode(), part.getBlockPos().getX(),
							part.getBlockPos().getY(), part.getBlockPos().getZ()));
		}

		if (connectedParts.isEmpty())
		{
			MultiblockRegistry.addDeadController(this.worldObj, this);
			return;
		}

		MultiblockRegistry.addDirtyController(this.worldObj, this);

		if (referenceCoord == null)
		{
			selectNewReferenceCoord();
		}
	}

	protected abstract int getMinimumNumberOfBlocksForAssembledMachine();

	protected abstract int getMaximumXSize();

	protected abstract int getMaximumZSize();

	protected abstract int getMaximumYSize();

	protected int getMinimumXSize() {
		return 1;
	}

	protected int getMinimumYSize() {
		return 1;
	}

	protected int getMinimumZSize() {
		return 1;
	}

	public MultiblockValidationException getLastValidationException() {
		return lastValidationException;
	}

	protected abstract void isMachineWhole() throws MultiblockValidationException;

	public void checkIfMachineIsWhole()
	{
		AssemblyState oldState = this.assemblyState;
		boolean isWhole;
		this.lastValidationException = null;
		try
		{
			isMachineWhole();
			isWhole = true;
		}
		catch (MultiblockValidationException e)
		{
			lastValidationException = e;
			isWhole = false;
		}
		if (isWhole)
		{
			assembleMachine(oldState);
		}
		else if (oldState == AssemblyState.Assembled)
		{
			disassembleMachine();
		}
	}

	private void assembleMachine(AssemblyState oldState)
	{
		for (IMultiblockPart part : connectedParts)
		{
			part.onMachineAssembled(this);
		}

		this.assemblyState = AssemblyState.Assembled;
		if (oldState == AssemblyState.Paused)
		{
			onMachineRestored();
		}
		else
		{
			onMachineAssembled();
		}
	}

	private void disassembleMachine()
	{
		for (IMultiblockPart part : connectedParts)
		{
			part.onMachineBroken();
		}
		this.assemblyState = AssemblyState.Disassembled;
		onMachineDisassembled();
	}

	public void assimilate(MultiblockControllerBase other)
	{
		BlockPos otherReferenceCoord = other.getReferenceCoord();
		if (otherReferenceCoord != null && getReferenceCoord().compareTo(otherReferenceCoord) >= 0)
		{
			throw new IllegalArgumentException("The controller with the lowest minimum-coord value must consume the one with the higher coords");
		}

		Set<IMultiblockPart> partsToAcquire = new HashSet<IMultiblockPart>(other.connectedParts);

		other._onAssimilated(this);

		for (IMultiblockPart acquiredPart : partsToAcquire)
		{
			if (acquiredPart.isInvalid())
			{
				continue;
			}

			connectedParts.add(acquiredPart);
			acquiredPart.onAssimilated(this);
			this.onBlockAdded(acquiredPart);
		}

		this.onAssimilate(other);
		other.onAssimilated(this);
	}

	private void _onAssimilated(MultiblockControllerBase otherController)
	{
		if (referenceCoord != null)
		{
			if (this.worldObj.isLoaded(this.referenceCoord))
			{
				BlockEntity te = this.worldObj.getBlockEntity(referenceCoord);
				if (te instanceof IMultiblockPart)
				{
					((IMultiblockPart) te).forfeitMultiblockSaveDelegate();
				}
			}
			this.referenceCoord = null;
		}
		connectedParts.clear();
	}

	protected abstract void onAssimilate(MultiblockControllerBase assimilated);

	protected abstract void onAssimilated(MultiblockControllerBase assimilator);

	public final void updateMultiblockEntity()
	{
		if (connectedParts.isEmpty())
		{
			MultiblockRegistry.addDeadController(this.worldObj, this);
			return;
		}

		if (this.assemblyState != AssemblyState.Assembled)
		{
			return;
		}

		if (worldObj.isClientSide)
		{
			updateClient();
		}
		else if (updateServer())
		{
			if (minimumCoord != null && maximumCoord != null && this.worldObj.hasChunksAt(this.minimumCoord, this.maximumCoord))
			{
				int minChunkX = minimumCoord.getX() >> 4;
				int minChunkZ = minimumCoord.getZ() >> 4;
				int maxChunkX = maximumCoord.getX() >> 4;
				int maxChunkZ = maximumCoord.getZ() >> 4;

				for (int x = minChunkX; x <= maxChunkX; x++)
				{
					for (int z = minChunkZ; z <= maxChunkZ; z++)
					{
						LevelChunk chunkToSave = this.worldObj.getChunk(x, z);
						chunkToSave.markUnsaved();
					}
				}
			}
		}
	}

	protected abstract boolean updateServer();

	protected abstract void updateClient();

	protected void isBlockGoodForFrame(Level world, int x, int y, int z) throws MultiblockValidationException
	{
		throw new MultiblockValidationException(String.format("%d, %d, %d - Block is not valid for use in the machine's interior", x, y, z));
	}

	protected void isBlockGoodForTop(Level world, int x, int y, int z) throws MultiblockValidationException
	{
		throw new MultiblockValidationException(String.format("%d, %d, %d - Block is not valid for use in the machine's interior", x, y, z));
	}

	protected void isBlockGoodForBottom(Level world, int x, int y, int z) throws MultiblockValidationException
	{
		throw new MultiblockValidationException(String.format("%d, %d, %d - Block is not valid for use in the machine's interior", x, y, z));
	}

	protected void isBlockGoodForSides(Level world, int x, int y, int z) throws MultiblockValidationException
	{
		throw new MultiblockValidationException(String.format("%d, %d, %d - Block is not valid for use in the machine's interior", x, y, z));
	}

	protected void isBlockGoodForInterior(Level world, int x, int y, int z) throws MultiblockValidationException
	{
		throw new MultiblockValidationException(String.format("%d, %d, %d - Block is not valid for use in the machine's interior", x, y, z));
	}

	public BlockPos getReferenceCoord()
	{
		if (referenceCoord == null)
		{
			selectNewReferenceCoord();
		}
		return referenceCoord;
	}

	public int getNumConnectedBlocks() {
		return connectedParts.size();
	}

	public abstract void write(CompoundTag data);

	public abstract void read(CompoundTag data);

	public void recalculateMinMaxCoords()
	{
		Integer minX, minY, minZ;
		Integer maxX, maxY, maxZ;
		minX = minY = minZ = Integer.MAX_VALUE;
		maxX = maxY = maxZ = Integer.MIN_VALUE;

		for (IMultiblockPart part : connectedParts)
		{
			BlockPos pos = part.getBlockPos();
			if (pos.getX() < minX)
			{
				minX = pos.getX();
			}
			if (pos.getX() > maxX)
			{
				maxX = pos.getX();
			}
			if (pos.getY() < minY)
			{
				minY = pos.getY();
			}
			if (pos.getY() > maxY)
			{
				maxY = pos.getY();
			}
			if (pos.getZ() < minZ)
			{
				minZ = pos.getZ();
			}
			if (pos.getZ() > maxZ)
			{
				maxZ = pos.getZ();
			}
		}
		this.minimumCoord = new BlockPos(minX, minY, minZ);
		this.maximumCoord = new BlockPos(maxX, maxY, maxZ);
	}

	public BlockPos getMinimumCoord()
	{
		if (minimumCoord == null)
		{
			recalculateMinMaxCoords();
		}
		return minimumCoord;
	}

	public BlockPos getMaximumCoord()
	{
		if (maximumCoord == null)
		{
			recalculateMinMaxCoords();
		}
		return maximumCoord;
	}

	public abstract void formatDescriptionPacket(CompoundTag data);

	public abstract void decodeDescriptionPacket(CompoundTag data);

	public boolean isEmpty() {
		return connectedParts.isEmpty();
	}

	public boolean shouldConsume(MultiblockControllerBase otherController)
	{
		if (!otherController.getClass().equals(getClass()))
		{
			throw new IllegalArgumentException("Attempting to merge two multiblocks with different master classes - this should never happen!");
		}

		if (otherController == this)
		{
			return false;
		}

		int res = _shouldConsume(otherController);
		if (res < 0)
		{
			return true;
		}
		else if (res > 0)
		{
			return false;
		}
		else
		{
			System.out.println(String.format("[%s] Encountered two controllers with the same reference coordinate. Auditing connected parts and retrying.", worldObj.isClientSide ? "CLIENT" : "SERVER"));
			auditParts();
			otherController.auditParts();

			res = _shouldConsume(otherController);
			if (res < 0)
			{
				return true;
			}
			else if (res > 0)
			{
				return false;
			}
			else
			{
				System.out.println(String.format("My Controller (%d): size (%d), parts: %s", hashCode(), connectedParts.size(), getPartsListString()));
				System.out.println(String.format("Other Controller (%d): size (%d), coords: %s", otherController.hashCode(), otherController.connectedParts.size(), otherController.getPartsListString()));
				throw new IllegalArgumentException("[" + (worldObj.isClientSide ? "CLIENT" : "SERVER") + "] Two controllers with the same reference coord that somehow both have valid parts - this should never happen!");
			}
		}
	}

	private int _shouldConsume(MultiblockControllerBase otherController)
	{
		BlockPos myCoord = getReferenceCoord();
		BlockPos theirCoord = otherController.getReferenceCoord();
		if (theirCoord == null)
		{
			return -1;
		}
		else
		{
			return myCoord.compareTo(theirCoord);
		}
	}

	private String getPartsListString()
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (IMultiblockPart part : connectedParts)
		{
			if (!first)
			{
				sb.append(", ");
			}
			sb.append(String.format("(%d: %d, %d, %d)", part.hashCode(), part.getBlockPos().getX(), part.getBlockPos().getY(), part.getBlockPos().getZ()));
			first = false;
		}
		return sb.toString();
	}

	private void auditParts()
	{
		HashSet<IMultiblockPart> deadParts = new HashSet<IMultiblockPart>();
		for (IMultiblockPart part : connectedParts)
		{
			if (part.isInvalid() || worldObj.getBlockEntity(part.getBlockPos()) != part)
			{
				onDetachBlock(part);
				deadParts.add(part);
			}
		}

		connectedParts.removeAll(deadParts);
		System.out.println(String.format("[%s] Controller found %d dead parts during an audit, %d parts remain attached", worldObj.isClientSide ? "CLIENT" : "SERVER", deadParts.size(), connectedParts.size()));
	}

	public Set<IMultiblockPart> checkForDisconnections()
	{
		if (!this.shouldCheckForDisconnections)
		{
			return null;
		}
		if (this.isEmpty())
		{
			MultiblockRegistry.addDeadController(worldObj, this);
			return null;
		}
		referenceCoord = null;

		Set<IMultiblockPart> deadParts = new HashSet<IMultiblockPart>();
		BlockPos pos;
		IMultiblockPart referencePart = null;

		int originalSize = connectedParts.size();

		for (IMultiblockPart part : connectedParts)
		{
			pos = part.getWorldLocation();
			if (!this.worldObj.isLoaded(pos) || part.isInvalid())
			{
				deadParts.add(part);
				onDetachBlock(part);
				continue;
			}

			if (worldObj.getBlockEntity(pos) != part)
			{
				deadParts.add(part);
				onDetachBlock(part);
				continue;
			}

			part.setUnvisited();
			part.forfeitMultiblockSaveDelegate();

			if (referenceCoord == null)
			{
				referenceCoord = pos;
				referencePart = part;
			}
			else if (pos.compareTo(referenceCoord) < 0)
			{
				referenceCoord = pos;
				referencePart = part;
			}
		}
		connectedParts.removeAll(deadParts);
		deadParts.clear();

		if (referencePart == null || isEmpty())
		{
			shouldCheckForDisconnections = false;
			MultiblockRegistry.addDeadController(worldObj, this);
			return null;
		} else
		{
			referencePart.becomeMultiblockSaveDelegate();
		}

		IMultiblockPart part;
		LinkedList<IMultiblockPart> partsToCheck = new LinkedList<IMultiblockPart>();
		IMultiblockPart[] nearbyParts = null;
		int visitedParts = 0;

		partsToCheck.add(referencePart);

		while (!partsToCheck.isEmpty())
		{
			part = partsToCheck.removeFirst();
			part.setVisited();
			visitedParts++;
			nearbyParts = part.getNeighboringParts();
			for (IMultiblockPart nearbyPart : nearbyParts)
			{
				if (nearbyPart.getMultiblockController() != this)
				{
					continue;
				}

				if (!nearbyPart.isVisited())
				{
					nearbyPart.setVisited();
					partsToCheck.add(nearbyPart);
				}
			}
		}

		Set<IMultiblockPart> removedParts = new HashSet<IMultiblockPart>();
		for (IMultiblockPart orphanCandidate : connectedParts)
		{
			if (!orphanCandidate.isVisited())
			{
				deadParts.add(orphanCandidate);
				orphanCandidate.onOrphaned(this, originalSize, visitedParts);
				onDetachBlock(orphanCandidate);
				removedParts.add(orphanCandidate);
			}
		}
		connectedParts.removeAll(deadParts);
		deadParts.clear();
		if (referenceCoord == null)
		{
			selectNewReferenceCoord();
		}

		shouldCheckForDisconnections = false;
		return removedParts;
	}

	public Set<IMultiblockPart> detachAllBlocks()
	{
		if (worldObj == null)
		{
			return new HashSet<IMultiblockPart>();
		}

		for (IMultiblockPart part : connectedParts)
		{
			if (this.worldObj.isLoaded(part.getWorldLocation()))
			{
				onDetachBlock(part);
			}
		}
		Set<IMultiblockPart> detachedParts = connectedParts;
		connectedParts = new HashSet<IMultiblockPart>();
		return detachedParts;
	}

	public boolean isAssembled() {
		return this.assemblyState == AssemblyState.Assembled;
	}

	private void selectNewReferenceCoord()
	{
		IMultiblockPart theChosenOne = null;
		BlockPos pos;
		referenceCoord = null;

		for (IMultiblockPart part : connectedParts)
		{
			pos = part.getWorldLocation();
			if (part.isInvalid() || !this.worldObj.isLoaded(pos))
			{
				continue;
			}
			if (referenceCoord == null || referenceCoord.compareTo(pos) > 0)
			{
				referenceCoord = pos;
				theChosenOne = part;
			}
		}
		if (theChosenOne != null)
		{
			theChosenOne.becomeMultiblockSaveDelegate();
		}
	}

	protected void markReferenceCoordForUpdate()
	{
		BlockPos rc = getReferenceCoord();
		if (worldObj != null && rc != null)
		{
			BlockState state = worldObj.getBlockState(rc);
			worldObj.sendBlockUpdated(rc, state, state, 3);
		}
	}

	protected void markReferenceCoordDirty()
	{
		if (worldObj == null || worldObj.isClientSide)
		{
			return;
		}
		BlockPos referenceCoord = getReferenceCoord();
		if (referenceCoord == null)
		{
			return;
		}
	}
}
