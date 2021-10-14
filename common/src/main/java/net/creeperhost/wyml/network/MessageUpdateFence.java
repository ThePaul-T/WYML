package net.creeperhost.wyml.network;

import me.shedaniel.architectury.networking.NetworkManager;
import net.creeperhost.wyml.tiles.TileMultiBlockFenceGate;
import net.creeperhost.wyml.tiles.TilePaperBag;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class MessageUpdateFence
{
    BlockPos blockPos;
    int storedCount;
    int[] entityIds;

    public MessageUpdateFence(BlockPos blockPos, int storedCount)
    {
        this.blockPos = blockPos;
        this.storedCount = storedCount;
    }

    public MessageUpdateFence(FriendlyByteBuf buffer)
    {
        this.blockPos = buffer.readBlockPos();
        this.storedCount = buffer.readInt();
        this.entityIds = buffer.readVarIntArray();
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(blockPos);
        buf.writeInt(storedCount);
        buf.writeVarIntArray(entityIds);
    }

    public void handle(Supplier<NetworkManager.PacketContext> context)
    {
        context.get().queue(() ->
        {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            if (level.getBlockEntity(blockPos) instanceof TileMultiBlockFenceGate)
            {
                TileMultiBlockFenceGate tileMultiBlockFenceGate = (TileMultiBlockFenceGate) level.getBlockEntity(blockPos);
                if (tileMultiBlockFenceGate == null) return;

                tileMultiBlockFenceGate.setStoredEntityCount(storedCount);
            }
        });
    }
}
