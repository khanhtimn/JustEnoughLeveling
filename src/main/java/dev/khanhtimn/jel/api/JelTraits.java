package dev.khanhtimn.jel.api;

import dev.khanhtimn.jel.api.trait.TraitKey;
import dev.khanhtimn.jel.common.PlayerSkillData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;

public final class JelTraits {

	private JelTraits() {
	}

	public static boolean has(Player player, TraitKey key) {
		return has(player, key.id());
	}

	public static boolean has(Player player, ResourceLocation traitId) {
		PlayerSkillData data = JelSkills.getSkillData(player);
		return data != null && data.hasTrait(traitId);
	}

	public static float value(Player player, TraitKey key) {
		return value(player, key.id());
	}

	public static float value(Player player, ResourceLocation traitId) {
		PlayerSkillData data = JelSkills.getSkillData(player);
		return data != null ? data.getTraitValue(traitId) : 0f;
	}

	public static boolean testChance(Player player, TraitKey key) {
		float v = value(player, key);
		return v > 0 && player.getRandom().nextFloat() < v;
	}

	public static boolean testChance(Player player, TraitKey key, LootContext ctx) {
		float v = value(player, key);
		return v > 0 && ctx.getRandom().nextFloat() < v;
	}
}
