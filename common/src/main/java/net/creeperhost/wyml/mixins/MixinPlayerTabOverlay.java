package net.creeperhost.wyml.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

    @Inject(at = @At("TAIL"), method = "extractPingIcon")
    private void renderLatency(GuiGraphicsExtractor graphics, int slotWidth, int x, int y, PlayerInfo playerInfo, CallbackInfo ci)
    {
        graphics.text(minecraft.font, playerInfo.getLatency() + " ms", x + slotWidth + 2, y, -1);
    }
}
