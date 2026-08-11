package net.creeperhost.wyml.client;

import net.creeperhost.polylib.client.modulargui.ModularGui;
import net.creeperhost.polylib.client.modulargui.ModularGuiContainer;
import net.creeperhost.polylib.client.modulargui.elements.GuiRectangle;
import net.creeperhost.polylib.client.modulargui.elements.GuiScrolling;
import net.creeperhost.polylib.client.modulargui.elements.GuiSlots;
import net.creeperhost.polylib.client.modulargui.elements.GuiText;
import net.creeperhost.polylib.client.modulargui.lib.Constraints;
import net.creeperhost.polylib.client.modulargui.lib.container.ContainerGuiProvider;
import net.creeperhost.polylib.client.modulargui.lib.container.ContainerScreenAccess;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Align;
import net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint;
import net.creeperhost.wyml.containers.ContainerPaperBag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.time.Instant;

import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.match;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.Constraint.relative;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.BOTTOM;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.HEIGHT;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.LEFT;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.RIGHT;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.TOP;
import static net.creeperhost.polylib.client.modulargui.lib.geometry.GeoParam.WIDTH;

public class ScreenPaperBag extends ContainerGuiProvider<ContainerPaperBag>
{
    @Override
    public void buildGui(ModularGui gui, ContainerScreenAccess<ContainerPaperBag> screenAccess)
    {
        ContainerPaperBag menu = screenAccess.getMenu();
        gui.initStandardGui(212, 240);
        gui.setGuiTitle(Component.translatable("container.wyml.paper_bag"));

        var root = gui.getRoot();
        GuiRectangle background = new GuiRectangle(root).rectangle(0xFFD0D0D0, 0xFF202020);
        Constraints.bind(background, root);

        new GuiText(root, gui.getGuiTitle())
                .setTextColour(0xFF303030)
                .setShadow(false)
                .constrain(TOP, relative(root.get(TOP), 7))
                .constrain(LEFT, relative(root.get(LEFT), 8))
                .constrain(RIGHT, relative(root.get(RIGHT), -8))
                .constrain(HEIGHT, Constraint.literal(10));

        new GuiText(root, () -> Component.literal(statusText(menu)))
                .setTextColour(0xFFAA2020)
                .setShadow(false)
                .setAlignment(Align.MAX)
                .constrain(TOP, relative(root.get(TOP), 7))
                .constrain(LEFT, relative(root.get(LEFT), 70))
                .constrain(RIGHT, relative(root.get(RIGHT), -8))
                .constrain(HEIGHT, Constraint.literal(10));

        var scrollWindow = GuiScrolling.simpleScrollWindow(root, true, false);
        scrollWindow.container
                .constrain(TOP, relative(root.get(TOP), 22))
                .constrain(LEFT, relative(root.get(LEFT), 15))
                .constrain(WIDTH, Constraint.literal(182))
                .constrain(HEIGHT, Constraint.literal(112));

        var scrollContent = scrollWindow.primary.getContentElement();
        new GuiSlots(scrollContent, screenAccess, menu.paperBag, 9)
                .constrain(TOP, match(scrollContent.get(TOP)))
                .constrain(LEFT, match(scrollContent.get(LEFT)));

        var playerSlots = GuiSlots.player(root, screenAccess, menu.playerMain, menu.playerHotbar);
        playerSlots.container
                .constrain(LEFT, Constraint.midPoint(root.get(LEFT), root.get(RIGHT), -81))
                .constrain(BOTTOM, relative(root.get(BOTTOM), -8));
    }

    private static String statusText(ContainerPaperBag menu)
    {
        long remaining = Math.max(0, menu.tile.getDespawnTime() - Instant.now().getEpochSecond());
        return String.format("%d/%d slots  %02d:%02d", menu.tile.getUsedSlots(), menu.tile.getInventory().getContainerSize(), remaining / 60, remaining % 60);
    }

    public static ModularGuiContainer<ContainerPaperBag> create(ContainerPaperBag menu, Inventory inventory, Component title)
    {
        return new ModularGuiContainer<>(menu, inventory, new ScreenPaperBag());
    }
}
