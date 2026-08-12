package net.creeperhost.wyml.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import net.creeperhost.polylib.platform.Services;
import net.creeperhost.wyml.WhyYouMakeLag;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WymlConfig
{
    private static AtomicReference<ConfigData> data = new AtomicReference<>();
    private static File lastFile;
    private static boolean loaded;
    private static Jankson gson = Jankson.builder().build();
    private static boolean HAS_INITIALISED = false;
    private static WatchService watcher;
    private static boolean watching;

    public static void loadFromFile(File file)
    {
        lastFile = file;
        try
        {
            JsonObject jObject = gson.load(file);
            ConfigData newData = gson.fromJson(jObject, ConfigData.class);
            if (newData != data.get())
            {
                data.set(newData);
                if (!isLoaded())
                {
                    //Save again immediately, as this makes sure that any missing config values get added with their defaults to the config file, and the comments are restored.
                    Files.writeString(file.toPath(), WymlConfig.saveConfig(), StandardCharsets.UTF_8);
                }
                loaded = true;
            }
        } catch (Exception ignored)
        {
            data.set(new ConfigData());
            loaded = true;
        }
    }

    public static boolean isLoaded()
    {
        return loaded;
    }

    public static void saveConfigToFile(File file)
    {
        try (FileOutputStream configOut = new FileOutputStream(file))
        {
            IOUtils.write(WymlConfig.saveConfig(), configOut, Charset.defaultCharset());
        } catch (Throwable ignored)
        {
        }
    }

    public static ConfigData cached()
    {
        if(!WymlConfig.HAS_INITIALISED) WymlConfig.init();
        return data.get();
    }

    public static boolean isEnabled()
    {
        return cached().ENABLE_WYML;
    }

    public static synchronized ConfigData update(ConfigData _data)
    {
        data.set(_data);
        return data.get();
    }

    public static synchronized boolean reload()
    {
        if (lastFile != null)
        {
            loadFromFile(lastFile);
            return true;
        }
        return false;
    }

    public static String saveConfig()
    {
        ConfigData conf = data.get();
        JsonElement elem = gson.toJson(conf);
        return elem.toJson(true, true);
    }

    public static void init()
    {
        init(Services.PLATFORM.getConfigFolder().resolve(WhyYouMakeLag.MOD_ID + ".json").toFile());
    }

    public static void init(File file)
    {
        if (lastFile == null) lastFile = file;
        try
        {
            Path parent = file.toPath().getParent();
            if (parent != null) Files.createDirectories(parent);
            if (!file.exists())
            {
                ConfigData configData = new ConfigData();
                data.set(configData);
                Files.writeString(file.toPath(), WymlConfig.saveConfig(), StandardCharsets.UTF_8);
                loaded = true;
            }
            else
            {
                WymlConfig.loadFromFile(file);
            }
        } catch (Exception ignored)
        {
        }
        WymlConfig.HAS_INITIALISED = true;
    }

    public static synchronized void startWatcher(ScheduledExecutorService executor)
    {
        if (watching || lastFile == null || executor == null) return;
        try
        {
            watcher = FileSystems.getDefault().newWatchService();
            lastFile.toPath().getParent().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            watching = true;
            executor.scheduleWithFixedDelay(WymlConfig::pollWatcher, 10, 10, TimeUnit.SECONDS);
        }
        catch (IOException exception)
        {
            WhyYouMakeLag.LOGGER.warn("Unable to watch WYML config for changes", exception);
        }
    }

    public static synchronized void stopWatcher()
    {
        watching = false;
        if (watcher != null)
        {
            try
            {
                watcher.close();
            }
            catch (IOException ignored)
            {
            }
            watcher = null;
        }
    }

    private static void pollWatcher()
    {
        WatchKey key;
        while (watching && watcher != null && (key = watcher.poll()) != null)
        {
            for (WatchEvent<?> event : key.pollEvents())
            {
                Path changed = (Path) event.context();
                if (changed.endsWith(lastFile.getName()) && isLoaded() && reload())
                {
                    WhyYouMakeLag.LOGGER.info("Config at " + lastFile.getAbsolutePath() + " has changed, reloaded!");
                }
            }
            key.reset();
        }
    }
}
