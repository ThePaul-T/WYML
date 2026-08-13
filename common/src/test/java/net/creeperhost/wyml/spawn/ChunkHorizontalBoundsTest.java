package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkHorizontalBoundsTest
{
    @Test
    void exactAtOriginAndFarPositiveAndNegativeCoordinates()
    {
        assertChunk(0, 0);
        assertChunk(128, 64);
        assertChunk(-128, -64);
    }

    private static void assertChunk(int chunkX, int chunkZ)
    {
        ChunkHorizontalBounds bounds = ChunkHorizontalBounds.fromChunkCoordinates(chunkX, chunkZ);
        assertEquals(16, bounds.width());
        assertEquals(16, bounds.depth());
        assertEquals(chunkX << 4, bounds.minX());
        assertEquals(chunkZ << 4, bounds.minZ());
        assertEquals((chunkX << 4) + 16, bounds.maxXExclusive());
        assertEquals((chunkZ << 4) + 16, bounds.maxZExclusive());
    }
}
