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

public class WymlConfig
{
    public AtomicInteger MOB_TRIES = new AtomicInteger(1);
    public AtomicBoolean MULTIPLY_BY_PLAYERS = new AtomicBoolean(true);
    public AtomicInteger SLOW_TICKS = new AtomicInteger(600);
    public AtomicInteger PAUSE_TICKS = new AtomicInteger(1800);
    public AtomicInteger PAUSE_RATE = new AtomicInteger(65);
    public AtomicInteger RESUME_RATE = new AtomicInteger(10);
    public AtomicDouble MOJANG_MAGIC_NUM = new AtomicDouble(17D);
    public AtomicInteger PAUSE_MIN = new AtomicInteger(256);
    public AtomicInteger SAMPLE_TICKS = new AtomicInteger(5);
    public AtomicInteger SPAWNLOC_CACHE_TICKS = new AtomicInteger(600);
    public AtomicInteger MANAGER_CACHE_TICKS = new AtomicInteger(600);
    public AtomicBoolean ALLOW_PAUSE = new AtomicBoolean(false);
    public AtomicBoolean ALLOW_SLOW = new AtomicBoolean(true);
    public AtomicBoolean DEBUG_PRINT = new AtomicBoolean(true);
    public AtomicBoolean CLEAN_PRINT = new AtomicBoolean(true);
    public AtomicBoolean DOWNSCALE_MAGIC_NUM = new AtomicBoolean(true);
    public AtomicDouble  DOWNSCALE_MAGIC_NUM_MIN = new AtomicDouble(8D);
    public AtomicInteger MAX_CHUNK_SPAWN_REQ_TICK = new AtomicInteger(12);

    public static WymlConfig instance;


    //TODO save and load
    public static void loadFromFile(File file)
    {
        Gson gson = new Gson();
        try
        {
            FileReader fileReader = new FileReader(file);
            instance = gson.fromJson(fileReader, WymlConfig.class);
        } catch (Exception ignored)
        {
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

    public static String saveConfig()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        return gson.toJson(instance);
    }

    public static void init(File file)
    {
        try
        {
            if (!file.exists())
            {
                WymlConfig.instance = new WymlConfig();

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
