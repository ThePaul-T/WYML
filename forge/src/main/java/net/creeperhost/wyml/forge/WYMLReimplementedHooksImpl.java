package net.creeperhost.wyml.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;

public class WYMLReimplementedHooksImpl
{
    public static int canSpawn(Mob mob, ServerLevel level, double d0, int i, double d1, BaseSpawner spawner, MobSpawnType reason)
    {
        return net.minecraftforge.common.ForgeHooks.canEntitySpawn(mob, level, d0, i, d1, spawner, reason);
    }
    public static boolean doSpecialSpawn(Mob mob, ServerLevel level, double d0, int i, double d1, BaseSpawner spawner, MobSpawnType reason)
    {
        return net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(mob, level, (float) d0, i, (float) d1, spawner, reason);
    }
    public static int getMaxGroupSize(Mob mob)
    {
        return net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(mob);
    }
}
