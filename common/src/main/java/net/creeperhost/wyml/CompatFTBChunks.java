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
        if(!FTBChunksAPI.isManagerLoaded()) return null;
        if (FTBChunksAPI.getManager() == null) return null;
        return FTBChunksAPI.getManager().getAllClaimedChunks();
    }

    public static List<Long> getChunkPosList()
    {
        Collection<ClaimedChunk> claimedChunks = getClaimed();
        List<Long> chunkPosList = new ArrayList<>();
        if(claimedChunks == null) return chunkPosList;
        for(ClaimedChunk chunk : claimedChunks)
        {
            chunkPosList.add(chunk.getPos().getChunkPos().toLong());
        }
        return chunkPosList;
    }
}
