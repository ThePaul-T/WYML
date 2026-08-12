package net.creeperhost.wyml.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WymlBootConfigTest
{
    @TempDir
    Path configDirectory;

    @AfterEach
    void clearOverride()
    {
        System.clearProperty("wyml.configDir");
    }

    @Test
    void missingProfilePreservesCompatibilityAndCreatesAProfile() throws Exception
    {
        System.setProperty("wyml.configDir", configDirectory.toString());

        WymlBootConfig config = WymlBootConfig.load();

        assertTrue(config.masterEnabled());
        assertTrue(config.enabled("spawn_controller"));
        assertTrue(Files.isRegularFile(configDirectory.resolve(WymlBootConfig.FILE_NAME)));
    }

    @Test
    void bootMasterSwitchDisablesAllMixins() throws Exception
    {
        System.setProperty("wyml.configDir", configDirectory.toString());
        Files.writeString(configDirectory.resolve(WymlBootConfig.FILE_NAME),
                "master_enabled=false\nspawn_controller=true\n", StandardCharsets.UTF_8);

        WymlBootConfig config = WymlBootConfig.load();

        assertFalse(config.masterEnabled());
    }

    @Test
    void mainRuntimeMasterSwitchAlsoDisablesBootTransformations() throws Exception
    {
        System.setProperty("wyml.configDir", configDirectory.toString());
        Files.writeString(configDirectory.resolve("wyml.json"),
                "{ ENABLE_WYML: false }", StandardCharsets.UTF_8);

        WymlBootConfig config = WymlBootConfig.load();

        assertFalse(config.masterEnabled());
        assertFalse(config.enabled("spawn_controller"));
    }

    @Test
    void individualModuleCanBeExcluded() throws Exception
    {
        System.setProperty("wyml.configDir", configDirectory.toString());
        Files.writeString(configDirectory.resolve(WymlBootConfig.FILE_NAME),
                "master_enabled=true\nentity_pushing=false\n", StandardCharsets.UTF_8);

        WymlBootConfig config = WymlBootConfig.load();

        assertTrue(config.masterEnabled());
        assertFalse(config.enabled("entity_pushing"));
        assertFalse(WymlBootConfig.moduleEnabled("entity_pushing"));
        assertTrue(WymlBootConfig.moduleEnabled("spawn_controller"));
    }
}
