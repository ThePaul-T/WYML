package net.creeperhost.wyml.network;

import me.shedaniel.architectury.networking.NetworkManager;
import net.creeperhost.wyml.blocks.TilePaperBag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class MessageUpdatePaperbag
{
    BlockPos blockPos;
    int usedSlots;
    long timeStamp;

    public MessageUpdatePaperbag(BlockPos blockPos, int usedSlots, long timestamp)
    {
        this.blockPos = blockPos;
        this.usedSlots = usedSlots;
        this.timeStamp = timestamp;
    }

    public MessageUpdatePaperbag(FriendlyByteBuf buffer)
    {
        this.blockPos = buffer.readBlockPos();
        this.usedSlots = buffer.readInt();
        this.timeStamp = buffer.readLong();
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(blockPos);
        buf.writeInt(usedSlots);
        buf.writeLong(timeStamp);
    }

    public void handle(Supplier<NetworkManager.PacketContext> context)
    {
        context.get().queue(() ->
        {
            Level level = context.get().getPlayer().level;
            if (level == null) return;

            if (level.getBlockEntity(blockPos) instanceof TilePaperBag)
            {
                TilePaperBag tilePaperBag = (TilePaperBag) level.getBlockEntity(blockPos);
                if (tilePaperBag == null) return;

                tilePaperBag.setDespawnTime(timeStamp);
                tilePaperBag.setUsedCount(usedSlots);
            }
        });
    }
}
