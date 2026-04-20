package dev.khanhtimn.jel.common.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SkillDefinition(
		SkillDisplay display,
		int maxLevel,
		XpFormula xp,
		XpConversion xpConversion,
		List<SkillPassive> passives
) {
	public static final Codec<SkillDefinition> CODEC =
			RecordCodecBuilder.create(instance -> instance.group(
					SkillDisplay.CODEC.fieldOf("display")
							.forGetter(SkillDefinition::display),
					Codec.INT.optionalFieldOf("max_level", 10)
							.forGetter(SkillDefinition::maxLevel),
					XpFormula.CODEC.optionalFieldOf(
									"xp",
									XpFormula.Vanilla.INSTANCE
							)
							.forGetter(SkillDefinition::xp),
					XpConversion.CODEC.optionalFieldOf(
									"xp_conversion",
									XpConversion.Identity.INSTANCE
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

	/** Creates a new fluent builder for constructing a {@link SkillDefinition}. */
	public static Builder builder() {
		return new Builder();
	}

	public int clampLevel(int level) {
		return net.minecraft.util.Mth.clamp(level, 0, maxLevel);
	}

	public boolean isMaxLevel(int level) {
		return level >= maxLevel;
	}

	/** Skill XP needed to reach targetLevel from targetLevel-1. */
	public int xpCostForLevel(int targetLevel) {
		if (targetLevel <= 0 || targetLevel > maxLevel) return 0;
		return xp.costForLevel(targetLevel);
	}

	/** Skill XP needed to go from currentLevel to currentLevel+1. */
	public int xpCostForNextLevel(int currentLevel) {
		if (isMaxLevel(currentLevel)) return 0;
		return xp.costForLevel(currentLevel + 1);
	}

	/** Total skill XP to reach targetLevel from 0. */
	public int totalXpCostToReachLevel(int targetLevel) {
		targetLevel = clampLevel(targetLevel);
		return xp.totalCostToLevel(targetLevel);
	}

	/** Convert vanilla XP to skill XP using this skill's conversion rate. */
	public int vanillaToSkillXp(int vanillaXp, int skillLevel) {
		return xpConversion.vanillaToSkillXp(vanillaXp, skillLevel);
	}

	/** Convert skill XP to vanilla XP (for refunds) using this skill's conversion rate. */
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
	 *     .name("Combat", 0xFF4444)
	 *     .description("Increases attack damage")
	 *     .icon("minecraft:diamond_sword")
	 *     .maxLevel(10)
	 *     .xpFormula(XpFormula.linear(100, 50))
	 *     .passive(SkillPassive.attributeBonus(1, "minecraft:generic.attack_damage", 0.5, ADD_VALUE))
	 *     .build();
	 * }</pre>
	 */
	public static final class Builder {

		private Component name = Component.literal("Unnamed");
		private Component description = Component.empty();
		private ResourceLocation iconItem = ResourceLocation.withDefaultNamespace("barrier");
		private int color = 0xFFFFFF;
		private int maxLevel = 10;
		private XpFormula xpFormula = XpFormula.vanilla();
		private XpConversion xpConversion = XpConversion.identity();
		private final List<SkillPassive> passives = new ArrayList<>();

		private Builder() {}

		public Builder name(String text, int nameColor) {
			this.name = Component.literal(text)
					.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(nameColor)));
			return this;
		}

		public Builder description(String text) {
			this.description = Component.literal(text);
			return this;
		}

		public Builder icon(String itemId) {
			this.iconItem = ResourceLocation.parse(itemId);
			return this;
		}

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

		public SkillDefinition build() {
			return new SkillDefinition(
					new SkillDisplay(name, description, iconItem, color),
					maxLevel,
					xpFormula,
					xpConversion,
					List.copyOf(passives)
			);
		}
	}
}
