package net.creeperhost.wyml.mixins;

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

    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        /*if(stringList.contains(targetClassName) || stringList.contains(mixinClassName))
        {
            System.out.println("[WYML] Removing " + mixinClassName + " due to conflicting mixin target");
            return false;
        }*/
        return true;
    }

    public List<String> stringList = new ArrayList<>();

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
    {
        List<String> copy = new ArrayList<>(myTargets);

        for(String ourTarget : copy)
        {
            if(otherTargets.contains(ourTarget))
            {
                stringList.add(ourTarget);
                myTargets.remove(ourTarget);
            }
        }
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
