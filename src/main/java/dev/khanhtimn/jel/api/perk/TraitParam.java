package dev.khanhtimn.jel.api.perk;

import dev.khanhtimn.jel.api.trait.TraitKey;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

public record TraitParam(
		TraitKey key,
		LevelBasedValue formula,
		int unlockLevel,
		@Nullable LootItemCondition condition
) {
	public static TraitParam of(TraitKey key, LevelBasedValue formula, int unlockLevel) {
		return new TraitParam(key, formula, unlockLevel, null);
	}

	public static TraitParam of(TraitKey key, LevelBasedValue formula, int unlockLevel,
	                             @Nullable LootItemCondition condition) {
		return new TraitParam(key, formula, unlockLevel, condition);
	}
}
