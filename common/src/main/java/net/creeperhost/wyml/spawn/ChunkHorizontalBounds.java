package net.creeperhost.wyml.spawn;

/** Inclusive minimum and exclusive maximum block coordinates for one chunk. */
public record ChunkHorizontalBounds(int minX, int minZ, int maxXExclusive, int maxZExclusive)
{
    public static ChunkHorizontalBounds fromChunkCoordinates(int chunkX, int chunkZ)
    {
        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        return new ChunkHorizontalBounds(minX, minZ, minX + 16, minZ + 16);
    }

    public int width()
    {
        return maxXExclusive - minX;
    }

    public int depth()
    {
        return maxZExclusive - minZ;
    }
}
