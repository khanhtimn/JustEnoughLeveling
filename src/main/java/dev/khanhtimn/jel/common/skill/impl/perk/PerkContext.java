package dev.khanhtimn.jel.common.skill.impl.perk;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import dev.khanhtimn.jel.common.skill.impl.PlayerSkillData;

public record PerkContext(
		ServerPlayer player,
		PlayerSkillData tracker,
		ResourceLocation skillId
) {

	public ServerLevel serverLevel() {
		return player.serverLevel();
	}
}
