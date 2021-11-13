package net.creeperhost.wyml.config;

import blue.endless.jankson.Comment;
import net.creeperhost.wyml.data.MobSpawnData;

import java.util.HashMap;

public class CategorySpawnConfigData {
    @Comment("Set the max amount of spawn tries base rate")
    public HashMap<String, MobSpawnConfigData> Categories;
}
