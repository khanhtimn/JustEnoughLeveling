package dev.khanhtimn.jel.api.loot;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Optional;

public final class JelLootContexts {

	public static LootContext entity(ServerLevel level, Entity entity, int skillLevel) {
		LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, entity)
				.withParameter(JelLootContextParams.SKILL_LEVEL, skillLevel)
				.withParameter(LootContextParams.ORIGIN, entity.position())
				.create(JelLootContextParamSets.SKILL_ENTITY);
		return new LootContext.Builder(params).create(Optional.empty());
	}

	public static LootContext damage(ServerLevel level, Entity entity,
	                                  int skillLevel, DamageSource source) {
		LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, entity)
				.withParameter(JelLootContextParams.SKILL_LEVEL, skillLevel)
				.withParameter(LootContextParams.ORIGIN, entity.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, source)
				.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
				.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity())
				.create(JelLootContextParamSets.SKILL_DAMAGE);
		return new LootContext.Builder(params).create(Optional.empty());
	}

	public static LootContext item(ServerLevel level, Entity entity,
	                                int skillLevel, ItemStack tool) {
		LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, entity)
				.withParameter(JelLootContextParams.SKILL_LEVEL, skillLevel)
				.withParameter(LootContextParams.ORIGIN, entity.position())
				.withParameter(LootContextParams.TOOL, tool)
				.create(JelLootContextParamSets.SKILL_ITEM);
		return new LootContext.Builder(params).create(Optional.empty());
	}

	private JelLootContexts() {
	}
}
