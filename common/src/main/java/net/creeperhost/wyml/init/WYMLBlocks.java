package net.creeperhost.wyml.init;

import net.creeperhost.polylib.registry.PolyRegistry;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.blocks.BlockPaperBag;
import net.creeperhost.wyml.tiles.TilePaperBag;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;
import java.util.function.Supplier;

public final class WYMLBlocks
{
    public static final PolyRegistry<Block> BLOCKS = PolyRegistry.create(Registries.BLOCK, WhyYouMakeLag.MOD_ID);
    public static final PolyRegistry<BlockEntityType<?>> BLOCK_ENTITIES = PolyRegistry.create(Registries.BLOCK_ENTITY_TYPE, WhyYouMakeLag.MOD_ID);
    public static final PolyRegistry<Item> ITEMS = PolyRegistry.create(Registries.ITEM, WhyYouMakeLag.MOD_ID);
    public static final PolyRegistry<CreativeModeTab> CREATIVE_TABS = PolyRegistry.create(Registries.CREATIVE_MODE_TAB, WhyYouMakeLag.MOD_ID);

    public static final Supplier<BlockPaperBag> PAPER_BAG = BLOCKS.registerBlock("paper_bag", "Paper Bag", BlockPaperBag::new);
    public static final Supplier<BlockEntityType<TilePaperBag>> PAPER_BAG_TILE = BLOCK_ENTITIES.register(
            "paper_bag",
            () -> new BlockEntityType<>(TilePaperBag::new, Set.of(PAPER_BAG.get()))
    );
    public static final Supplier<Item> PAPER_BAG_ITEM = ITEMS.registerItem(
            "paper_bag",
            "Paper Bag",
            properties -> new BlockItem(PAPER_BAG.get(), properties)
    );
    public static final Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.registerCreativeTab(
            "main",
            "Why You Make Lag",
            () -> new ItemStack(PAPER_BAG_ITEM.get()),
            (parameters, output) -> output.accept(PAPER_BAG_ITEM.get())
    );

    private WYMLBlocks()
    {
    }

    public static void init()
    {
        BLOCKS.init();
        BLOCK_ENTITIES.init();
        ITEMS.init();
        CREATIVE_TABS.init();
    }
}
