package net.creeperhost.wyml.fabric;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;

public class WYMLReimplementedHooksImpl
{
    public static int canSpawn(Mob mob, ServerLevel level, double d0, int i, double d1, BaseSpawner spawner, MobSpawnType reason)
    {
        return 0;
    }
    public static boolean doSpecialSpawn(Mob mob, ServerLevel level, double d0, int i, double d1, BaseSpawner spawner, MobSpawnType reason)
    {
        return false;
    }
    public static int getMaxGroupSize(Mob mob)
    {
        return mob.getMaxSpawnClusterSize();
    }
    public static boolean isValidPickup(ItemStack itemStack, Level level)
    {
        return true;
    }
}
