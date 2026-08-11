package net.creeperhost.wyml.blocks;

import net.creeperhost.polylib.platform.Services;
import net.creeperhost.wyml.tiles.TilePaperBag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockPaperBag extends Block implements EntityBlock
{
    public BlockPaperBag(Properties properties)
    {
        super(properties.strength(2.0F).noOcclusion());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (player instanceof ServerPlayer serverPlayer && blockEntity instanceof TilePaperBag paperBag)
        {
            paperBag.resetDespawnTime();
            Services.REGISTER_HELPER.openMenu(serverPlayer, (MenuProvider) blockEntity, buffer -> buffer.writeBlockPos(pos));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston)
    {
        if (level.getBlockEntity(pos) instanceof TilePaperBag paperBag)
        {
            Containers.dropContents(level, pos, paperBag.getInventory());
        }
        Containers.updateNeighboursAfterDestroy(state, level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return Block.box(2.0D, 0.0D, 5.0D, 14.0D, 15.0D, 11.0D);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TilePaperBag(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return (tickLevel, pos, tickState, blockEntity) ->
        {
            if (blockEntity instanceof TilePaperBag paperBag)
            {
                paperBag.tick();
            }
        };
    }
}
