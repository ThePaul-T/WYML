package net.creeperhost.wyml;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import net.creeperhost.wyml.config.CategorySpawnConfigData;
import net.creeperhost.wyml.config.MobSpawnConfigData;
import net.creeperhost.wyml.config.ModSpawnConfig;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.data.MobSpawnData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class MobManager {
    public static Jankson gson = Jankson.builder().build();
    public static boolean canManage = false;
    private static HashMap<String, ModSpawnConfig> cached = new HashMap<String, ModSpawnConfig>();
    public static void init()
    {
        for(EntityType<?> entity : Registry.ENTITY_TYPE)
        {
            if(entity.getCategory() != MobCategory.MISC) {
                ResourceLocation resourceLocation = Registry.ENTITY_TYPE.getKey(entity);
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
                _mob.limit = 8;
                switch(catName.toUpperCase(Locale.ROOT))
                {
                    case "WATER_CREATURE":
                        _mob.limit = WymlConfig.cached().WATER_CREATURES_PER_CHUNK;
                        break;
                    case "WATER_AMBIENT":
                        _mob.limit = WymlConfig.cached().WATER_AMBIENT_PER_CHUNK;
                        break;
                    case "MONSTER":
                        _mob.limit = WymlConfig.cached().MONSTER_PER_CHUNK;
                        break;
                    case "CREATURE":
                        _mob.limit = WymlConfig.cached().CREATURES_PER_CHUNK;
                        break;
                    case "AMBIENT":
                        _mob.limit = WymlConfig.cached().AMBIENT_CREATURES_PER_CHUNK;
                        break;
                }
                mod.addMob(catName, mobName, _mob);
            }
        }
        CompletableFuture.runAsync(MobManager::saveConfigs).thenRun(() -> {canManage = true;});
    }
    public static boolean saveConfigs()
    {
        Path path = WymlExpectPlatform.getConfigDirectory().resolve(WhyYouMakeLag.MOD_ID + "-SpawnRules").toAbsolutePath();
        for(String modName : cached.keySet())
        {
            ModSpawnConfig mod = cached.get(modName);
            if(mod.Save(path))
            {
                System.out.println("Wrote "+modName+" for WYML mob manager with values.");
            } else {
                System.out.println("Failed to save "+modName+" for WYML mob manager with values.");
            }
        }
        return true;
    }
    public static ModSpawnConfig getMod(String name)
    {
        if(cached.containsKey(name)) return cached.get(name);
        Path path = WymlExpectPlatform.getConfigDirectory().resolve(WhyYouMakeLag.MOD_ID + "-SpawnRules").toAbsolutePath();
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
                if(result.Save(path))
                {
                    System.out.println("Wrote "+name+" for WYML mob manager with no values.");
                } else {
                    System.out.println("Failed to save "+name+" for WYML mob manager with no values.");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return result;
        }
        if(result == null) {
            System.out.println("Error loading mob spawn config for "+name);
            result = new ModSpawnConfig(name, new CategorySpawnConfigData());
        }
        cached.put(name, result);
        return result;
    }

}
