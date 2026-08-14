package net.creeperhost.wyml.spawn;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.IdentityHashMap;
import java.util.Map;

/** Server/dimension/chunk/type population index maintained by admission, movement, and removal hooks. */
public final class MobPopulationIndex
{
    private static final Map<MinecraftServer, IdentityPopulationCounter<Entity, PopulationKey>> SERVERS =
            new IdentityHashMap<>();

    private MobPopulationIndex()
    {
    }

    public static synchronized void admitted(Entity entity)
    {
        if (!(entity instanceof Mob) || !(entity.level() instanceof ServerLevel level)) return;
        counter(level.getServer()).assign(entity, PopulationKey.of(level, entity.chunkPosition(), entity.getType()));
    }

    public static synchronized void moved(Entity entity)
    {
        if (!(entity instanceof Mob) || !(entity.level() instanceof ServerLevel level)) return;
        IdentityPopulationCounter<Entity, PopulationKey> counter = SERVERS.get(level.getServer());
        if (counter != null)
        {
            counter.reassignIfPresent(entity, PopulationKey.of(level, entity.chunkPosition(), entity.getType()));
        }
    }

    public static synchronized void removed(Entity entity)
    {
        if (!(entity instanceof Mob) || !(entity.level() instanceof ServerLevel level)) return;
        IdentityPopulationCounter<Entity, PopulationKey> counter = SERVERS.get(level.getServer());
        if (counter != null) counter.remove(entity);
    }

    public static synchronized int count(ServerLevel level, ChunkPos chunk, EntityType<?> type)
    {
        IdentityPopulationCounter<Entity, PopulationKey> counter = SERVERS.get(level.getServer());
        return counter == null ? 0 : counter.count(PopulationKey.of(level, chunk, type));
    }

    public static synchronized void clear(MinecraftServer server)
    {
        IdentityPopulationCounter<Entity, PopulationKey> counter = SERVERS.remove(server);
        if (counter != null) counter.clear();
    }

    private static IdentityPopulationCounter<Entity, PopulationKey> counter(MinecraftServer server)
    {
        return SERVERS.computeIfAbsent(server, ignored -> new IdentityPopulationCounter<>());
    }

    private record PopulationKey(ResourceKey<Level> dimension, long chunk, EntityType<?> type)
    {
        static PopulationKey of(ServerLevel level, ChunkPos chunk, EntityType<?> type)
        {
            return new PopulationKey(level.dimension(), chunk.pack(), type);
        }
    }
}
