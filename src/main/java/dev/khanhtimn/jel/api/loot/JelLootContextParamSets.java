package dev.khanhtimn.jel.api.loot;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class JelLootContextParamSets {

	public static final LootContextParamSet SKILL_ENTITY = LootContextParamSet.builder()
			.required(LootContextParams.THIS_ENTITY)
			.required(JelLootContextParams.SKILL_LEVEL)
			.required(LootContextParams.ORIGIN)
			.build();

	public static final LootContextParamSet SKILL_DAMAGE = LootContextParamSet.builder()
			.required(LootContextParams.THIS_ENTITY)
			.required(JelLootContextParams.SKILL_LEVEL)
			.required(LootContextParams.ORIGIN)
			.required(LootContextParams.DAMAGE_SOURCE)
			.optional(LootContextParams.ATTACKING_ENTITY)
			.optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
			.build();

	public static final LootContextParamSet SKILL_ITEM = LootContextParamSet.builder()
			.required(LootContextParams.THIS_ENTITY)
			.required(JelLootContextParams.SKILL_LEVEL)
			.required(LootContextParams.ORIGIN)
			.required(LootContextParams.TOOL)
			.build();

	private JelLootContextParamSets() {
	}
}
