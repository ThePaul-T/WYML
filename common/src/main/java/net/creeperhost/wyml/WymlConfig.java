package net.creeperhost.wyml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WymlConfig
{
    private static AtomicReference<ConfigData> data = new AtomicReference<>();
    private static File lastFile;



    //TODO switch from gson to jankson (JSON5 with comments, ideally)
    public static void loadFromFile(File file)
    {
        lastFile = file;

        Gson gson = new Gson();
        try
        {
            FileReader fileReader = new FileReader(file);
            data.set(gson.fromJson(fileReader, ConfigData.class));
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
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        return gson.toJson(data.get());
    }

    public static void init(File file)
    {
        try
        {
            //TODO make someone who understands java better make sure this is sane.
            try(WatchService watcher = FileSystems.getDefault().newWatchService())
            {
                WatchKey key = lastFile.toPath().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                Runnable configWatcher = () -> {
                    try {
                        WatchKey checker = watcher.take();
                        for (WatchEvent<?> event : checker.pollEvents()) {
                            Path changed = (Path) event.context();
                            if (changed.equals(lastFile)) {
                                reload();
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
