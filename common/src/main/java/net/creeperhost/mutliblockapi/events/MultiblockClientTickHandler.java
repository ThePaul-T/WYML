package net.creeperhost.mutliblockapi.events;

import net.creeperhost.mutliblockapi.MultiblockRegistry;
import net.minecraft.client.Minecraft;

public class MultiblockClientTickHandler
{
	public static void onClientTick(Minecraft minecraft)
	{
		MultiblockRegistry.tickStart(minecraft.level);
	}
}
