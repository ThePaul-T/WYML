package net.creeperhost.wyml;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WymlConfig
{
    private static AtomicReference<ConfigData> data = new AtomicReference<>();
    private static File lastFile;

    //TODO save and load
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
    public static ConfigData update(ConfigData _data)
    {
        data.set(_data);
        return data.get();
    }

    public static boolean reload()
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
