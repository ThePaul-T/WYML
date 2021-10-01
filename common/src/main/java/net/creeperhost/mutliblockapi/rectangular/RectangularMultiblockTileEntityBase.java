package net.creeperhost.mutliblockapi.rectangular;

import net.creeperhost.mutliblockapi.MultiblockControllerBase;
import net.creeperhost.mutliblockapi.MultiblockTileEntityBase;
import net.creeperhost.mutliblockapi.MultiblockValidationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class RectangularMultiblockTileEntityBase extends MultiblockTileEntityBase
{
	PartPosition position;
	Direction outwards;

	public RectangularMultiblockTileEntityBase(BlockEntityType<?> tileEntityType)
	{
		super(tileEntityType);
		position = PartPosition.Unknown;
		outwards = null;
	}

	public Direction getOutwardsDir() {
		return outwards;
	}

	public PartPosition getPartPosition() {
		return position;
	}

	@Override
	public void onAttached(MultiblockControllerBase newController)
	{
		super.onAttached(newController);
		recalculateOutwardsDirection(newController.getMinimumCoord(), newController.getMaximumCoord());
	}

	@Override
	public void onMachineAssembled(MultiblockControllerBase controller)
	{
		BlockPos maxCoord = controller.getMaximumCoord();
		BlockPos minCoord = controller.getMinimumCoord();
		recalculateOutwardsDirection(minCoord, maxCoord);
	}

	@Override
	public void onMachineBroken()
	{
		position = PartPosition.Unknown;
		outwards = null;
	}

	public void recalculateOutwardsDirection(BlockPos minCoord, BlockPos maxCoord)
	{
		outwards = null;
		position = PartPosition.Unknown;
		int facesMatching = 0;
		if (maxCoord.getX() == this.getBlockPos().getX() || minCoord.getX() == this.getBlockPos().getX())
		{
			facesMatching++;
		}
		if (maxCoord.getY() == this.getBlockPos().getY() || minCoord.getY() == this.getBlockPos().getY())
		{
			facesMatching++;
		}
		if (maxCoord.getZ() == this.getBlockPos().getZ() || minCoord.getZ() == this.getBlockPos().getZ())
		{
			facesMatching++;
		}
		if (facesMatching <= 0)
		{
			position = PartPosition.Interior;
		}
		else if (facesMatching >= 3)
		{
			position = PartPosition.FrameCorner;
		}
		else if (facesMatching == 2)
		{
			position = PartPosition.Frame;
		}
		else
		{
			if (maxCoord.getX() == this.getBlockPos().getX())
			{
				position = PartPosition.EastFace;
				outwards = Direction.EAST;
			}
			else if (minCoord.getX() == this.getBlockPos().getX())
			{
				position = PartPosition.WestFace;
				outwards = Direction.WEST;
			}
			else if (maxCoord.getZ() == this.getBlockPos().getZ())
			{
				position = PartPosition.SouthFace;
				outwards = Direction.SOUTH;
			}
			else if (minCoord.getZ() == this.getBlockPos().getZ())
			{
				position = PartPosition.NorthFace;
				outwards = Direction.NORTH;
			}
			else if (maxCoord.getY() == this.getBlockPos().getY())
			{
				position = PartPosition.TopFace;
				outwards = Direction.UP;
			}
			else
			{
				position = PartPosition.BottomFace;
				outwards = Direction.DOWN;
			}
		}
	}

	public abstract void isGoodForFrame() throws MultiblockValidationException;

	public abstract void isGoodForSides() throws MultiblockValidationException;

	public abstract void isGoodForTop() throws MultiblockValidationException;

	public abstract void isGoodForBottom() throws MultiblockValidationException;

	public abstract void isGoodForInterior() throws MultiblockValidationException;
}
