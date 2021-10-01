package net.creeperhost.mutliblockapi;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class MultiblockWorldRegistry
{
	private Level worldObj;

	private Set<MultiblockControllerBase> controllers;
	private Set<MultiblockControllerBase> dirtyControllers;
	private Set<MultiblockControllerBase> deadControllers;
	private Set<IMultiblockPart> orphanedParts;
	private Set<IMultiblockPart> detachedParts;
	private HashMap<Integer, Set<IMultiblockPart>> partsAwaitingChunkLoad;
	private final Object partsAwaitingChunkLoadMutex;
	private Object orphanedPartsMutex;

	public MultiblockWorldRegistry(final Level world) {
		worldObj = world;

		controllers = new HashSet<MultiblockControllerBase>();
		deadControllers = new HashSet<MultiblockControllerBase>();
		dirtyControllers = new HashSet<MultiblockControllerBase>();

		detachedParts = new HashSet<IMultiblockPart>();
		orphanedParts = new HashSet<IMultiblockPart>();

		partsAwaitingChunkLoad = new HashMap<Integer, Set<IMultiblockPart>>();
		partsAwaitingChunkLoadMutex = new Object();
		orphanedPartsMutex = new Object();
	}

	public void tickStart()
	{
		if (controllers.size() > 0)
		{
			for (MultiblockControllerBase controller : controllers)
			{
				if (controller.worldObj == worldObj && controller.worldObj.isClientSide == worldObj.isClientSide)
				{
					if (controller.isEmpty())
					{
						deadControllers.add(controller);
					} else
					{
						controller.updateMultiblockEntity();
					}
				}
			}
		}
	}

	public void processMultiblockChanges()
	{
		BlockPos coord;

		List<Set<MultiblockControllerBase>> mergePools = null;
		if (orphanedParts.size() > 0)
		{
			Set<IMultiblockPart> orphansToProcess = null;
			synchronized (orphanedPartsMutex)
			{
				if (orphanedParts.size() > 0)
				{
					orphansToProcess = orphanedParts;
					orphanedParts = new HashSet<IMultiblockPart>();
				}
			}

			if (orphansToProcess != null && orphansToProcess.size() > 0)
			{
				Set<MultiblockControllerBase> compatibleControllers;
				for (IMultiblockPart orphan : orphansToProcess)
				{
					coord = orphan.getWorldLocation();
					if (!this.worldObj.isLoaded(coord))
					{
						continue;
					}

					if (worldObj.getBlockEntity(coord) != orphan)
					{
						continue;
					}
					compatibleControllers = orphan.attachToNeighbors();
					if (compatibleControllers == null)
					{
						MultiblockControllerBase newController = orphan.createNewMultiblock();
						newController.attachBlock(orphan);
						this.controllers.add(newController);
					}
					else if (compatibleControllers.size() > 1)
					{
						if (mergePools == null)
						{
							mergePools = new ArrayList<Set<MultiblockControllerBase>>();
						}
						List<Set<MultiblockControllerBase>> candidatePools = new ArrayList<Set<MultiblockControllerBase>>();
						for (Set<MultiblockControllerBase> candidatePool : mergePools)
						{
							if (!Collections.disjoint(candidatePool, compatibleControllers))
							{
								candidatePools.add(candidatePool);
							}
						}

						if (candidatePools.size() <= 0)
						{
							mergePools.add(compatibleControllers);
						}
						else if (candidatePools.size() == 1)
						{
							candidatePools.get(0).addAll(compatibleControllers);
						}
						else
						{
							Set<MultiblockControllerBase> masterPool = candidatePools.get(0);
							Set<MultiblockControllerBase> consumedPool;
							for (int i = 1; i < candidatePools.size(); i++)
							{
								consumedPool = candidatePools.get(i);
								masterPool.addAll(consumedPool);
								mergePools.remove(consumedPool);
							}
							masterPool.addAll(compatibleControllers);
						}
					}
				}
			}
		}

		if (mergePools != null && mergePools.size() > 0)
		{
			for (Set<MultiblockControllerBase> mergePool : mergePools)
			{
				MultiblockControllerBase newMaster = null;
				for (MultiblockControllerBase controller : mergePool)
				{
					if (newMaster == null || controller.shouldConsume(newMaster))
					{
						newMaster = controller;
					}
				}
				if (newMaster == null)
				{
					System.out.println(String.format("Multiblock system checked a merge pool of size %d, found no master candidates. This should never happen.",
									mergePool.size()));
				}
				else
				{
					addDirtyController(newMaster);
					for (MultiblockControllerBase controller : mergePool)
					{
						if (controller != newMaster)
						{
							newMaster.assimilate(controller);
							addDeadController(controller);
							addDirtyController(newMaster);
						}
					}
				}
			}
		}

		if (dirtyControllers.size() > 0)
		{
			Set<IMultiblockPart> newlyDetachedParts = null;
			for (MultiblockControllerBase controller : dirtyControllers)
			{
				newlyDetachedParts = controller.checkForDisconnections();
				if (!controller.isEmpty())
				{
					controller.recalculateMinMaxCoords();
					controller.checkIfMachineIsWhole();
				} else
				{
					addDeadController(controller);
				}
				if (newlyDetachedParts != null && newlyDetachedParts.size() > 0)
				{
					detachedParts.addAll(newlyDetachedParts);
				}
			}
			dirtyControllers.clear();
		}

		if (deadControllers.size() > 0)
		{
			for (MultiblockControllerBase controller : deadControllers)
			{
				if (!controller.isEmpty())
				{
					detachedParts.addAll(controller.detachAllBlocks());
				}
				this.controllers.remove(controller);
			}
			deadControllers.clear();
		}
		for (IMultiblockPart part : detachedParts)
		{
			part.assertDetached();
		}
		addAllOrphanedPartsThreadsafe(detachedParts);
		detachedParts.clear();
	}

	public void onPartAdded(IMultiblockPart part)
	{
		BlockPos pos = part.getWorldLocation();

		if (!this.worldObj.isLoaded(pos))
		{
			Set<IMultiblockPart> partSet;
			int chunkHash = new ChunkPos(pos).hashCode();

			synchronized (partsAwaitingChunkLoadMutex)
			{
				if (!partsAwaitingChunkLoad.containsKey(chunkHash))
				{
					partSet = new HashSet<IMultiblockPart>();
					partsAwaitingChunkLoad.put(chunkHash, partSet);
				}
				else
				{
					partSet = partsAwaitingChunkLoad.get(chunkHash);
				}
				partSet.add(part);
			}
		}
		else
		{
			addOrphanedPartThreadsafe(part);
		}
	}

	public void onPartRemovedFromWorld(IMultiblockPart part)
	{
		BlockPos pos = part.getWorldLocation();
		if (pos != null)
		{
			int chunkHash = new ChunkPos(pos).hashCode();

			if (partsAwaitingChunkLoad.containsKey(chunkHash))
			{
				synchronized (partsAwaitingChunkLoadMutex)
				{
					if (partsAwaitingChunkLoad.containsKey(chunkHash))
					{
						partsAwaitingChunkLoad.get(chunkHash).remove(part);
						if (partsAwaitingChunkLoad.get(chunkHash).size() <= 0)
						{
							partsAwaitingChunkLoad.remove(chunkHash);
						}
					}
				}
			}
		}

		detachedParts.remove(part);
		if (orphanedParts.contains(part))
		{
			synchronized (orphanedPartsMutex)
			{
				orphanedParts.remove(part);
			}
		}
		part.assertDetached();
	}

	public void onWorldUnloaded()
	{
		controllers.clear();
		deadControllers.clear();
		dirtyControllers.clear();
		detachedParts.clear();

		synchronized (partsAwaitingChunkLoadMutex)
		{
			partsAwaitingChunkLoad.clear();
		}

		synchronized (orphanedPartsMutex)
		{
			orphanedParts.clear();
		}

		worldObj = null;
	}

	public void onChunkLoaded(LevelChunk chunk)
	{
		int chunkHash = chunk.getPos().hashCode();
		if (partsAwaitingChunkLoad.containsKey(chunkHash))
		{
			synchronized (partsAwaitingChunkLoadMutex)
			{
				if (partsAwaitingChunkLoad.containsKey(chunkHash))
				{
					addAllOrphanedPartsThreadsafe(partsAwaitingChunkLoad.get(chunkHash));
					partsAwaitingChunkLoad.remove(chunkHash);
				}
			}
		}
	}

	public void addDeadController(MultiblockControllerBase deadController)
	{
		this.deadControllers.add(deadController);
	}

	public void addDirtyController(MultiblockControllerBase dirtyController)
	{
		this.dirtyControllers.add(dirtyController);
	}

	public Set<MultiblockControllerBase> getControllers()
	{
		return Collections.unmodifiableSet(controllers);
	}

	private void addOrphanedPartThreadsafe(IMultiblockPart part)
	{
		synchronized (orphanedPartsMutex)
		{
			orphanedParts.add(part);
		}
	}

	private void addAllOrphanedPartsThreadsafe(Collection<? extends IMultiblockPart> parts)
	{
		synchronized (orphanedPartsMutex)
		{
			orphanedParts.addAll(parts);
		}
	}
}
