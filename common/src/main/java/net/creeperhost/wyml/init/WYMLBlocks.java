package net.creeperhost.wyml.init;

import dev.architectury.hooks.block.BlockEntityHooks;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.blocks.BlockPaperBag;
import net.creeperhost.wyml.tiles.TilePaperBag;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class WYMLBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(WhyYouMakeLag.MOD_ID, Registry.BLOCK_REGISTRY);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(WhyYouMakeLag.MOD_ID, Registry.BLOCK_ENTITY_TYPE_REGISTRY);
    public static final CreativeModeTab CREATIVE_TAB = CreativeTabRegistry.create(new ResourceLocation(WhyYouMakeLag.MOD_ID, "creative_tab"), () -> new ItemStack(WYMLBlocks.PAPER_BAG.get()));


    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(WhyYouMakeLag.MOD_ID, Registry.ITEM_REGISTRY);

    public static final RegistrySupplier<Block> PAPER_BAG = BLOCKS.register("paper_bag", BlockPaperBag::new);

    public static final RegistrySupplier<BlockEntityType<TilePaperBag>> PAPER_BAG_TILE = TILES.register("paper_bag", () -> BlockEntityHooks.builder(TilePaperBag::new, PAPER_BAG.get()).build(null));

    public static final RegistrySupplier<Item> PAPER_BAG_ITEM = ITEMS.register("paper_bag", () -> new ItemNameBlockItem(PAPER_BAG.get(), new Item.Properties().tab(CREATIVE_TAB)));

    //Fence Gates
//    public static final RegistrySupplier<Block> OAK_FENCE_GATE = BLOCKS.register("oak_fence_gate", BlockMultiBlockFenceGate::new);
//    public static final RegistrySupplier<Block> SPRUCE_FENCE_GATE = BLOCKS.register("spruce_fence_gate", BlockMultiBlockFenceGate::new);
//    public static final RegistrySupplier<Block> BIRCH_FENCE_GATE = BLOCKS.register("birch_fence_gate", BlockMultiBlockFenceGate::new);
//    public static final RegistrySupplier<Block> JUNGLE_FENCE_GATE = BLOCKS.register("jungle_fence_gate", BlockMultiBlockFenceGate::new);
//    public static final RegistrySupplier<Block> ACACIA_FENCE_GATE = BLOCKS.register("acacia_fence_gate", BlockMultiBlockFenceGate::new);
//    public static final RegistrySupplier<Block> DARK_OAK_FENCE_GATE = BLOCKS.register("dark_oak_fence_gate", BlockMultiBlockFenceGate::new);
//    public static final RegistrySupplier<Block> CRIMSON_FENCE_GATE = BLOCKS.register("crimson_fence_gate", BlockMultiBlockFenceGate::new);
//    public static final RegistrySupplier<Block> WARPED_FENCE_GATE = BLOCKS.register("warped_fence_gate", BlockMultiBlockFenceGate::new);
//
//    public static final RegistrySupplier<Item> OAK_FENCE_GATE_ITEM = ITEMS.register("oak_fence_gate", () -> new ItemNameBlockItem(OAK_FENCE_GATE.get(), new Item.Properties().tab(CREATIVE_TAB)));
//    public static final RegistrySupplier<Item> SPRUCE_FENCE_GATE_ITEM = ITEMS.register("spruce_fence_gate", () -> new ItemNameBlockItem(SPRUCE_FENCE_GATE.get(), new Item.Properties().tab(CREATIVE_TAB)));
//    public static final RegistrySupplier<Item> BIRCH_FENCE_GATE_ITEM = ITEMS.register("birch_fence_gate", () -> new ItemNameBlockItem(BIRCH_FENCE_GATE.get(), new Item.Properties().tab(CREATIVE_TAB)));
//    public static final RegistrySupplier<Item> JUNGLE_FENCE_GATE_ITEM = ITEMS.register("jungle_fence_gate", () -> new ItemNameBlockItem(JUNGLE_FENCE_GATE.get(), new Item.Properties().tab(CREATIVE_TAB)));
//    public static final RegistrySupplier<Item> ACACIA_FENCE_GATE_ITEM = ITEMS.register("acacia_fence_gate", () -> new ItemNameBlockItem(ACACIA_FENCE_GATE.get(), new Item.Properties().tab(CREATIVE_TAB)));
//    public static final RegistrySupplier<Item> DARK_OAK_FENCE_GATE_ITEM = ITEMS.register("dark_oak_fence_gate", () -> new ItemNameBlockItem(DARK_OAK_FENCE_GATE.get(), new Item.Properties().tab(CREATIVE_TAB)));
//    public static final RegistrySupplier<Item> CRIMSON_FENCE_GATE_ITEM = ITEMS.register("crimson_fence_gate", () -> new ItemNameBlockItem(CRIMSON_FENCE_GATE.get(), new Item.Properties().tab(CREATIVE_TAB)));
//    public static final RegistrySupplier<Item> WARPED_FENCE_GATE_ITEM = ITEMS.register("warped_fence_gate", () -> new ItemNameBlockItem(WARPED_FENCE_GATE.get(), new Item.Properties().tab(CREATIVE_TAB)));
//
//    public static final RegistrySupplier<BlockEntityType<?>> FENCE_GATE_TILE = TILES.register("fence_gate", () -> BlockEntityType.Builder.of(TileMultiBlockFenceGate::new,
//            OAK_FENCE_GATE.get(), SPRUCE_FENCE_GATE.get(), BIRCH_FENCE_GATE.get(), JUNGLE_FENCE_GATE.get(), ACACIA_FENCE_GATE.get(), DARK_OAK_FENCE_GATE.get(), CRIMSON_FENCE_GATE.get(), WARPED_FENCE_GATE.get())
//            .build(null));

    public static void init()
    {
        BLOCKS.register();
        TILES.register();
        ITEMS.register();
    }
}
