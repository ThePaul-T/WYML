package net.creeperhost.wyml;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import net.creeperhost.wyml.config.CategorySpawnConfigData;
import net.creeperhost.wyml.config.MobSpawnConfigData;
import net.creeperhost.wyml.config.ModSpawnConfig;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.data.MobSpawnData;
import net.creeperhost.wyml.spawn.PerMobLimitPolicy;
import net.creeperhost.polylib.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MobManager {
    public static Jankson gson = Jankson.builder().build();
    public static volatile boolean canManage = false;
    private static final Map<String, ModSpawnConfig> cached = new ConcurrentHashMap<>();
    public static void init()
    {
        canManage = false;
        cached.clear();
        for(EntityType<?> entity : BuiltInRegistries.ENTITY_TYPE)
        {
            if(entity.getCategory() != MobCategory.MISC) {
                Identifier resourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity);
                String modName = resourceLocation.getNamespace();
                String mobName = resourceLocation.getPath();
                String catName = entity.getCategory().getName();

                ModSpawnConfig mod = getMod(modName);

                MobSpawnConfigData cat = mod.getCategory(catName);
                if (cat == null) mod.addCategory(catName);

                MobSpawnData mob = mod.getMob(mobName);
                if (mob != null) continue;

                MobSpawnData _mob = new MobSpawnData();
                _mob.name = mobName;
                _mob.limit = PerMobLimitPolicy.defaultLimit(catName, WymlConfig.cached());
                mod.addMob(catName, mobName, _mob);
            }
        }
        saveConfigs();
        canManage = true;
    }
    public static boolean saveConfigs()
    {
        Path path = Services.PLATFORM.getConfigFolder().resolve(WhyYouMakeLag.MOD_ID + "-SpawnRules").toAbsolutePath();
        boolean success = true;
        for(Map.Entry<String, ModSpawnConfig> entry : cached.entrySet())
        {
            String modName = entry.getKey();
            ModSpawnConfig mod = entry.getValue();
            if(mod.save(path))
            {
                System.out.println("Wrote "+modName+" for WYML mob manager with values.");
            } else {
                System.out.println("Failed to save "+modName+" for WYML mob manager with values.");
                success = false;
            }
        }
        return success;
    }
    public static ModSpawnConfig getMod(String name)
    {
        ModSpawnConfig cachedConfig = cached.get(name);
        if (cachedConfig != null) return cachedConfig;
        Path path = Services.PLATFORM.getConfigFolder().resolve(WhyYouMakeLag.MOD_ID + "-SpawnRules").toAbsolutePath();
        Path file = path.resolve(name+".json");
        CategorySpawnConfigData tmp = new CategorySpawnConfigData();
        tmp.categories = new HashMap<>();
        ModSpawnConfig result = new ModSpawnConfig(name, tmp);
        try {
            if(Files.notExists(path)) {
                Files.createDirectories(path);
            }
            if(Files.exists(file))
            {
                JsonObject jsonObj = gson.load(file.toFile());
                result = gson.fromJson(jsonObj, ModSpawnConfig.class);
                System.out.println("Loaded "+file+" for WYML mob manager.");
            } else {
                System.out.println("Preparing new WYML mob rules for "+name+".");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if(result == null) {
            System.out.println("Error loading mob spawn config for "+name);
            result = new ModSpawnConfig(name, new CategorySpawnConfigData());
        }
        result.normalize(name);
        ModSpawnConfig raced = cached.putIfAbsent(name, result);
        return raced == null ? result : raced;
    }

}
