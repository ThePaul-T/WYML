package net.creeperhost.mutliblockapi.events;

import net.creeperhost.mutliblockapi.MultiblockRegistry;
import net.minecraft.server.level.ServerLevel;

public class MultiblockEventHandler
{
	//TODO, There is no event for this
//	public void onChunkLoad(ChunkEvent.Load loadEvent)
//	{
//		LevelChunk chunk = loadEvent.getChunk();
//		Level world = loadEvent.getWorld();
//		MultiblockRegistry.onChunkLoaded((World) world, (Chunk) chunk);
//	}

	public static void onWorldUnload(ServerLevel serverLevel)
	{
		MultiblockRegistry.onWorldUnloaded(serverLevel);
	}
}
