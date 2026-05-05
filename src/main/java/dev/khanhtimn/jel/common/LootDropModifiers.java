package dev.khanhtimn.jel.common;

import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Farming;
import dev.khanhtimn.jel.content.skills.Mining;
import dev.khanhtimn.jel.core.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class LootDropModifiers {

	private LootDropModifiers() {
	}

	public static void modifyMiningDrops(ServerPlayer player, List<ItemStack> drops,
										 BlockState state, ServerLevel level) {
		if (JelSkills.getLevel(player, ModSkills.MINING) <= 0) return;

		boolean isOre = state.is(ModTags.Blocks.ORES);
		applyBonusDrop(player, drops);
		applyMotherLode(player, drops, isOre);
		applyHeartOfMountain(player, drops, isOre);
		applyGemFinder(player, drops, state);
		applyEnchantedHarvest(player, drops, isOre);
		applyMidasTouch(player, drops, level);
	}

	public static void modifyFarmingDrops(ServerPlayer player, List<ItemStack> drops, BlockState state) {
		applyHarvestYield(player, drops, state);
		applyBountifulHarvest(player, drops, state);
		applySeedSavant(player, drops, state);
		applyGaiasBlessing(player, drops, state);
	}

	// ===== Mining Multiplicative =====

	private static void applyBonusDrop(ServerPlayer player, List<ItemStack> drops) {
		if (!JelTraits.testChance(player, Mining.BONUS_DROP_CHANCE)) return;
		int size = drops.size();
		for (int i = 0; i < size; i++) {
			drops.add(drops.get(i).copy());
		}
	}

	private static void applyMotherLode(ServerPlayer player, List<ItemStack> drops, boolean isOre) {
		if (!isOre) return;
		if (!JelTraits.branchTestChance(player, ModSkills.MINING,
				Mining.EXCAVATOR_BRANCH, Mining.MOTHER_LODE_CHANCE)) return;
		int size = drops.size();
		for (int i = 0; i < size; i++) {
			drops.add(drops.get(i).copy());
			drops.add(drops.get(i).copy());
		}
	}

	private static void applyHeartOfMountain(ServerPlayer player, List<ItemStack> drops, boolean isOre) {
		if (!isOre) return;
		float active = JelTraits.branchValue(player, ModSkills.MINING,
				Mining.GEOMANCER_BRANCH, Mining.HEART_OF_MOUNTAIN_ACTIVE);
		if (active <= 0) return;
		int size = drops.size();
		for (int i = 0; i < size; i++) {
			drops.add(drops.get(i).copy());
		}
	}

	// ===== Mining Additive =====

	private static void applyGemFinder(ServerPlayer player, List<ItemStack> drops, BlockState state) {
		if (!state.is(ModTags.Blocks.STONES)
				&& !state.is(ModTags.Blocks.COBBLESTONES)
				&& !state.is(BlockTags.BASE_STONE_OVERWORLD)) return;
		if (!JelTraits.branchTestChance(player, ModSkills.MINING,
				Mining.GEOMANCER_BRANCH, Mining.GEM_FINDER_CHANCE)) return;
		drops.add(randomGem(player));
	}

	private static void applyEnchantedHarvest(ServerPlayer player, List<ItemStack> drops, boolean isOre) {
		if (!isOre) return;
		if (!JelTraits.branchTestChance(player, ModSkills.MINING,
				Mining.GEOMANCER_BRANCH, Mining.ENCHANTED_HARVEST_CHANCE)) return;
		drops.add(new ItemStack(Items.BOOK));
	}

	// ===== Mining Transformative =====

	private static void applyMidasTouch(ServerPlayer player, List<ItemStack> drops, ServerLevel level) {
		float chance = JelTraits.branchValue(player, ModSkills.MINING,
				Mining.GEOMANCER_BRANCH, Mining.MIDAS_TOUCH_CHANCE);
		if (chance <= 0 || player.getRandom().nextFloat() >= chance) return;

		for (int i = 0; i < drops.size(); i++) {
			ItemStack stack = drops.get(i);
			SingleRecipeInput input = new SingleRecipeInput(stack);
			var optional = level.getRecipeManager()
					.getRecipeFor(RecipeType.SMELTING, input, level);
			if (optional.isPresent()) {
				ItemStack result = optional.get().value().getResultItem(level.registryAccess()).copy();
				result.setCount(stack.getCount());
				drops.set(i, result);
			}
		}
	}

	// ===== Farming Multiplicative =====

	private static void applyHarvestYield(ServerPlayer player, List<ItemStack> drops, BlockState state) {
		if (!(state.getBlock() instanceof CropBlock)) return;
		float multiplier = JelTraits.value(player, Farming.HARVEST_YIELD_MULTIPLIER);
		if (multiplier <= 0 || Math.abs(multiplier - 1.0f) < 0.001f) return;

		for (int i = 0; i < drops.size(); i++) {
			ItemStack stack = drops.get(i);
			if (stack.isEmpty()) continue;
			float scaledCount = stack.getCount() * multiplier;
			int baseCount = (int) scaledCount;
			float fractional = scaledCount - baseCount;
			if (fractional > 0 && player.getRandom().nextFloat() < fractional) {
				baseCount++;
			}
			if (baseCount <= 0) {
				drops.set(i, ItemStack.EMPTY);
			} else {
				stack.setCount(baseCount);
			}
		}
		drops.removeIf(ItemStack::isEmpty);
	}

	private static void applyBountifulHarvest(ServerPlayer player, List<ItemStack> drops, BlockState state) {
		if (!(state.getBlock() instanceof CropBlock crop)) return;
		if (!crop.isMaxAge(state)) return;
		if (!JelTraits.branchTestChance(player, ModSkills.FARMING,
				Farming.CULTIVATOR_BRANCH, Farming.BOUNTIFUL_HARVEST_CHANCE)) return;

		int size = drops.size();
		for (int i = 0; i < size; i++) {
			drops.add(drops.get(i).copy());
		}
	}

	// ===== Farming Additive =====

	private static void applySeedSavant(ServerPlayer player, List<ItemStack> drops, BlockState state) {
		if (!(state.getBlock() instanceof CropBlock crop)) return;
		if (!crop.isMaxAge(state)) return;
		if (!JelTraits.testChance(player, Farming.SEED_SAVANT_CHANCE)) return;

		Item seedItem = crop.asItem();
		for (ItemStack stack : drops) {
			if (stack.is(seedItem)) {
				stack.grow(1);
				return;
			}
		}
		drops.add(new ItemStack(seedItem));
	}

	private static void applyGaiasBlessing(ServerPlayer player, List<ItemStack> drops, BlockState state) {
		if (!(state.getBlock() instanceof CropBlock crop)) return;
		if (!crop.isMaxAge(state)) return;
		float active = JelTraits.branchValue(player, ModSkills.FARMING,
				Farming.CULTIVATOR_BRANCH, Farming.GAIAS_BLESSING_ACTIVE);
		if (active <= 0) return;

		int size = drops.size();
		for (int i = 0; i < size; i++) {
			drops.add(drops.get(i).copy());
		}
	}

	// ===== Helpers =====

	private static ItemStack randomGem(ServerPlayer player) {
		Iterable<Holder<Item>> gems = BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.Items.GEMS);
		List<Item> gemList = new ArrayList<>();
		for (Holder<Item> holder : gems) {
			gemList.add(holder.value());
		}
		if (gemList.isEmpty()) {
			return new ItemStack(Items.AMETHYST_SHARD);
		}
		return new ItemStack(gemList.get(player.getRandom().nextInt(gemList.size())));
	}
}
