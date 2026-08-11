package net.creeperhost.wyml.platform;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;

public interface WymlPlatformHooks
{
    int canSpawn(Mob mob, ServerLevel level, EntitySpawnReason reason);

    default boolean doSpecialSpawn(Mob mob, ServerLevel level, double x, int y, double z, BaseSpawner spawner, EntitySpawnReason reason)
    {
        return false;
    }

    default int getMaxGroupSize(Mob mob)
    {
        return mob.getMaxSpawnClusterSize();
    }

    boolean isValidPickup(ItemStack itemStack, Level level);
}
