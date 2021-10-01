package net.creeperhost.mutliblockapi.events;

import net.creeperhost.mutliblockapi.MultiblockRegistry;
import net.minecraft.server.MinecraftServer;

public class MultiblockServerTickHandler
{
	public static void onWorldTick(MinecraftServer minecraftServer)
	{
		MultiblockRegistry.tickStart(minecraftServer.overworld());
	}
}
