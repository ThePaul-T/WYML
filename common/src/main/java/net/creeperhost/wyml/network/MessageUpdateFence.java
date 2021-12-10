package net.creeperhost.wyml.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class MessageUpdateFence
{
    BlockPos blockPos;
    int storedCount;
    List<EntityType<?>> entityTypes;

    public MessageUpdateFence(BlockPos blockPos, int storedCount, List<EntityType<?>> entityTypes)
    {
        this.blockPos = blockPos;
        this.storedCount = storedCount;
        this.entityTypes = entityTypes;
    }

    public MessageUpdateFence(FriendlyByteBuf buffer)
    {
        this.blockPos = buffer.readBlockPos();
        this.storedCount = buffer.readInt();
        int[] ints = buffer.readVarIntArray();
        List<EntityType<?>> entityTypeList = new ArrayList<>();
        for (int entityID : ints)
        {
            EntityType<?> entityType = Registry.ENTITY_TYPE.byId(entityID);
            entityTypeList.add(entityType);
        }
        this.entityTypes = entityTypeList;
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(blockPos);
        buf.writeInt(storedCount);
        List<Integer> integerList = new ArrayList<>();
        for (EntityType<?> entityType : entityTypes)
        {
            integerList.add(Registry.ENTITY_TYPE.getId(entityType));
        }
        int[] ints = integerList.stream().filter(Objects::nonNull).mapToInt(value -> value).toArray();
        buf.writeVarIntArray(ints);
    }

    public void handle(Supplier<NetworkManager.PacketContext> context)
    {
        context.get().queue(() ->
        {
            Level level = context.get().getPlayer().level;
            if (level == null) return;

//            if (level.getBlockEntity(blockPos) instanceof TileMultiBlockFenceGate)
//            {
//                TileMultiBlockFenceGate tileMultiBlockFenceGate = (TileMultiBlockFenceGate) level.getBlockEntity(blockPos);
//                if (tileMultiBlockFenceGate == null) return;
//
//                tileMultiBlockFenceGate.setStoredEntityCount(storedCount);
//                if(entityTypes != null && !entityTypes.isEmpty())
//                {
//                    tileMultiBlockFenceGate.setStoredEntities(entityTypes);
//                }
//            }
        });
    }
}
