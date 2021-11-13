package net.creeperhost.wyml.config;

import net.creeperhost.wyml.data.MobSpawnData;

import java.util.HashMap;

public class ModSpawnConfig {
    private String mod;
    private CategorySpawnConfigData spawn;
    public ModSpawnConfig(String name, CategorySpawnConfigData spawns)
    {
        this.mod = name;
        this.spawn = spawns;
    }
    public String getName()
    {
        return mod;
    }
    public MobSpawnConfigData getCategory(String categoryName)
    {
        if(spawn.Categories != null && spawn.Categories.containsKey(categoryName)) {
            return spawn.Categories.get(categoryName);
        }
        return null;
    }
    public MobSpawnConfigData addCategory(String categoryName)
    {
        if(spawn.Categories == null)
        {
            spawn.Categories = new HashMap<String, MobSpawnConfigData>();
        }
        if(!spawn.Categories.containsKey(categoryName)) {
            MobSpawnConfigData data = new MobSpawnConfigData();
            data.Spawns = new HashMap<String, MobSpawnData>();
            spawn.Categories.put(categoryName, data);
        }
        return getCategory(categoryName);
    }
    public MobSpawnData getMob(String mobName)
    {
        if(spawn.Categories == null) return null;
        for(String cat : spawn.Categories.keySet())
        {
            MobSpawnConfigData _c = spawn.Categories.get(cat);
            if(_c == null) continue;
            for(String mob : _c.Spawns.keySet())
            {
                if(mob == mobName)
                {
                    return _c.Spawns.get(mob);
                }
            }
        }
        return null;
    }
    public boolean addMob(String categoryName, String mobName, MobSpawnData mobSpawnData)
    {
        MobSpawnConfigData cat = getCategory(categoryName);
        if(cat == null) return false;
        if(cat.Spawns == null)
        {
            cat.Spawns = new HashMap<String, MobSpawnData>();
        }
        if(cat.Spawns.containsKey(mobName)) return false;
        cat.Spawns.put(mobName, mobSpawnData);
        return true;
    }
    public boolean updateMob(String categoryName, String mobName, MobSpawnData mobSpawnData)
    {
        MobSpawnConfigData cat = getCategory(categoryName);
        if(cat == null) return false;
        if(cat.Spawns == null) return false;
        if(!cat.Spawns.containsKey(mobName)) return false;
        cat.Spawns.put(mobName, mobSpawnData);
        return true;
    }
}
