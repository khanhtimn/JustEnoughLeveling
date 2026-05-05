package dev.khanhtimn.jel.common;

import dev.khanhtimn.jel.api.loot.JelTraitChanceCondition;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Farming;
import dev.khanhtimn.jel.content.skills.Mining;
import dev.khanhtimn.jel.core.ModTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.TagEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class JelLootPoolInjector {

	private static final Set<ResourceKey<LootTable>> STONE_TABLES = Set.of(
			lootTable("blocks/stone"),
			lootTable("blocks/cobblestone"),
			lootTable("blocks/deepslate"),
			lootTable("blocks/cobbled_deepslate"),
			lootTable("blocks/andesite"),
			lootTable("blocks/granite"),
			lootTable("blocks/diorite"),
			lootTable("blocks/tuff"),
			lootTable("blocks/calcite")
	);

	private JelLootPoolInjector() {
	}

	public static List<LootPool.Builder> buildMiningPools(ResourceKey<LootTable> tableKey) {
		List<LootPool.Builder> pools = new ArrayList<>();

		if (STONE_TABLES.contains(tableKey)) {
			pools.add(gemFinderPool());
		}

		String path = tableKey.location().getPath();
		if (path.startsWith("blocks/") && isLikelyOreTable(path)) {
			pools.add(enchantedHarvestPool());
		}

		return pools;
	}

	public static LootPool.Builder gemFinderPool() {
		return LootPool.lootPool()
				.when(() -> JelTraitChanceCondition.ofBranch(
						ModSkills.MINING, Mining.GEOMANCER_BRANCH, Mining.GEM_FINDER_CHANCE))
				.add(TagEntry.expandTag(ModTags.Items.GEMS).setWeight(1));
	}

	public static LootPool.Builder enchantedHarvestPool() {
		return LootPool.lootPool()
				.when(() -> JelTraitChanceCondition.ofBranch(
						ModSkills.MINING, Mining.GEOMANCER_BRANCH, Mining.ENCHANTED_HARVEST_CHANCE))
				.add(LootItem.lootTableItem(Items.BOOK));
	}

	public static LootPool.Builder seedSavantPool(Block cropBlock) {
		return LootPool.lootPool()
				.when(() -> JelTraitChanceCondition.ofTrait(Farming.SEED_SAVANT_CHANCE))
				.add(LootItem.lootTableItem(cropBlock.asItem()));
	}

	private static boolean isLikelyOreTable(String path) {
		return path.contains("ore") || path.contains("_ore");
	}

	private static ResourceKey<LootTable> lootTable(String path) {
		return ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
				ResourceLocation.withDefaultNamespace(path));
	}
}
