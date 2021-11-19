package net.creeperhost.wyml.config;

import blue.endless.jankson.Comment;

import java.util.HashMap;

public class CategorySpawnConfigData {
    public CategorySpawnConfigData()
    {
        categories = new HashMap<>();
    }
    @Comment("Array of all categories this mod has spawns for")
    public HashMap<String, MobSpawnConfigData> categories;
}
