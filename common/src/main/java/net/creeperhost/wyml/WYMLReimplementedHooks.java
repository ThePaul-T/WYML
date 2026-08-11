package net.creeperhost.wyml;

import net.creeperhost.wyml.platform.WymlPlatformHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;

import java.util.ServiceLoader;

public class WYMLReimplementedHooks
{
    private static final WymlPlatformHooks PLATFORM = ServiceLoader.load(WymlPlatformHooks.class, WYMLReimplementedHooks.class.getClassLoader())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No WYML platform hooks were provided by the active loader"));

    public static int canSpawn(Mob mob, ServerLevel level, double d0, int i, double d1, BaseSpawner spawner, EntitySpawnReason reason)
    {
        return PLATFORM.canSpawn(mob, level, reason);
    }

    public static boolean doSpecialSpawn(Mob mob, ServerLevel level, double d0, int i, double d1, BaseSpawner spawner, EntitySpawnReason reason)
    {
        return PLATFORM.doSpecialSpawn(mob, level, d0, i, d1, spawner, reason);
    }

    public static int getMaxGroupSize(Mob mob)
    {
        return PLATFORM.getMaxGroupSize(mob);
    }

    public static boolean isValidPickup(ItemStack itemStack, Level level)
    {
        return PLATFORM.isValidPickup(itemStack, level);
    }
}
