package net.creeperhost.wyml;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WymlConfig
{
    public static AtomicInteger MOB_TRIES = new AtomicInteger(1);
    public static AtomicBoolean MULTIPLY_BY_PLAYERS = new AtomicBoolean(true);
    public static AtomicInteger SLOW_TICKS = new AtomicInteger(600);
    public static AtomicInteger PAUSE_TICKS = new AtomicInteger(1800);
    public static AtomicInteger PAUSE_RATE = new AtomicInteger(65);
    public static AtomicInteger RESUME_RATE = new AtomicInteger(10);
    public static AtomicDouble MOJANG_MAGIC_NUM = new AtomicDouble(17D);
    public static AtomicInteger PAUSE_MIN = new AtomicInteger(256);
    public static AtomicInteger SAMPLE_TICKS = new AtomicInteger(5);
    public static AtomicInteger SPAWNLOC_CACHE_TICKS = new AtomicInteger(600);
    public static AtomicInteger MANAGER_CACHE_TICKS = new AtomicInteger(600);
    public static AtomicBoolean ALLOW_PAUSE = new AtomicBoolean(false);
    public static AtomicBoolean ALLOW_SLOW = new AtomicBoolean(true);
    public static AtomicBoolean DEBUG_PRINT = new AtomicBoolean(false);
    public static AtomicBoolean CLEAN_PRINT = new AtomicBoolean(true);
    public static AtomicBoolean DOWNSCALE_MAGIC_NUM = new AtomicBoolean(true);
    public static AtomicDouble  DOWNSCALE_MAGIC_NUM_MIN = new AtomicDouble(8D);
    public static AtomicInteger MAX_CHUNK_SPAWN_REQ_TICK = new AtomicInteger(12);

    //TODO save and load
}
