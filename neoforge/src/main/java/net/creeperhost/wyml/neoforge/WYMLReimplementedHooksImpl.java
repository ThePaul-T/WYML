package net.creeperhost.wyml.neoforge;

import net.creeperhost.wyml.platform.WymlPlatformHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

public class WYMLReimplementedHooksImpl implements WymlPlatformHooks
{
    @Override
    public int canSpawn(Mob mob, ServerLevel level, EntitySpawnReason reason)
    {
        MobSpawnEvent.PositionCheck event = new MobSpawnEvent.PositionCheck(mob, level, reason, null);
        NeoForge.EVENT_BUS.post(event);
        return switch (event.getResult())
        {
            case SUCCEED -> 1;
            case FAIL -> -1;
            case DEFAULT -> 0;
        };
    }
    @Override
    public boolean doSpecialSpawn(Mob mob, ServerLevel level, double d0, int i, double d1, BaseSpawner spawner, EntitySpawnReason reason)
    {
        return false;
    }
    @Override
    public int getMaxGroupSize(Mob mob)
    {
        return net.neoforged.neoforge.event.EventHooks.getMaxSpawnClusterSize(mob);
    }
    @Override
    public boolean isValidPickup(ItemStack itemStack, Level level)
    {
        return !itemStack.isEmpty();
    }
}
