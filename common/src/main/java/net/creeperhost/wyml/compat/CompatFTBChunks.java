package net.creeperhost.wyml.compat;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class CompatFTBChunks
{
    private CompatFTBChunks()
    {
    }

    public static boolean isClaimed(Level level, ChunkPos chunkPos)
    {
        try
        {
            FTBChunksAPI.API api = FTBChunksAPI.api();
            return api.isManagerLoaded() && api.getOwningTeam(level, chunkPos).isPresent();
        }
        catch (RuntimeException | LinkageError ignored)
        {
            return false;
        }
    }
}
