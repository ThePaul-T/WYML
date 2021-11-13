package net.creeperhost.wyml.config;

import blue.endless.jankson.Comment;
import net.creeperhost.wyml.data.MobSpawnData;

import java.util.HashMap;

public class MobSpawnConfigData {
    @Comment("Per mob spawning limits")
    public HashMap<String, MobSpawnData> Spawns;
}
