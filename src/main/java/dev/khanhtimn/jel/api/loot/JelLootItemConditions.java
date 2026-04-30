package dev.khanhtimn.jel.api.loot;

import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class JelLootItemConditions {

	public static final LootItemConditionType SKILL_LEVEL_CHECK =
			new LootItemConditionType(SkillLevelCondition.CODEC);

	private JelLootItemConditions() {
	}
}
