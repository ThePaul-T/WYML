package net.creeperhost.mutliblockapi.rectangular;

import net.creeperhost.mutliblockapi.MultiblockControllerBase;
import net.creeperhost.mutliblockapi.MultiblockValidationException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class RectangularMultiblockControllerBase extends MultiblockControllerBase
{
	protected RectangularMultiblockControllerBase(Level world) {
		super(world);
	}

	@Override
	public void isMachineWhole() throws MultiblockValidationException
	{
		if (connectedParts.size() < getMinimumNumberOfBlocksForAssembledMachine())
		{
			throw new MultiblockValidationException("Machine is too small. Current: " + connectedParts + " Min: " + getMinimumNumberOfBlocksForAssembledMachine());
		}

		BlockPos maximumCoord = getMaximumCoord();
		BlockPos minimumCoord = getMinimumCoord();

		// Quickly check for exceeded dimensions
		int deltaX = maximumCoord.getX() - minimumCoord.getX() + 1;
		int deltaY = maximumCoord.getY() - minimumCoord.getY() + 1;
		int deltaZ = maximumCoord.getZ() - minimumCoord.getZ() + 1;

		int maxX = getMaximumXSize();
		int maxY = getMaximumYSize();
		int maxZ = getMaximumZSize();
		int minX = getMinimumXSize();
		int minY = getMinimumYSize();
		int minZ = getMinimumZSize();

		if (maxX > 0 && deltaX > maxX)
		{
			throw new MultiblockValidationException(String.format("Machine is too large, it may be at most %d blocks in the X dimension", maxX));
		}
		if (maxY > 0 && deltaY > maxY)
		{
			throw new MultiblockValidationException(String.format("Machine is too large, it may be at most %d blocks in the Y dimension", maxY));
		}
		if (maxZ > 0 && deltaZ > maxZ)
		{
			throw new MultiblockValidationException(String.format("Machine is too large, it may be at most %d blocks in the Z dimension", maxZ));
		}
		if (deltaX < minX)
		{
			throw new MultiblockValidationException(String.format("Machine is too small, it must be at least %d blocks in the X dimension", minX));
		}
		if (deltaY < minY)
		{
			throw new MultiblockValidationException(String.format("Machine is too small, it must be at least %d blocks in the Y dimension", minY));
		}
		if (deltaZ < minZ)
		{
			throw new MultiblockValidationException(String.format("Machine is too small, it must be at least %d blocks in the Z dimension", minZ));
		}

		BlockEntity te;
		RectangularMultiblockTileEntityBase part;
		Class<? extends RectangularMultiblockControllerBase> myClass = this.getClass();

		for (int x = minimumCoord.getX(); x <= maximumCoord.getX(); x++)
		{
			for (int y = minimumCoord.getY(); y <= maximumCoord.getY(); y++)
			{
				for (int z = minimumCoord.getZ(); z <= maximumCoord.getZ(); z++)
				{
					te = this.worldObj.getBlockEntity(new BlockPos(x, y, z));
					if (te instanceof RectangularMultiblockTileEntityBase)
					{
						part = (RectangularMultiblockTileEntityBase) te;
						if (!myClass.equals(part.getMultiblockControllerType()))
						{
							throw new MultiblockValidationException(String.format("Part @ %d, %d, %d is incompatible with machines of type %s", x, y, z, myClass.getSimpleName()));
						}
					}
					else
					{
						part = null;
					}
					int extremes = 0;
					if (x == minimumCoord.getX())
					{
						extremes++;
					}
					if (y == minimumCoord.getY())
					{
						extremes++;
					}
					if (z == minimumCoord.getZ())
					{
						extremes++;
					}

					if (x == maximumCoord.getX())
					{
						extremes++;
					}
					if (y == maximumCoord.getY())
					{
						extremes++;
					}
					if (z == maximumCoord.getZ())
					{
						extremes++;
					}

					if (extremes >= 2)
					{
						if (part != null)
						{
							part.isGoodForFrame();
						}
						else
						{
							isBlockGoodForFrame(this.worldObj, x, y, z);
						}
					}
					else if (extremes == 1)
					{
						if (y == maximumCoord.getY())
						{
							if (part != null)
							{
								part.isGoodForTop();
							}
							else
							{
								isBlockGoodForTop(this.worldObj, x, y, z);
							}
						}
						else if (y == minimumCoord.getY())
						{
							if (part != null)
							{
								part.isGoodForBottom();
							}
							else
							{
								isBlockGoodForBottom(this.worldObj, x, y, z);
							}
						}
						else
						{
							if (part != null)
							{
								part.isGoodForSides();
							}
							else
							{
								isBlockGoodForSides(this.worldObj, x, y, z);
							}
						}
					}
					else
					{
						if (part != null)
						{
							part.isGoodForInterior();
						}
						else
						{
							isBlockGoodForInterior(this.worldObj, x, y, z);
						}
					}
				}
			}
		}
	}
}
