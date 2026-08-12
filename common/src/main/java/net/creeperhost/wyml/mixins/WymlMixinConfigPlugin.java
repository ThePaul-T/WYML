package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.config.WymlBootConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class WymlMixinConfigPlugin implements IMixinConfigPlugin
{
    private WymlBootConfig config;

    @Override
    public void onLoad(String mixinPackage)
    {
        config = WymlBootConfig.load();
    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        if (!config.masterEnabled())
        {
            return false;
        }

        String mixin = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
        return switch (mixin)
        {
            case "MixinEntity" -> any("item_lifetime", "entity_pushing");
            case "MixinItemEntity" -> config.enabled("item_lifetime");
            case "MixinItemEntityMergeScheduling" -> config.enabled("item_merging");
            case "MixinLivingEntity" -> config.enabled("entity_pushing");
            case "MixinMobCategory" -> config.enabled("category_policy");
            case "MixinNaturalSpawner" -> any("spawn_controller", "per_mob_rules");
            case "AccessorMinecraftServer" -> config.enabled("spawn_controller");
            case "MixinSpawnState" -> config.enabled("spawn_controller");
            case "MixinSpawnStateCategoryPolicy" -> config.enabled("category_policy");
            case "MixinServerWorld" -> any("paper_bags", "per_mob_rules");
            case "MixinMinecraftServer" -> any(
                    "spawn_controller", "category_policy", "per_mob_rules", "post_load_gc");
            case "MixinPlayerTabOverlay" -> config.enabled("numeric_ping");
            default -> true;
        };
    }

    private boolean any(String... modules)
    {
        for (String module : modules)
        {
            if (config.enabled(module))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
    {
    }

    @Override
    public List<String> getMixins()
    {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {
    }
}
