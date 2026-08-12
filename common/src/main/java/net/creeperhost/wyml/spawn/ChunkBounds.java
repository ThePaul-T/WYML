package net.creeperhost.wyml.spawn;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Exact bounds for a single chunk. Kept separate to prevent the historical
 * origin-dependent AABB regression from returning during counting changes.
 */
public final class ChunkBounds
{
    private ChunkBounds()
    {
    }

    public static AABB fullHeight(ChunkPos chunk, Level level)
    {
        ChunkHorizontalBounds horizontal = ChunkHorizontalBounds.fromChunkCoordinates(chunk.x(), chunk.z());
        return new AABB(
                horizontal.minX(), level.getMinY(), horizontal.minZ(),
                horizontal.maxXExclusive(), level.getMaxY() + 1, horizontal.maxZExclusive());
    }
}
