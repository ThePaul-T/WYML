package net.creeperhost.wyml.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.containers.ContainerFence;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.time.Instant;

public class ScreenFence extends AbstractContainerScreen<ContainerFence>
{
    private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation(WhyYouMakeLag.MOD_ID, "textures/gui/fence.png");

    private final ContainerFence containerFence;

    public ScreenFence(ContainerFence containerFence, Inventory inventory, Component component)
    {
        super(containerFence, inventory, component);
        this.containerFence = containerFence;
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
