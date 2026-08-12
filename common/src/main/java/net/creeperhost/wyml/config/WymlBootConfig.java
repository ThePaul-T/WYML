package net.creeperhost.wyml.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Restart-only feature switches used before Minecraft classes are transformed.
 * Runtime limits remain in {@code wyml.json}; changing this file requires a restart.
 */
public final class WymlBootConfig
{
    public static final String FILE_NAME = "wyml-mixins.properties";

    private static final System.Logger LOGGER = System.getLogger("WYML/Mixins");
    private static final Pattern MAIN_ENABLED = Pattern.compile(
            "(?im)[\\\"']?ENABLE_WYML[\\\"']?\\s*:\\s*(true|false)");
    private static volatile WymlBootConfig active;

    private final Properties properties;
    private final boolean mainConfigEnabled;

    private WymlBootConfig(Properties properties, boolean mainConfigEnabled)
    {
        this.properties = properties;
        this.mainConfigEnabled = mainConfigEnabled;
    }

    public static WymlBootConfig load()
    {
        Path configDirectory = resolveConfigDirectory();
        Path bootFile = configDirectory.resolve(FILE_NAME);
        Properties properties = defaults();

        if (Files.isRegularFile(bootFile))
        {
            try (Reader reader = Files.newBufferedReader(bootFile, StandardCharsets.UTF_8))
            {
                properties.load(reader);
            }
            catch (IOException exception)
            {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Could not read " + bootFile + "; preserving the compatibility profile.", exception);
            }
        }
        else
        {
            writeDefaults(bootFile, properties);
        }

        WymlBootConfig loaded = new WymlBootConfig(properties, readMainEnabled(configDirectory.resolve("wyml.json")));
        active = loaded;
        return loaded;
    }

    public static boolean moduleEnabled(String module)
    {
        WymlBootConfig current = active;
        return current == null || (current.masterEnabled() && current.enabled(module));
    }

    public boolean masterEnabled()
    {
        return mainConfigEnabled && enabled("master_enabled");
    }

    public boolean enabled(String module)
    {
        if (!mainConfigEnabled)
        {
            return false;
        }
        return Boolean.parseBoolean(properties.getProperty(module, "true").trim());
    }

    private static Path resolveConfigDirectory()
    {
        String override = System.getProperty("wyml.configDir");
        if (override != null && !override.isBlank())
        {
            return Path.of(override);
        }
        return Path.of(System.getProperty("user.dir", "."), "config");
    }

    private static Properties defaults()
    {
        Properties defaults = new Properties();
        defaults.setProperty("master_enabled", "true");
        defaults.setProperty("spawn_controller", "true");
        defaults.setProperty("category_policy", "true");
        defaults.setProperty("per_mob_rules", "true");
        defaults.setProperty("paper_bags", "true");
        defaults.setProperty("item_lifetime", "true");
        defaults.setProperty("item_merging", "true");
        defaults.setProperty("entity_pushing", "true");
        defaults.setProperty("tick_pacing", "true");
        defaults.setProperty("post_load_gc", "true");
        defaults.setProperty("numeric_ping", "true");
        return defaults;
    }

    private static void writeDefaults(Path bootFile, Properties properties)
    {
        try
        {
            Files.createDirectories(bootFile.getParent());
            try (Writer writer = Files.newBufferedWriter(bootFile, StandardCharsets.UTF_8))
            {
                writer.write("# WYML restart-only mixin profile. Changes require a game/server restart.\n");
                writer.write("# Missing profiles preserve the legacy-compatible enabled state.\n");
                writer.write("# Set master_enabled=false for an all-off transformation baseline.\n");
                for (String name : properties.stringPropertyNames().stream().sorted().toList())
                {
                    writer.write(name.toLowerCase(Locale.ROOT) + "=" + properties.getProperty(name) + "\n");
                }
            }
        }
        catch (IOException exception)
        {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Could not create " + bootFile + "; preserving the compatibility profile.", exception);
        }
    }

    private static boolean readMainEnabled(Path mainFile)
    {
        if (!Files.isRegularFile(mainFile))
        {
            return true;
        }
        try
        {
            Matcher matcher = MAIN_ENABLED.matcher(Files.readString(mainFile, StandardCharsets.UTF_8));
            return !matcher.find() || Boolean.parseBoolean(matcher.group(1));
        }
        catch (IOException exception)
        {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Could not inspect " + mainFile + " for ENABLE_WYML; preserving compatibility.", exception);
            return true;
        }
    }
}
