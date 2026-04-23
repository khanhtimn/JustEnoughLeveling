package dev.khanhtimn.jel.common.skill.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * Data-driven bidirectional conversion between vanilla XP and skill XP.
 * <p>
 * Each direction has its own {@link LevelBasedValue} rate, computed as a
 * function of the player's current skill level. This allows per-level,
 * per-direction control with full LBV flexibility (constant, linear, lookup,
 * clamped, etc.).
 * <p>
 * Configured per-skill via the {@code xp_conversion} field in the
 * {@link SkillDefinition} datapack JSON. If omitted, defaults to identity
 * (1:1 ratio in both directions).
 *
 * <h2>JSON examples</h2>
 * <pre>{@code
 * // Identity (default — can be omitted)
 * "xp_conversion": {
 *   "vanilla_to_skill_rate": 1.0,
 *   "skill_to_vanilla_rate": 1.0
 * }
 *
 * // Fixed 2:1 ratio
 * "xp_conversion": {
 *   "vanilla_to_skill_rate": 0.5,
 *   "skill_to_vanilla_rate": 2.0
 * }
 *
 * // Level-scaled with clamping
 * "xp_conversion": {
 *   "vanilla_to_skill_rate": {
 *     "type": "clamped",
 *     "value": { "type": "linear", "base": 1.0, "per_level_above_first": -0.05 },
 *     "min": 0.1, "max": 1.0
 *   },
 *   "skill_to_vanilla_rate": {
 *     "type": "linear", "base": 1.0, "per_level_above_first": 0.05
 *   }
 * }
 * }</pre>
 */
public record XpConversion(
		LevelBasedValue vanillaToSkillRate,
		LevelBasedValue skillToVanillaRate
) {

	public static final XpConversion IDENTITY = new XpConversion(
			LevelBasedValue.constant(1f),
			LevelBasedValue.constant(1f)
	);

	public static final Codec<XpConversion> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					LevelBasedValue.CODEC
							.optionalFieldOf("vanilla_to_skill_rate", LevelBasedValue.constant(1f))
							.forGetter(XpConversion::vanillaToSkillRate),
					LevelBasedValue.CODEC
							.optionalFieldOf("skill_to_vanilla_rate", LevelBasedValue.constant(1f))
							.forGetter(XpConversion::skillToVanillaRate)
			).apply(instance, XpConversion::new)
	);


	public static XpConversion identity() {
		return IDENTITY;
	}

	/**
	 * Fixed ratio: {@code vanillaPerSkillXp} vanilla XP per 1 skill XP.
	 * Refund uses the inverse rate.
	 *
	 * @param vanillaPerSkillXp e.g. 2.0 means 2 vanilla XP = 1 skill XP
	 */
	public static XpConversion ratio(float vanillaPerSkillXp) {
		return new XpConversion(
				LevelBasedValue.constant(1f / vanillaPerSkillXp),
				LevelBasedValue.constant(vanillaPerSkillXp)
		);
	}

	public static XpConversion of(LevelBasedValue vanillaToSkill, LevelBasedValue skillToVanilla) {
		return new XpConversion(vanillaToSkill, skillToVanilla);
	}


	public int vanillaToSkillXp(int vanillaXp, int skillLevel) {
		float rate = vanillaToSkillRate.calculate(skillLevel + 1);
		return Math.max(0, Math.round(vanillaXp * rate));
	}

	public int skillToVanillaXp(int skillXp, int skillLevel) {
		float rate = skillToVanillaRate.calculate(skillLevel + 1);
		return Math.max(0, Math.round(skillXp * rate));
	}
}
