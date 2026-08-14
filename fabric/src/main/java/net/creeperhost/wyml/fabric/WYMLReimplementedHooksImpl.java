package net.creeperhost.wyml.fabric;

import net.creeperhost.wyml.platform.WymlPlatformHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;

public class WYMLReimplementedHooksImpl implements WymlPlatformHooks
{
    @Override
    public int canSpawn(Mob mob, ServerLevel level, EntitySpawnReason reason)
    {
        return 0;
    }

    @Override
    public boolean doSpecialSpawn(Mob mob, ServerLevel level, double d0, int i, double d1, BaseSpawner spawner, EntitySpawnReason reason)
    {
        return false;
    }
    @Override
    public int getMaxGroupSize(Mob mob)
    {
        return mob.getMaxSpawnClusterSize();
    }
    @Override
    public boolean isValidPickup(ItemStack itemStack, Level level)
    {
        return !itemStack.isEmpty();
    }
}
