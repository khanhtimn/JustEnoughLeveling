package dev.khanhtimn.jel.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {

	public static final class Blocks {

		public static final TagKey<Block> ORES = cBlock("ores");
		public static final TagKey<Block> STONES = cBlock("stones");
		public static final TagKey<Block> COBBLESTONES = cBlock("cobblestones");
		public static final TagKey<Block> STORAGE_BLOCKS = cBlock("storage_blocks");
		public static final TagKey<Block> BUDDING_BLOCKS = cBlock("budding_blocks");

		private static TagKey<Block> cBlock(String path) {
			return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
		}

		private Blocks() {
		}
	}

	public static final class Items {

		public static final TagKey<Item> GEMS = cItem("gems");
		public static final TagKey<Item> RAW_MATERIALS = cItem("raw_materials");
		public static final TagKey<Item> DUSTS = cItem("dusts");
		public static final TagKey<Item> INGOTS = cItem("ingots");
		public static final TagKey<Item> NUGGETS = cItem("nuggets");
		public static final TagKey<Item> ORES = cItem("ores");

		public static final TagKey<Item> TOOLS = cItem("tools");
		public static final TagKey<Item> MINING_TOOLS = cItem("tools/mining_tool");
		public static final TagKey<Item> MELEE_WEAPONS = cItem("tools/melee_weapon");
		public static final TagKey<Item> RANGED_WEAPONS = cItem("tools/ranged_weapon");
		public static final TagKey<Item> BOWS = cItem("tools/bow");
		public static final TagKey<Item> CROSSBOWS = cItem("tools/crossbow");
		public static final TagKey<Item> SHIELDS = cItem("tools/shield");

		public static final TagKey<Item> ARMORS = cItem("armors");

		public static final TagKey<Item> FOODS = cItem("foods");
		public static final TagKey<Item> CROPS = cItem("crops");
		public static final TagKey<Item> SEEDS = cItem("seeds");

		public static final TagKey<Item> ENCHANTABLES = cItem("enchantables");

		private static TagKey<Item> cItem(String path) {
			return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
		}

		private Items() {
		}
	}

	private ModTags() {
	}
}
