package net.creeperhost.wyml.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class MixinPlayerTabOverlay
{
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(at = @At("TAIL"), method = "renderPingIcon", cancellable = true)
    private void render(PoseStack poseStack, int i, int j, int k, PlayerInfo playerInfo, CallbackInfo ci)
    {
        minecraft.font.drawShadow(poseStack, "" + playerInfo.getLatency() + " ms", j + i + 2, k, -1);
    }
}
