package net.creeperhost.wyml;

import blue.endless.jankson.Jankson;
import net.creeperhost.wyml.config.CategorySpawnConfigData;
import net.creeperhost.wyml.config.MobSpawnConfigData;
import net.creeperhost.wyml.config.ModSpawnConfig;
import net.creeperhost.wyml.data.MobSpawnData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class MobManager {
    private static Jankson gson = Jankson.builder().build();
    public static void init()
    {
        //TODO: Figure out when to actually do this
        for(EntityType<?> entity : Registry.ENTITY_TYPE)
        {
            ResourceLocation resourceLocation = Registry.ENTITY_TYPE.getKey(entity);
            String modName = resourceLocation.getNamespace();
            String mobName = resourceLocation.getPath();
            String catName = entity.getCategory().getName();

            ModSpawnConfig mod = getMod(modName);

            MobSpawnConfigData cat = mod.getCategory(catName);
            if(cat == null) mod.addCategory(catName);

            MobSpawnData mob = mod.getMob(mobName);
            if(mob != null) continue;

            MobSpawnData _mob = new MobSpawnData();
            _mob.name = mobName;
            //TODO: Replace with the configured category limit
            _mob.limit = 8;
            mod.addMob(catName, mobName, _mob);
        }
    }
    public static ModSpawnConfig getMod(String name)
    {
        Path path = WymlExpectPlatform.getConfigDirectory().resolve(WhyYouMakeLag.MOD_ID + "-SpawnRules").toAbsolutePath();
        Path file = path.resolve(name+".json");
        CategorySpawnConfigData tmp = new CategorySpawnConfigData();
        ModSpawnConfig result = new ModSpawnConfig(name, tmp);
        try {
            if(Files.notExists(path)) {
                Files.createDirectories(path);
            }
            if(Files.exists(file))
            {
                result = gson.fromJson(gson.load(file.toFile()), ModSpawnConfig.class);
            } else {
                try (FileOutputStream configOut = new FileOutputStream(file.toFile()))
                {
                    IOUtils.write(gson.toJson(result).toJson(true, true), configOut, Charset.defaultCharset());
                } catch (Throwable ignored)
                {
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
