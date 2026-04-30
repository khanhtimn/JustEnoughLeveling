package dev.khanhtimn.jel.api.perk;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;

import dev.khanhtimn.jel.common.PlayerSkillData;

public record PerkContext(
		ServerPlayer player,
		PlayerSkillData tracker,
		ResourceLocation skillId,
		LootContext lootContext
) {

	public ServerLevel serverLevel() {
		return player.serverLevel();
	}
}
