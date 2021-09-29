package net.creeperhost.wyml;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;

public class WYMLReimplementedHooks
{
    @ExpectPlatform
    public static int canSpawn(Mob mob, ServerLevel level, double d0, int i, double d1, BaseSpawner spawner, MobSpawnType reason)
    {
        return 0;
    }

    @ExpectPlatform
    public static boolean doSpecialSpawn(Mob mob, ServerLevel level, double d0, int i, double d1, BaseSpawner spawner, MobSpawnType reason)
    {
        return false;
    }

    @ExpectPlatform
    public static int getMaxGroupSize(Mob mob)
    {
        return mob.getMaxSpawnClusterSize();
    }
}
