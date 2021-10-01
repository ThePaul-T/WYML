package net.creeperhost.mutliblockapi;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Set;

public class MultiblockRegistry
{
	private static HashMap<Level, MultiblockWorldRegistry> registries = new HashMap<Level, MultiblockWorldRegistry>();

	public static void tickStart(Level world)
	{
		if (registries.containsKey(world))
		{
			MultiblockWorldRegistry registry = registries.get(world);
			registry.processMultiblockChanges();
			registry.tickStart();
		}
	}

	public static void onChunkLoaded(Level world, LevelChunk chunk)
	{
		if (registries.containsKey(world)) registries.get(world).onChunkLoaded(chunk);
	}

	public static void onPartAdded(Level world, IMultiblockPart part)
	{
		MultiblockWorldRegistry registry = getOrCreateRegistry(world);
		registry.onPartAdded(part);
	}

	public static void onPartRemovedFromWorld(Level world, IMultiblockPart part)
	{
		if (registries.containsKey(world)) registries.get(world).onPartRemovedFromWorld(part);
	}

	public static void onWorldUnloaded(Level world)
	{
		if (registries.containsKey(world))
		{
			registries.get(world).onWorldUnloaded();
			registries.remove(world);
		}
	}

	public static void addDirtyController(Level world, MultiblockControllerBase controller)
	{
		if (registries.containsKey(world)) registries.get(world).addDirtyController(controller);
	}

	public static void addDeadController(Level world, MultiblockControllerBase controller)
	{
		if (registries.containsKey(world)) registries.get(world).addDeadController(controller);
	}

	public static Set<MultiblockControllerBase> getControllersFromWorld(Level world)
	{
		if (registries.containsKey(world)) return registries.get(world).getControllers();
		return null;
	}

	private static MultiblockWorldRegistry getOrCreateRegistry(Level world)
	{
		if (registries.containsKey(world))
		{
			return registries.get(world);
		}
		else
		{
			MultiblockWorldRegistry newRegistry = new MultiblockWorldRegistry(world);
			registries.put(world, newRegistry);
			return newRegistry;
		}
	}

}
