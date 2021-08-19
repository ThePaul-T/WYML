package net.creeperhost.wyml;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.Marshaller;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WymlConfig
{
    private static AtomicReference<ConfigData> data = new AtomicReference<>();
    private static File lastFile;
    private static
    Jankson gson = Jankson
            .builder()
            .build();

    //TODO switch from gson to jankson (JSON5 with comments, ideally)
    public static void loadFromFile(File file)
    {
        lastFile = file;
        try
        {
            JsonObject jObject = gson.load(file);
            data.set(gson.fromJson(jObject, ConfigData.class));
        } catch (Exception ignored)
        {
            data.set(new ConfigData());
        }
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
        return data.get();
    }
    public static synchronized ConfigData update(ConfigData _data)
    {
        data.set(_data);
        return data.get();
    }

    public static synchronized boolean reload()
    {
        if(lastFile != null)
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

    public static void init(File file)
    {
        if(lastFile == null) lastFile = file;
        try
        {
            //TODO make someone who understands java better make sure this is sane.
            try
            {
                AtomicReference<WatchService> watcher = new AtomicReference<>();
                Runnable configWatcher = () -> {
                    try {
                        if(watcher.get() == null)
                        {
                            watcher.set(FileSystems.getDefault().newWatchService());
                            lastFile.toPath().getParent().register(watcher.get(), StandardWatchEventKinds.ENTRY_MODIFY);
                        }
                        WatchKey checker = watcher.get().take();
                        for (WatchEvent<?> event : checker.pollEvents()) {
                            Path changed = (Path) event.context();
                            if (changed.endsWith(lastFile.getName())) {
                                if(reload()) WhyYouMakeLag.LOGGER.info("Config at "+lastFile.getAbsolutePath()+" has changed, reloaded!");
                            }
                        }
                        checker.reset();
                    } catch(Exception ignored) {}
                };
                WhyYouMakeLag.scheduledExecutorService2.scheduleAtFixedRate(configWatcher, 0, 10, TimeUnit.SECONDS);

            } catch(Exception ignored) {}

            if (!file.exists())
            {
                ConfigData configData = new ConfigData();
                data.set(configData);
                FileWriter tileWriter = new FileWriter(file);
                tileWriter.write(WymlConfig.saveConfig());
                tileWriter.close();
            }
            else
            {
                WymlConfig.loadFromFile(file);
            }
        } catch (Exception ignored)
        {
        }
    }
}
