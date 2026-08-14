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
        return WymlMixinPolicy.shouldApply(mixin, config::enabled);
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
