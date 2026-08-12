package net.creeperhost.wyml.spawn;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public record ControllerKey(ResourceKey<Level> dimension, long chunkPosition, MobCategory category)
{
    public static ControllerKey of(ServerLevel level, ChunkPos chunk, MobCategory category)
    {
        return new ControllerKey(level.dimension(), chunk.pack(), category);
    }
}
