package net.creeperhost.wyml.tiles;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.blocks.BlockMultiBlockFenceGate;
import net.creeperhost.wyml.containers.ContainerFence;
import net.creeperhost.wyml.data.BlockTurn;
import net.creeperhost.wyml.data.FencePart;
import net.creeperhost.wyml.init.WYMLBlocks;
import net.creeperhost.wyml.network.MessageUpdateFence;
import net.creeperhost.wyml.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.time.Instant;
import java.util.*;
import java.util.List;

public class TileMultiBlockFenceGate extends BaseContainerBlockEntity implements TickableBlockEntity
{
    public Map<BlockPos, FencePart> CONNECTED_BLOCKS = new HashMap<>();
    public List<BlockPos> DIRTY_BLOCKS = new ArrayList<>();
    public List<BlockPos> INTERNAL_BLOCKS = new ArrayList<>();
    public List<EntityType<?>> STORED_ENTITIES = new ArrayList<>();
    public int STORED_ENTITY_COUNT = 0;
    public long LAST_UPDATED_TIME = -1;
    public boolean IS_WALKING = false;
    public boolean IS_ASSEMBLED = false;
    //TODO config this value
    public long CHECK_TIME = 20;
    public int MAX_SCAN_SIZE = 20;

    public TileMultiBlockFenceGate()
    {
        super(WYMLBlocks.FENCE_GATE_TILE.get());
    }

    //Don't ask me how this works I have no idea anymore
    public void walkFence()
    {
        if(IS_WALKING) return;

        IS_WALKING = true;
        CONNECTED_BLOCKS.clear();
        DIRTY_BLOCKS.clear();
        IS_ASSEMBLED = false;

        Level level = getLevel();
        if(level == null) return;
        BlockPos gatePos = getBlockPos();
        BlockPos searchPos = gatePos;
        List<BlockTurn> blockTurnList = new ArrayList<>();
        int count = 1;

        while (IS_WALKING)
        {
            Direction direction = getNextDirection(level, searchPos, null);

            if(blockTurnList.isEmpty())
            {
                BlockTurn blockTurn = new BlockTurn(searchPos, direction);
                blockTurnList.add(blockTurn);
            }
            if(blockTurnList.get(blockTurnList.size() - 1).getDirection() != direction && direction != null)
            {
                BlockTurn blockTurn = new BlockTurn(searchPos, direction);
                blockTurnList.add(blockTurn);
            }

            if(direction == null)
            {
//                spawnParticle(level, searchPos, ParticleTypes.SMOKE);

                int i = blockTurnList.size() - count;
                if(i > 0 && blockTurnList.get(i) != null)
                {
                    BlockTurn blockTurn = blockTurnList.get(i);
                    DIRTY_BLOCKS.add(searchPos);

                    for (int j = 0; j < 5; j++)
                    {
                        BlockPos blockPos1 = blockTurn.getBlockPos().relative(blockTurn.getDirection(), j);
                        if(!blockPosMatches(blockTurn.getBlockPos(), blockPos1)) DIRTY_BLOCKS.add(blockPos1);
                    }

                    count++;
                    searchPos = blockTurn.getBlockPos();
                    direction = getNextDirection(level, searchPos, blockTurn.getDirection());

//                    if (!level.isClientSide) System.out.println("Attempting to turn " + direction + " blockTurnList " + blockTurnList.size() + " count " + count);
                }

                if(direction == null && count >= blockTurnList.size())
                {
                    IS_WALKING = false;
                    IS_ASSEMBLED = false;
//                    if(!level.isClientSide) System.out.println("direction is null, breaking loop");
                    break;
                }
            }

//            if(!level.isClientSide && direction != null) System.out.println(searchPos.relative(direction) + " Start: " + gatePos);

            if(direction != null && canConnect(level, searchPos.relative(direction)))
            {
                CONNECTED_BLOCKS.put(searchPos.relative(direction), new FencePart(level.getBlockState(searchPos.relative(direction)).getBlock(), searchPos.relative(direction), false));
                spawnParticle(level, searchPos.relative(direction), ParticleTypes.CRIT);
                if(blockPosMatches(searchPos.relative(direction), getBlockPos()))
                {
                    IS_ASSEMBLED = true;
                    IS_WALKING = false;
                    //Remove the old "dirty" blocks
                    for (BlockTurn blockTurn : blockTurnList)
                    {
                        CONNECTED_BLOCKS.put(blockTurn.getBlockPos(), new FencePart(level.getBlockState(blockTurn.getBlockPos()).getBlock(), blockTurn.getBlockPos(), true));
                    }
                    if(!DIRTY_BLOCKS.isEmpty())
                    {
                        for (BlockPos blockPos : DIRTY_BLOCKS)
                        {
                            CONNECTED_BLOCKS.remove(blockPos);
                        }
                    }
                    onAssembled();
//                    if(!level.isClientSide) System.out.println("Loop finished, We have found our gate again");
                    break;
                }
                //Reset the counter when connection works
                count = 1;
            }
            if(direction != null) searchPos = searchPos.relative(direction);
        }
    }

    public List<BlockPos> getCorners()
    {
        if(!IS_ASSEMBLED) return null;

        List<BlockPos> corners = new ArrayList<>();
        for (FencePart value : CONNECTED_BLOCKS.values())
        {
            if(value.isCorner() && value.getBlock() instanceof FenceBlock)
            {
                corners.add(value.getBlockPos());
            }
        }
        return corners;
    }

    public List<BlockPos> findOpFence(BlockPos blockPos, Direction direction, int maxValue)
    {
        List<BlockPos> list = new ArrayList<>();
        for (int i = 1; i < maxValue; i++)
        {
            BlockPos blockPos1 = blockPos.relative(direction, i);
            if(level.getBlockState(blockPos1).getBlock() instanceof FenceBlock || level.getBlockState(blockPos1).getBlock() instanceof BlockMultiBlockFenceGate) return list;
            list.add(blockPos1);
        }
        return new ArrayList<>();
    }

    public void rewalkFence()
    {
        INTERNAL_BLOCKS.clear();
        for (FencePart value : CONNECTED_BLOCKS.values())
        {
            BlockPos blockPos = value.getBlockPos();
            List<BlockPos> op = findOpFence(blockPos, Direction.EAST, MAX_SCAN_SIZE);
            if(!op.isEmpty())
            {
                //I would use addAll but that will also add objects that are already in the list
                for (BlockPos pos : op)
                {
                    if(!INTERNAL_BLOCKS.contains(pos))
                    {
                        INTERNAL_BLOCKS.add(pos);
                    }
                }
            }
        }
    }

    public boolean blockPosMatches(BlockPos blockPos1, BlockPos blockPos2)
    {
        return blockPos1.getX() == blockPos2.getX() && blockPos1.getY() == blockPos2.getY() && blockPos1.getZ() == blockPos2.getZ();
    }

    //Used for debugging (Don't remove)
    @SuppressWarnings("unused")
    public void spawnParticle(Level level, BlockPos blockPos, ParticleOptions particleOptions)
    {
        Random random = level.random;
        double d = (double)blockPos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
        double e = (double)blockPos.getY() + 1.1D + (random.nextDouble() - 0.5D) * 0.2D;
        double f = (double)blockPos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
        level.addParticle(particleOptions, d, e, f, 0.0D, 0.0D, 0.0D);
    }

    public Direction getNextDirection(Level level, BlockPos blockPos, Direction skip)
    {
        for(Direction direction : Direction.values())
        {
            if(canConnect(level, blockPos.relative(direction)))
            {
                if(direction != null && direction != skip) return direction;
            }
        }
        return null;
    }

    public boolean canConnect(Level level, BlockPos blockPos)
    {
        if(CONNECTED_BLOCKS.containsKey(blockPos)) return false;
        if(level.getBlockState(blockPos).getBlock() instanceof FenceBlock) return true;
        if(level.getBlockState(blockPos).getBlock() instanceof BlockMultiBlockFenceGate) return true;

        return false;
    }

    public void onAssembled()
    {
        WhyYouMakeLag.LOGGER.info("New fence MultiBlock created at " + getBlockPos());
        LAST_UPDATED_TIME = Instant.now().getEpochSecond();
        rewalkFence();
    }

    @Override
    public void tick()
    {
        tileFirstTick();;

        if(IS_ASSEMBLED)
        {
            //TODO debug, Remove
            if(!INTERNAL_BLOCKS.isEmpty())
            {
                for (BlockPos internal_block : INTERNAL_BLOCKS)
                {
                    spawnParticle(level, internal_block.below(), ParticleTypes.BUBBLE);
                }
            }
            if(Instant.now().getEpochSecond() > (LAST_UPDATED_TIME + CHECK_TIME))
            {
                if(!stillValid())
                {
                    walkFence();
                    LAST_UPDATED_TIME = Instant.now().getEpochSecond();
                }
            }
        }
    }

    public void updateStoredEntities()
    {

    }

    public void addEntity(EntityType<?> entityType)
    {
        STORED_ENTITIES.add(entityType);
        setStoredEntityCount(STORED_ENTITIES.size());
    }

    public void removeEntity(EntityType<?> entityType)
    {
        STORED_ENTITIES.remove(entityType);
        setStoredEntityCount(STORED_ENTITIES.size());
    }

    public int getStoredEntityCount()
    {
        return STORED_ENTITY_COUNT;
    }

    public void setStoredEntityCount(int STORED_ENTITY_COUNT)
    {
        this.STORED_ENTITY_COUNT = STORED_ENTITY_COUNT;
    }

    public void setStoredEntities(List<EntityType<?>> entityTypeList)
    {
        this.STORED_ENTITIES = entityTypeList;
    }

    public boolean stillValid()
    {
        boolean returnValue = true;

        for (FencePart value : CONNECTED_BLOCKS.values())
        {
            if(!(level.getBlockState(value.getBlockPos()).getBlock() instanceof FenceBlock)) returnValue = false;
            if(!(level.getBlockState(value.getBlockPos()).getBlock() instanceof BlockMultiBlockFenceGate)) returnValue = false;
        }

        return returnValue;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag)
    {
        CompoundTag compoundTag1 = super.save(compoundTag);
        compoundTag1.putLong("lastupdated", LAST_UPDATED_TIME);
        compoundTag1.merge(saveEntityList());
        return compoundTag1;
    }

    public CompoundTag saveEntityList()
    {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < STORED_ENTITIES.size(); i++)
        {
            if (STORED_ENTITIES.get(i) != null)
            {
                CompoundTag entityTag = new CompoundTag();
                EntityType<?> entityType = STORED_ENTITIES.get(i);
                entityTag.putString("entity", EntityType.getKey(entityType).toString());
                nbtTagList.add(entityTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Entities", nbtTagList);
        nbt.putInt("Size", STORED_ENTITIES.size());
        return nbt;
    }

    @Override
    public void load(BlockState blockState, CompoundTag compoundTag)
    {
        super.load(blockState, compoundTag);
        LAST_UPDATED_TIME = compoundTag.getLong("lastupdated");
        loadEntityList(compoundTag);
        setStoredEntityCount(STORED_ENTITIES.size());
        walkFence();
    }

    public void loadEntityList(CompoundTag nbt)
    {
        if(nbt == null) return;

        ListTag tagList = nbt.getList("Entities", 10);
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag entityTags = tagList.getCompound(i);
            EntityType<?> entityType = EntityType.byString(entityTags.getString("entity")).orElse(null);
            if(entityType != null) STORED_ENTITIES.add(entityType);
        }
    }

    @Override
    protected Component getDefaultName()
    {
        return new TranslatableComponent("container." + WhyYouMakeLag.MOD_ID + ".fence");
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory)
    {
        return new ContainerFence(i, inventory, this);
    }

    boolean loaded = false;

    public void tileFirstTick()
    {
        if(!loaded)
        {
            walkFence();
            loaded = true;
        }
    }

    //Inventory
    @Override
    public int getContainerSize()
    {
        return 0;
    }

    @Override
    public boolean isEmpty() {return false;}

    @Override
    public ItemStack getItem(int i)
    {
        return null;
    }

    @Override
    public ItemStack removeItem(int i, int j)
    {
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i)
    {
        return null;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {}

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    @Override
    public void clearContent() {}
}
