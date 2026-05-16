package dev.khanhtimn.jel.api.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import dev.khanhtimn.jel.api.perk.EffectPerk;
import dev.khanhtimn.jel.api.perk.Perk;
import dev.khanhtimn.jel.api.perk.TraitParam;
import dev.khanhtimn.jel.api.trait.TraitKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
		SkillIcon icon,
		int color,
		int maxLevel,
		XpFormula xp,
		XpConversion xpConversion,
		List<AttributeEffect> attributes,
		List<Perk> perks,
		Map<Integer, Requirement> requirements
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
			SkillIcon.CODEC.fieldOf("icon")
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
					.forGetter(SkillDefinition::perks),
			Codec.unboundedMap(
					Codec.STRING.xmap(Integer::parseInt, String::valueOf),
					Requirement.CODEC
			).optionalFieldOf("requirements", Map.of())
					.forGetter(SkillDefinition::requirements)
	).apply(instance, SkillDefinition::new));

	public static final Codec<SkillDefinition> NETWORK_CODEC = CODEC;

	public SkillDefinition {
		if (maxLevel < 1) {
			maxLevel = 1;
		}
		attributes = List.copyOf(attributes);
		perks = List.copyOf(perks);
		requirements = Map.copyOf(requirements);
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

	public Optional<Requirement> requirementForLevel(int targetLevel) {
		return Optional.ofNullable(requirements.get(targetLevel));
	}

	public int vanillaToSkillXp(int vanillaXp, int skillLevel) {
		return xpConversion.vanillaToSkillXp(vanillaXp, skillLevel);
	}

	public int skillToVanillaXp(int skillXp, int skillLevel) {
		return xpConversion.skillToVanillaXp(skillXp, skillLevel);
	}


	/**
	 * Fluent builder for constructing {@link SkillDefinition} instances.
	 * <pre>{@code
	 * SkillDefinition.builder()
	 *     .name("Combat")
	 *     .icon(Items.DIAMOND_SWORD)
	 *     .color(0xFF4444)
	 *     .maxLevel(10)
	 *     .base(Attributes.ATTACK_DAMAGE, LevelBasedValue.perLevel(0.4f, 0.2f))
	 *     .modifier(Attributes.ATTACK_KNOCKBACK, ADD_VALUE, perLevel(0.05f), 18)
	 *     .trait(CRIT_DAMAGE, CRIT_DAMAGE_BONUS, perLevel(0, 0.005f), 21)
	 *     .effect(MobEffects.REGENERATION, 0, 30)
	 *     .tag("jel.combat_mastery", 5)
	 *     .build();
	 * }</pre>
	 */
	public static final class Builder {

		private Component name = Component.literal("Unnamed");
		private Component description = Component.empty();
		private SkillIcon skillIcon = new SkillIcon.ItemIcon(ResourceLocation.withDefaultNamespace("barrier"));
		private int color = 0xFFFFFF;
		private int maxLevel = 10;
		private XpFormula xpFormula = XpFormula.vanilla();
		private XpConversion xpConversion = XpConversion.identity();
		private final List<AttributeEffect> attributes = new ArrayList<>();
		private final List<Perk> perks = new ArrayList<>();
		private final Map<Integer, Requirement> requirements = new LinkedHashMap<>();

		private Builder() {
		}

		// --- Identity ---

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
			this.skillIcon = new SkillIcon.ItemIcon(ResourceLocation.parse(itemId));
			return this;
		}

		public Builder icon(Item item) {
			this.skillIcon = new SkillIcon.ItemIcon(BuiltInRegistries.ITEM.getKey(item));
			return this;
		}

		public Builder icon(ResourceLocation itemId) {
			this.skillIcon = new SkillIcon.ItemIcon(itemId);
			return this;
		}

		public Builder iconTexture(ResourceLocation texturePath) {
			this.skillIcon = new SkillIcon.TextureIcon(texturePath);
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

		// --- Attributes ---

		public Builder attribute(AttributeEffect effect) {
			this.attributes.add(effect);
			return this;
		}

		public Builder base(Holder<Attribute> attribute, LevelBasedValue value) {
			return attribute(AttributeEffect.base(attribute, value));
		}

		public Builder modifier(Holder<Attribute> attribute,
		                        AttributeModifier.Operation operation,
		                        LevelBasedValue value, int unlockLevel) {
			return attribute(AttributeEffect.modifier(attribute, operation, value, unlockLevel));
		}

		// --- Perks ---

		public Builder perk(Perk perk) {
			this.perks.add(perk);
			return this;
		}

		public Builder tag(String tag, int unlockLevel) {
			return perk(Perk.tag(tag, unlockLevel));
		}

		public Builder trait(String trait, int unlockLevel) {
			return perk(Perk.trait(trait, unlockLevel));
		}

		public Builder trait(ResourceLocation trait, int unlockLevel) {
			return perk(Perk.trait(trait, unlockLevel));
		}

		public Builder trait(ResourceLocation trait, TraitParam... params) {
			return perk(Perk.trait(trait, params));
		}

		public Builder trait(ResourceLocation trait, TraitKey param,
		                     LevelBasedValue formula, int unlockLevel) {
			return perk(Perk.trait(trait, TraitParam.of(param, formula, unlockLevel)));
		}

		public Builder effect(Holder<MobEffect> effect, int amplifier, int unlockLevel) {
			return perk(Perk.effect(effect, amplifier, unlockLevel));
		}

		public Builder effect(Holder<MobEffect> effect, LevelBasedValue amplifier, int unlockLevel) {
			return perk(Perk.effect(effect, amplifier, unlockLevel));
		}

		public Builder effect(Holder<MobEffect> effect, LevelBasedValue amplifier,
		                      boolean ambient, boolean showParticles, boolean showIcon,
		                      int unlockLevel) {
			return perk(EffectPerk.of(effect, amplifier, ambient, showParticles, showIcon, unlockLevel));
		}

		public Builder function(String grant, int unlockLevel) {
			return perk(Perk.function(grant, unlockLevel));
		}

		public Builder function(String grant, String revoke, int unlockLevel) {
			return perk(Perk.function(grant, revoke, unlockLevel));
		}

		public Builder command(String grantCommand, int unlockLevel) {
			return perk(Perk.command(grantCommand, unlockLevel));
		}

		public Builder command(String grantCommand, String revokeCommand, int unlockLevel) {
			return perk(Perk.command(grantCommand, revokeCommand, unlockLevel));
		}

		public Builder lootTable(String lootTable, int unlockLevel) {
			return perk(Perk.lootTable(lootTable, unlockLevel));
		}

		public Builder lootTable(ResourceLocation lootTable, int unlockLevel) {
			return perk(Perk.lootTable(lootTable, unlockLevel));
		}

		// --- Requirements ---

		public Builder requirement(int level, Requirement req) {
			this.requirements.put(level, req);
			return this;
		}

		public Builder requirement(int level, LootItemCondition condition) {
			return requirement(level, Requirement.of(condition));
		}

		public Builder requirement(int level, Component description, LootItemCondition condition) {
			return requirement(level, Requirement.of(description, condition));
		}

		public Builder requiresSkill(int level, ResourceLocation otherSkill, int minSkillLevel) {
			return requirement(level, Requirement.skillLevel(otherSkill, minSkillLevel));
		}

		public Builder requiresSkill(int level, Component description,
		                             ResourceLocation otherSkill, int minSkillLevel) {
			return requirement(level, Requirement.skillLevel(description, otherSkill, minSkillLevel));
		}

		public SkillDefinition build() {
			return new SkillDefinition(
					name,
					description,
					skillIcon,
					color,
					maxLevel,
					xpFormula,
					xpConversion,
					List.copyOf(attributes),
					List.copyOf(perks),
					Map.copyOf(requirements)
			);
		}
	}
}
