package net.creeperhost.wyml.init;

import me.shedaniel.architectury.hooks.BlockEntityHooks;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.RegistrySupplier;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.blocks.BlockPaperBag;
import net.creeperhost.wyml.blocks.TilePaperBag;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class WYMLBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(WhyYouMakeLag.MOD_ID, Registry.BLOCK_REGISTRY);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(WhyYouMakeLag.MOD_ID, Registry.BLOCK_ENTITY_TYPE_REGISTRY);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(WhyYouMakeLag.MOD_ID, Registry.ITEM_REGISTRY);

    public static final RegistrySupplier<Block> PAPER_BAG = BLOCKS.register("paper_bag", BlockPaperBag::new);
    public static final RegistrySupplier<BlockEntityType<?>> PAPER_BAG_TILE = TILES.register("paper_bag", () ->
            BlockEntityType.Builder.of(TilePaperBag::new, PAPER_BAG.get()).build(null));


    public static final RegistrySupplier<Item> PAPER_BAG_ITEM = ITEMS.register("paper_bag",
            () -> new ItemNameBlockItem(PAPER_BAG.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static void init()
    {
        BLOCKS.register();
        TILES.register();
        ITEMS.register();
    }
}
