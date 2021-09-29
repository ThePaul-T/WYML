package net.creeperhost.wyml.mixins;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface AccessorChunkMap
{
    @Invoker("getChunks")
    Iterable<ChunkHolder> getChunks1();

    @Invoker("tick")
    void tick1();

    @Invoker("noPlayersCloseForSpawning")
    boolean noPlayersCloseForSpawning1(ChunkPos chunkPos);
}
