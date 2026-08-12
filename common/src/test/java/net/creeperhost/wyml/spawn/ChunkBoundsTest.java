package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkBoundsTest
{
    @Test
    void horizontalBoundsAreAlwaysExactlyOneChunk()
    {
        assertChunkWidth(0, 0);
        assertChunkWidth(128, 64);
        assertChunkWidth(-128, -64);
    }

    private static void assertChunkWidth(int chunkX, int chunkZ)
    {
        ChunkHorizontalBounds bounds = ChunkHorizontalBounds.fromChunkCoordinates(chunkX, chunkZ);

        assertEquals(16, bounds.width());
        assertEquals(16, bounds.depth());
        assertEquals(chunkX << 4, bounds.minX());
        assertEquals((chunkX << 4) + 16, bounds.maxXExclusive());
    }
}
