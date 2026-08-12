package net.creeperhost.wyml.spawn;

import net.creeperhost.wyml.config.ConfigData;

import java.util.Locale;

/**
 * Pure policy for legacy per-mob population limits.
 *
 * <p>The configured limit is a population count for one entity type in one
 * chunk. It is deliberately unrelated to a biome spawn entry's pack size.</p>
 */
public final class PerMobLimitPolicy
{
    private PerMobLimitPolicy()
    {
    }

    public static boolean blocksProspectiveSpawn(int currentPopulation, int limit)
    {
        return limit >= 0 && currentPopulation >= limit;
    }

    public static boolean isExcessAfterAdmission(int currentPopulation, int limit)
    {
        return limit >= 0 && currentPopulation > limit;
    }

    public static boolean checksWorldGeneration(boolean disableCountingChunkGeneratedMobs)
    {
        return !disableCountingChunkGeneratedMobs;
    }

    public static int defaultLimit(String categoryName, ConfigData config)
    {
        String normalized = categoryName == null ? "" : categoryName.toLowerCase(Locale.ROOT);
        return switch (normalized)
        {
            case "monster" -> config.MONSTER_PER_CHUNK;
            case "creature" -> config.CREATURES_PER_CHUNK;
            case "ambient" -> config.AMBIENT_CREATURES_PER_CHUNK;
            case "water_creature", "underground_water_creature", "axolotls" -> config.WATER_CREATURES_PER_CHUNK;
            case "water_ambient" -> config.WATER_AMBIENT_PER_CHUNK;
            case "misc" -> config.MISC_CREATURES_PER_CHUNK;
            default -> config.MISC_CREATURES_PER_CHUNK;
        };
    }
}
