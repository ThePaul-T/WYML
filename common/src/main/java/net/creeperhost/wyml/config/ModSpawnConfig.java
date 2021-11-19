package net.creeperhost.wyml.config;

import net.creeperhost.wyml.MobManager;
import net.creeperhost.wyml.data.MobSpawnData;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;

public class ModSpawnConfig {
    private String mod;
    transient private boolean modified = false;
    private CategorySpawnConfigData spawn;
    public ModSpawnConfig(){}
    public ModSpawnConfig(String name, CategorySpawnConfigData spawns)
    {
        this.mod = name;
        this.spawn = spawns;
    }
    public boolean Save(Path path)
    {
        Path file = path.resolve(getName()+".json");
        try (FileOutputStream configOut = new FileOutputStream(file.toFile()))
        {
            IOUtils.write(MobManager.gson.toJson(this).toJson(true, true), configOut, Charset.defaultCharset());
        } catch (Throwable t)
        {
            t.printStackTrace();
            return false;
        }
        modified = false;
        return true;
    }
    public String getName()
    {
        return mod;
    }
    public MobSpawnConfigData getCategory(String categoryName)
    {
        if(spawn.categories == null) spawn.categories = new HashMap<>();
        if(spawn.categories.containsKey(categoryName)) {
            return spawn.categories.get(categoryName);
        }
        return null;
    }
    public MobSpawnConfigData addCategory(String categoryName)
    {
        if(spawn.categories == null)
        {
            spawn.categories = new HashMap<String, MobSpawnConfigData>();
        }
        if(!spawn.categories.containsKey(categoryName)) {
            spawn.categories.put(categoryName, new MobSpawnConfigData());
        }
        modified = true;
        return getCategory(categoryName);
    }
    public MobSpawnData getMob(String mobName)
    {
        if(spawn.categories == null) return null;
        for(String cat : spawn.categories.keySet())
        {
            MobSpawnConfigData _c = spawn.categories.get(cat);
            if(_c == null) continue;
            for(String mob : _c.spawns.keySet())
            {
                if(mob.equals(mobName))
                {
                    return _c.spawns.get(mob);
                }
            }
        }
        return null;
    }
    public boolean addMob(String categoryName, String mobName, MobSpawnData mobSpawnData)
    {
        MobSpawnConfigData cat = getCategory(categoryName);
        if(cat == null) return false;
        if(cat.spawns == null)
        {
            cat.spawns = new HashMap<String, MobSpawnData>();
        }
        if(cat.spawns.containsKey(mobName)) return false;
        cat.spawns.put(mobName, mobSpawnData);
        modified = true;
        return true;
    }
    public boolean updateMob(String categoryName, String mobName, MobSpawnData mobSpawnData)
    {
        MobSpawnConfigData cat = getCategory(categoryName);
        if(cat == null) return false;
        if(cat.spawns == null) return false;
        if(!cat.spawns.containsKey(mobName)) return false;
        cat.spawns.put(mobName, mobSpawnData);
        modified = true;
        return true;
    }
}
