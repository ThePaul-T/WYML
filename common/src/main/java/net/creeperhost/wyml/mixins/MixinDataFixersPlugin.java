package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.WymlConfig;
import net.creeperhost.wyml.WymlExpectPlatform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixinDataFixersPlugin implements IMixinConfigPlugin
{
    @Override
    public void onLoad(String mixinPackage)
    {
        if(!WymlConfig.isLoaded()) WymlConfig.init(WymlExpectPlatform.getConfigDirectory().resolve(WhyYouMakeLag.MOD_ID + ".json").toFile());
    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        if(!WymlConfig.isLoaded()) WymlConfig.init(WymlExpectPlatform.getConfigDirectory().resolve(WhyYouMakeLag.MOD_ID + ".json").toFile());
        if(targetClassName.equalsIgnoreCase("net.minecraft.util.datafix.DataFixers"))
        {
            if(WymlConfig.cached().ENABLE_DFU) {
                return false;
            }
        }
        return true;
    }

    public List<String> stringList = new ArrayList<>();

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
