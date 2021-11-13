package net.creeperhost.wyml.config;

import blue.endless.jankson.Comment;
import net.creeperhost.wyml.data.MobSpawnData;

import java.util.HashMap;

public class MobSpawnConfigData {
    public MobSpawnConfigData()
    {
        spawns = new HashMap<>();
    }
    @Comment("Set the limit per mob within this category")
    public HashMap<String, MobSpawnData> spawns;
}
