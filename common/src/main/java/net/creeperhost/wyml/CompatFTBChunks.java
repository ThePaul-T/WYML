package net.creeperhost.wyml;

import dev.ftb.mods.ftbchunks.data.ClaimedChunk;
import dev.ftb.mods.ftbchunks.data.FTBChunksAPI;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompatFTBChunks
{
    public static Collection<ClaimedChunk> getClaimed()
    {
        return FTBChunksAPI.getManager().getAllClaimedChunks();
    }

    public static List<Long> getChunkPosList()
    {
        Collection<ClaimedChunk> claimedChunks = getClaimed();
        List<Long> chunkPosList = new ArrayList<>();
        for(ClaimedChunk chunk : claimedChunks)
        {
            chunkPosList.add(chunk.getPos().getChunkPos().toLong());
        }
        return chunkPosList;
    }
}
