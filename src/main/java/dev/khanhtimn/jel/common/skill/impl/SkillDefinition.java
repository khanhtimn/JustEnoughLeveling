package dev.khanhtimn.jel.common.skill.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Data-driven definition of a skill, loaded from a datapack JSON.
 * <p>
 * All fields are top-level for maximum readability:
 * <pre>{@code
 * {
 *   "name": "Constitution",
 *   "description": "Increases max health",
 *   "icon": "minecraft:golden_apple",
 *   "color": "#CC3333",
 *   "max_level": 10,
 *   "xp": { "type": "minecraft:linear", "base": 100, "per_level_above_first": 50 },
 *   "xp_conversion": { ... },
 *   "passives": [ ... ]
 * }
 * }</pre>
 */
public record SkillDefinition(
		String name,
		String description,
		ResourceLocation icon,
		int color,
		int maxLevel,
		XpFormula xp,
		XpConversion xpConversion,
		List<SkillPassive> passives
) {

	// ---- Hex color codec: "#RRGGBB" ↔ int ----

	public static final Codec<Integer> HEX_COLOR_CODEC = Codec.STRING.comapFlatMap(
			s -> {
				try {
					String hex = s.startsWith("#") ? s.substring(1) : s;
					return DataResult.success(Integer.parseUnsignedInt(hex, 16));
				} catch (NumberFormatException e) {
					return DataResult.error(() -> "Invalid hex color: " + s);
				}
			},
			i -> "#" + String.format("%06X", i & 0xFFFFFF)
	);

	// ---- Main codec ----

	public static final Codec<SkillDefinition> CODEC =
			RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("name")
							.forGetter(SkillDefinition::name),
					Codec.STRING.optionalFieldOf("description", "")
							.forGetter(SkillDefinition::description),
					ResourceLocation.CODEC.fieldOf("icon")
							.forGetter(SkillDefinition::icon),
					HEX_COLOR_CODEC.optionalFieldOf("color", 0xFFFFFF)
							.forGetter(SkillDefinition::color),
					Codec.INT.fieldOf("max_level")
							.forGetter(SkillDefinition::maxLevel),
					XpFormula.CODEC.optionalFieldOf("xp", XpFormula.vanilla())
							.forGetter(SkillDefinition::xp),
					XpConversion.CODEC.optionalFieldOf(
									"xp_conversion",
									XpConversion.IDENTITY
							)
							.forGetter(SkillDefinition::xpConversion),
					SkillPassive.CODEC.listOf()
							.optionalFieldOf("passives", List.of())
							.forGetter(SkillDefinition::passives)
			).apply(instance, SkillDefinition::new));

	public static final Codec<SkillDefinition> NETWORK_CODEC = CODEC;

	public SkillDefinition {
		if (maxLevel < 1) maxLevel = 1;
		passives = List.copyOf(passives);
	}

	/**
	 * Creates a new fluent builder for constructing a {@link SkillDefinition}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public int clampLevel(int level) {
		return Mth.clamp(level, 0, maxLevel);
	}

	public boolean isMaxLevel(int level) {
		return level >= maxLevel;
	}

	/**
	 * Skill XP needed to reach targetLevel from targetLevel-1.
	 */
	public int xpCostForLevel(int targetLevel) {
		if (targetLevel <= 0 || targetLevel > maxLevel) return 0;
		return xp.costForLevel(targetLevel);
	}

	/**
	 * Skill XP needed to go from currentLevel to currentLevel+1.
	 */
	public int xpCostForNextLevel(int currentLevel) {
		if (isMaxLevel(currentLevel)) return 0;
		return xp.costForLevel(currentLevel + 1);
	}

	/**
	 * Total skill XP to reach targetLevel from 0.
	 */
	public int totalXpCostToReachLevel(int targetLevel) {
		targetLevel = clampLevel(targetLevel);
		return xp.totalCostToLevel(targetLevel);
	}

	/**
	 * Convert vanilla XP to skill XP using this skill's conversion rate.
	 */
	public int vanillaToSkillXp(int vanillaXp, int skillLevel) {
		return xpConversion.vanillaToSkillXp(vanillaXp, skillLevel);
	}

	/**
	 * Convert skill XP to vanilla XP (for refunds) using this skill's conversion rate.
	 */
	public int skillToVanillaXp(int skillXp, int skillLevel) {
		return xpConversion.skillToVanillaXp(skillXp, skillLevel);
	}

	// ---- Builder ----

	/**
	 * Fluent builder for constructing {@link SkillDefinition} instances.
	 * <p>
	 * Usage:
	 * <pre>{@code
	 * SkillDefinition.builder()
	 *     .name("Combat")
	 *     .description("Increases attack damage")
	 *     .icon("minecraft:diamond_sword")
	 *     .color(0xFF4444)
	 *     .maxLevel(10)
	 *     .xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
	 *     .passive(SkillPassive.attributeBonus(1, "minecraft:generic.attack_damage",
	 *         LevelBasedValue.perLevel(0.5f, 0.5f), AttributeModifier.Operation.ADD_VALUE))
	 *     .build();
	 * }</pre>
	 */
	public static final class Builder {

		private String name = "Unnamed";
		private String description = "";
		private ResourceLocation iconItem = ResourceLocation.withDefaultNamespace("barrier");
		private int color = 0xFFFFFF;
		private int maxLevel = 10;
		private XpFormula xpFormula = XpFormula.vanilla();
		private XpConversion xpConversion = XpConversion.identity();
		private final List<SkillPassive> passives = new ArrayList<>();

		private Builder() {
		}

		public Builder name(String text) {
			this.name = text;
			return this;
		}

		public Builder description(String text) {
			this.description = text;
			return this;
		}

		public Builder icon(String itemId) {
			this.iconItem = ResourceLocation.parse(itemId);
			return this;
		}

		/**
		 * Set the skill's theme color. Used for name text, progress bar,
		 * card border, and other UI accents.
		 */
		public Builder color(int displayColor) {
			this.color = displayColor;
			return this;
		}

		public Builder maxLevel(int max) {
			this.maxLevel = max;
			return this;
		}

		public Builder xpFormula(XpFormula formula) {
			this.xpFormula = formula;
			return this;
		}

		public Builder xpConversion(XpConversion conversion) {
			this.xpConversion = conversion;
			return this;
		}

		public Builder passive(SkillPassive passive) {
			this.passives.add(passive);
			return this;
		}

		/**
		 * Add an attribute base override passive. Always active from level 0.
		 * Sets the attribute's base value directly.
		 *
		 * @param attribute the attribute (e.g. "minecraft:generic.max_health")
		 * @param value     level-based value computing the new base
		 */
		public Builder attributeBase(String attribute, LevelBasedValue value) {
			this.passives.add(SkillPassive.attributeBase(attribute, value));
			return this;
		}

		public SkillDefinition build() {
			return new SkillDefinition(
					name,
					description,
					iconItem,
					color,
					maxLevel,
					xpFormula,
					xpConversion,
					List.copyOf(passives)
			);
		}
	}
}
