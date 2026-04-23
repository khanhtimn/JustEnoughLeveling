package dev.khanhtimn.jel.common.skill.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

import dev.khanhtimn.jel.common.skill.impl.perk.Perk;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition of a skill.
 * <pre>{@code
 * {
 *   "name": "Constitution",
 *   "name": {"translate": "skill.jel.constitution"},
 *   "description": "Increases max health",
 *   "icon": "minecraft:golden_apple",
 *   "color": "#CC3333",
 *   "max_level": 10,
 *   "xp": { "type": "minecraft:linear", "base": 100, "per_level_above_first": 50 },
 *   "attributes": [ ... ],
 *   "perks": [ ... ]
 * }
 * }</pre>
 */
public record SkillDefinition(
		Component name,
		Component description,
		ResourceLocation icon,
		int color,
		int maxLevel,
		XpFormula xp,
		XpConversion xpConversion,
		List<AttributeEffect> attributes,
		List<Perk> perks
) {

	/**
	 * Color codec using vanilla's {@link TextColor}. Accepts:
	 * <ul>
	 *   <li>Hex strings: {@code "#CC3333"}</li>
	 *   <li>Named colors: {@code "red"}, {@code "gold"}, {@code "dark_blue"}, etc.</li>
	 * </ul>
	 */
	public static final Codec<Integer> COLOR_CODEC = TextColor.CODEC.xmap(
			TextColor::getValue,
			TextColor::fromRgb
	);

	public static final Codec<SkillDefinition> CODEC
			= RecordCodecBuilder.create(instance -> instance.group(
			ComponentSerialization.CODEC.fieldOf("name")
					.forGetter(SkillDefinition::name),
			ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty())
					.forGetter(SkillDefinition::description),
			ResourceLocation.CODEC.fieldOf("icon")
					.forGetter(SkillDefinition::icon),
			COLOR_CODEC.optionalFieldOf("color", 0xFFFFFF)
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
			AttributeEffect.CODEC.listOf()
					.optionalFieldOf("attributes", List.of())
					.forGetter(SkillDefinition::attributes),
			Perk.CODEC.listOf()
					.optionalFieldOf("perks", List.of())
					.forGetter(SkillDefinition::perks)
	).apply(instance, SkillDefinition::new));

	public static final Codec<SkillDefinition> NETWORK_CODEC = CODEC;

	public SkillDefinition {
		if (maxLevel < 1) {
			maxLevel = 1;
		}
		attributes = List.copyOf(attributes);
		perks = List.copyOf(perks);
	}

	public static Builder builder() {
		return new Builder();
	}

	public int clampLevel(int level) {
		return Mth.clamp(level, 0, maxLevel);
	}

	public boolean isMaxLevel(int level) {
		return level >= maxLevel;
	}

	public int xpCostForLevel(int targetLevel) {
		if (targetLevel <= 0 || targetLevel > maxLevel) {
			return 0;
		}
		return xp.costForLevel(targetLevel);
	}

	public int xpCostForNextLevel(int currentLevel) {
		if (isMaxLevel(currentLevel)) {
			return 0;
		}
		return xp.costForLevel(currentLevel + 1);
	}

	public int totalXpCostToReachLevel(int targetLevel) {
		targetLevel = clampLevel(targetLevel);
		return xp.totalCostToLevel(targetLevel);
	}

	public int vanillaToSkillXp(int vanillaXp, int skillLevel) {
		return xpConversion.vanillaToSkillXp(vanillaXp, skillLevel);
	}

	public int skillToVanillaXp(int skillXp, int skillLevel) {
		return xpConversion.skillToVanillaXp(skillXp, skillLevel);
	}


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
	 *     .attribute(AttributeEffect.modifier("minecraft:generic.attack_damage",
	 *         AttributeModifier.Operation.ADD_VALUE, LevelBasedValue.perLevel(0.5f, 0.5f), 1))
	 *     .tag("jel.combat_mastery", 5)
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
		private final List<AttributeEffect> attributes = new ArrayList<>();
		private final List<Perk> perks = new ArrayList<>();

		private Builder() {
		}

		public Builder name(String text) {
			this.name = Component.literal(text);
			return this;
		}

		public Builder name(Component component) {
			this.name = component;
			return this;
		}

		public Builder description(String text) {
			this.description = Component.literal(text);
			return this;
		}

		public Builder description(Component component) {
			this.description = component;
			return this;
		}

		public Builder icon(String itemId) {
			this.iconItem = ResourceLocation.parse(itemId);
			return this;
		}

		public Builder icon(Item item) {
			this.iconItem = BuiltInRegistries.ITEM.getKey(item);
			return this;
		}

		public Builder icon(ResourceLocation itemId) {
			this.iconItem = itemId;
			return this;
		}

		/**
		 * Set the skill's theme color from an RGB int (e.g. {@code 0xFF4444}).
		 * Used for name text, progress bar, card border, and other UI accents.
		 */
		public Builder color(int rgb) {
			this.color = rgb;
			return this;
		}

		/**
		 * Set the skill's theme color from a hex string ({@code "#CC3333"})
		 * or named color ({@code "red"}, {@code "gold"}, etc.).
		 */
		public Builder color(String colorStr) {
			this.color = TextColor.parseColor(colorStr)
					.getOrThrow().getValue();
			return this;
		}

		public Builder color(TextColor textColor) {
			this.color = textColor.getValue();
			return this;
		}

		/**
		 * Set the skill's theme color from a vanilla {@link ChatFormatting}
		 * (e.g. {@code ChatFormatting.RED}, {@code ChatFormatting.GOLD}).
		 *
		 * @throws IllegalArgumentException if the formatting has no color
		 */
		public Builder color(ChatFormatting formatting) {
			TextColor tc = TextColor.fromLegacyFormat(formatting);
			if (tc == null) {
				throw new IllegalArgumentException(
						"ChatFormatting." + formatting.name() + " has no color");
			}
			this.color = tc.getValue();
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

		public Builder attribute(AttributeEffect effect) {
			this.attributes.add(effect);
			return this;
		}

		public Builder attribute(List<AttributeEffect> effects) {
			this.attributes.addAll(effects);
			return this;
		}

		public Builder perk(Perk perk) {
			this.perks.add(perk);
			return this;
		}

		public Builder perk(List<Perk> perkEntries) {
			this.perks.addAll(perkEntries);
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
					List.copyOf(attributes),
					List.copyOf(perks)
			);
		}
	}
}
