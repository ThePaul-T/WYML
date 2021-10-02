package net.creeperhost.wyml.blocks;

import net.creeperhost.wyml.tiles.TileMultiBlockFenceGate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockMultiBlockFenceGate extends BaseEntityBlock
{
    public static final BooleanProperty OPEN;
    public static final BooleanProperty POWERED;
    public static final BooleanProperty IN_WALL;
    protected static final VoxelShape Z_SHAPE;
    protected static final VoxelShape X_SHAPE;
    protected static final VoxelShape Z_SHAPE_LOW;
    protected static final VoxelShape X_SHAPE_LOW;
    protected static final VoxelShape Z_COLLISION_SHAPE;
    protected static final VoxelShape X_COLLISION_SHAPE;
    protected static final VoxelShape Z_OCCLUSION_SHAPE;
    protected static final VoxelShape X_OCCLUSION_SHAPE;
    protected static final VoxelShape Z_OCCLUSION_SHAPE_LOW;
    protected static final VoxelShape X_OCCLUSION_SHAPE_LOW;
    public static final DirectionProperty FACING;

    public BlockMultiBlockFenceGate()
    {
        super(Properties.of(Material.WOOD).noOcclusion().strength(2.0F));
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(OPEN, false)).setValue(POWERED, false)).setValue(IN_WALL, false));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext)
    {
        if ((Boolean)blockState.getValue(IN_WALL)) {
            return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_SHAPE_LOW : Z_SHAPE_LOW;
        } else {
            return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2)
    {
        Direction.Axis axis = direction.getAxis();
        if (((Direction)blockState.getValue(FACING)).getClockWise().getAxis() != axis) {
            return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
        } else {
            boolean bl = this.isWall(blockState2) || this.isWall(levelAccessor.getBlockState(blockPos.relative(direction.getOpposite())));
            return (BlockState)blockState.setValue(IN_WALL, bl);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext)
    {
        if ((Boolean)blockState.getValue(OPEN)) {
            return Shapes.empty();
        } else {
            return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
        }
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos)
    {
        if ((Boolean)blockState.getValue(IN_WALL)) {
            return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE_LOW : Z_OCCLUSION_SHAPE_LOW;
        } else {
            return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE : Z_OCCLUSION_SHAPE;
        }
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType)
    {
        switch(pathComputationType) {
            case LAND:
                return (Boolean)blockState.getValue(OPEN);
            case WATER:
                return false;
            case AIR:
                return (Boolean)blockState.getValue(OPEN);
            default:
                return false;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext)
    {
        Level level = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        boolean bl = level.hasNeighborSignal(blockPos);
        Direction direction = blockPlaceContext.getHorizontalDirection();
        Direction.Axis axis = direction.getAxis();
        boolean bl2 = axis == Direction.Axis.Z && (this.isWall(level.getBlockState(blockPos.west())) || this.isWall(level.getBlockState(blockPos.east()))) || axis == Direction.Axis.X && (this.isWall(level.getBlockState(blockPos.north())) || this.isWall(level.getBlockState(blockPos.south())));
        return (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction)).setValue(OPEN, bl)).setValue(POWERED, bl)).setValue(IN_WALL, bl2);
    }

    private boolean isWall(BlockState blockState) {
        return blockState.getBlock().is(BlockTags.WALLS);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult)
    {
//        if(level.isClientSide()) return InteractionResult.SUCCESS;

        TileMultiBlockFenceGate tileMultiBlockFenceGate = (TileMultiBlockFenceGate) level.getBlockEntity(blockPos);
        tileMultiBlockFenceGate.isWalking = false;
        tileMultiBlockFenceGate.connectedBlocks.clear();
        tileMultiBlockFenceGate.walkFence();

//        if (blockState.getValue(OPEN)) {
//            blockState = (BlockState)blockState.setValue(OPEN, false);
//            level.setBlock(blockPos, blockState, 10);
//        } else {
//            Direction direction = player.getDirection();
//            if (blockState.getValue(FACING) == direction.getOpposite()) {
//                blockState = (BlockState)blockState.setValue(FACING, direction);
//            }
//
//            blockState = (BlockState)blockState.setValue(OPEN, true);
//            level.setBlock(blockPos, blockState, 10);
//        }
//
//        level.levelEvent(player, (Boolean)blockState.getValue(OPEN) ? 1008 : 1014, blockPos, 0);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (!level.isClientSide) {
            boolean bl2 = level.hasNeighborSignal(blockPos);
            if ((Boolean)blockState.getValue(POWERED) != bl2) {
                level.setBlock(blockPos, (BlockState)((BlockState)blockState.setValue(POWERED, bl2)).setValue(OPEN, bl2), 2);
                if ((Boolean)blockState.getValue(OPEN) != bl2) {
                    level.levelEvent((Player)null, bl2 ? 1008 : 1014, blockPos, 0);
                }
            }

        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, OPEN, POWERED, IN_WALL});
    }

    public static boolean connectsToDirection(BlockState blockState, Direction direction) {
        return ((Direction)blockState.getValue(FACING)).getAxis() == direction.getClockWise().getAxis();
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState)
    {
        return RenderShape.MODEL;
    }

    static {
        OPEN = BlockStateProperties.OPEN;
        POWERED = BlockStateProperties.POWERED;
        IN_WALL = BlockStateProperties.IN_WALL;
        Z_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
        X_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
        Z_SHAPE_LOW = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 13.0D, 10.0D);
        X_SHAPE_LOW = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 13.0D, 16.0D);
        Z_COLLISION_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 24.0D, 10.0D);
        X_COLLISION_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 24.0D, 16.0D);
        Z_OCCLUSION_SHAPE = Shapes.or(Block.box(0.0D, 5.0D, 7.0D, 2.0D, 16.0D, 9.0D), Block.box(14.0D, 5.0D, 7.0D, 16.0D, 16.0D, 9.0D));
        X_OCCLUSION_SHAPE = Shapes.or(Block.box(7.0D, 5.0D, 0.0D, 9.0D, 16.0D, 2.0D), Block.box(7.0D, 5.0D, 14.0D, 9.0D, 16.0D, 16.0D));
        Z_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(0.0D, 2.0D, 7.0D, 2.0D, 13.0D, 9.0D), Block.box(14.0D, 2.0D, 7.0D, 16.0D, 13.0D, 9.0D));
        X_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(7.0D, 2.0D, 0.0D, 9.0D, 13.0D, 2.0D), Block.box(7.0D, 2.0D, 14.0D, 9.0D, 13.0D, 16.0D));
        FACING = BlockStateProperties.HORIZONTAL_FACING;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockGetter blockGetter)
    {
        return new TileMultiBlockFenceGate();
    }
}
