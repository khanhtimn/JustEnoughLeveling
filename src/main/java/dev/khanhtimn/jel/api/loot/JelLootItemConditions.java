package dev.khanhtimn.jel.api.loot;

import dev.khanhtimn.jel.api.skill.LevelCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class JelLootItemConditions {

	public static final LootItemConditionType SKILL_LEVEL_CHECK =
			new LootItemConditionType(LevelCondition.CODEC);

	public static final LootItemConditionType TRAIT_CHANCE =
			new LootItemConditionType(JelTraitChanceCondition.CODEC);

	private JelLootItemConditions() {
	}
}
