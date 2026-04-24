package dev.khanhtimn.jel.api;

import dev.khanhtimn.jel.common.PlayerSkillData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Static API for querying JEL traits on a player.
 * <p>
 * Traits are granted by {@code jel:trait} perks and derived from skill
 * levels — they are never persisted, only recomputed and synced.
 * <p>
 * Each trait carries a precomputed float value scaled by the player's
 * skill level via {@link net.minecraft.world.item.enchantment.LevelBasedValue}.
 * Boolean traits (no value formula) default to {@code 1.0f} when active.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Boolean check
 * if (JelTraits.has(player, "jel:double_jump")) { ... }
 *
 * // Scaled value
 * float bonus = JelTraits.value(player, "jel:bonus_regen");
 * if (bonus > 0) player.heal(bonus);
 * }</pre>
 */
public final class JelTraits {

	private JelTraits() {
	}

	public static boolean has(Player player, ResourceLocation traitId) {
		PlayerSkillData data = JelSkills.getSkillData(player);
		return data != null && data.hasTrait(traitId);
	}

	public static boolean has(Player player, String traitId) {
		return has(player, ResourceLocation.parse(traitId));
	}

	public static float value(Player player, ResourceLocation traitId) {
		PlayerSkillData data = JelSkills.getSkillData(player);
		return data != null ? data.getTraitValue(traitId) : 0f;
	}

	public static float value(Player player, String traitId) {
		return value(player, ResourceLocation.parse(traitId));
	}
}
