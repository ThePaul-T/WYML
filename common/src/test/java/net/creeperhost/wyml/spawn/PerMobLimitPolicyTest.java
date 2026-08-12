package net.creeperhost.wyml.spawn;

import net.creeperhost.wyml.config.ConfigData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerMobLimitPolicyTest
{
    @Test
    void prospectiveSpawnsStopAtThePopulationLimit()
    {
        assertFalse(PerMobLimitPolicy.blocksProspectiveSpawn(7, 8));
        assertTrue(PerMobLimitPolicy.blocksProspectiveSpawn(8, 8));
        assertFalse(PerMobLimitPolicy.blocksProspectiveSpawn(100, -1));
    }

    @Test
    void postAdmissionChecksOnlyRemoveActualExcess()
    {
        assertFalse(PerMobLimitPolicy.isExcessAfterAdmission(8, 8));
        assertTrue(PerMobLimitPolicy.isExcessAfterAdmission(9, 8));
        assertFalse(PerMobLimitPolicy.isExcessAfterAdmission(100, -1));
    }

    @Test
    void modernWaterCategoriesUseTheWaterCreatureDefault()
    {
        ConfigData config = new ConfigData();
        config.MONSTER_PER_CHUNK = 11;
        config.CREATURES_PER_CHUNK = 12;
        config.AMBIENT_CREATURES_PER_CHUNK = 13;
        config.WATER_CREATURES_PER_CHUNK = 14;
        config.WATER_AMBIENT_PER_CHUNK = 15;
        config.MISC_CREATURES_PER_CHUNK = -1;

        assertEquals(11, PerMobLimitPolicy.defaultLimit("monster", config));
        assertEquals(12, PerMobLimitPolicy.defaultLimit("creature", config));
        assertEquals(13, PerMobLimitPolicy.defaultLimit("ambient", config));
        assertEquals(14, PerMobLimitPolicy.defaultLimit("water_creature", config));
        assertEquals(14, PerMobLimitPolicy.defaultLimit("underground_water_creature", config));
        assertEquals(14, PerMobLimitPolicy.defaultLimit("axolotls", config));
        assertEquals(15, PerMobLimitPolicy.defaultLimit("water_ambient", config));
        assertEquals(-1, PerMobLimitPolicy.defaultLimit("provider_defined_category", config));
    }

    @Test
    void legacyWorldGenerationEscapeHatchBypassesWymlHandling()
    {
        assertTrue(PerMobLimitPolicy.checksWorldGeneration(false));
        assertFalse(PerMobLimitPolicy.checksWorldGeneration(true));
    }
}
