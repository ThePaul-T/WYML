package net.creeperhost.wyml.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.wyml.containers.ContainerPaperBag;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.time.Instant;

public class ScreenPaperBag extends AbstractContainerScreen<ContainerPaperBag>
{
    private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");

    private final ContainerPaperBag containerPaperBag;
    private long lastTimeRender = 0;
    private String renderString = "";

    public ScreenPaperBag(ContainerPaperBag containerPaperBag, Inventory inventory, Component component)
    {
        super(containerPaperBag, inventory, component);
        this.containerPaperBag = containerPaperBag;
        ++this.imageHeight;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        this.renderBackground(poseStack);
        super.render(poseStack, i, j, f);
        this.renderTooltip(poseStack, i, j);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j)
    {
        this.font.draw(poseStack, this.inventory.getDisplayName(), (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
        long despawn = containerPaperBag.getTilePaperBag().getDespawnTime();
        long timeSeconds = (despawn - Instant.now().getEpochSecond());
        this.font.draw(poseStack, ChatFormatting.RED + format(timeSeconds), this.titleLabelX, (float) this.titleLabelY, 4210752);
        //TODO Come back to this when brain decides to work
        this.font.draw(poseStack, ChatFormatting.RED + "Slots " + containerPaperBag.getTilePaperBag().getUsedSlots() + "/" + containerPaperBag.getTilePaperBag().getContainerSize(), imageWidth - 70, (float) this.titleLabelY, 4210752);

    }

    public String format(long timeSeconds)
    {
        if (lastTimeRender != timeSeconds)
        {
            long minutes = (timeSeconds % 3600) / 60;
            long seconds = timeSeconds % 60;
            lastTimeRender = timeSeconds;
            renderString = "Despawns in " + padLeftZeros("" + minutes, 2) + ":" + padLeftZeros("" + seconds, 2);
        }
        return renderString;
    }

    public String padLeftZeros(String inputString, int length)
    {
        if (inputString.length() >= length)
        {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length())
        {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(CONTAINER_TEXTURE);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
    }
}
