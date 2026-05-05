package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpFormula;
import dev.khanhtimn.jel.api.trait.TraitKey;
import dev.khanhtimn.jel.content.ModSkills;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public final class Melee {

	// --- Trait IDs (jel:melee/<trait>) ---
	// Berserker branch
	public static final ResourceLocation BLEEDING_EDGE = Constants.rl("melee/bleeding_edge");
	public static final ResourceLocation ADRENALINE = Constants.rl("melee/adrenaline");
	public static final ResourceLocation CRITICAL_FURY = Constants.rl("melee/critical_fury");
	public static final ResourceLocation BLOODLUST = Constants.rl("melee/bloodlust");
	public static final ResourceLocation GROUND_SLAM = Constants.rl("melee/ground_slam");
	public static final ResourceLocation WRATH = Constants.rl("melee/wrath");

	// Duelist branch
	public static final ResourceLocation LIFE_STEAL = Constants.rl("melee/life_steal");
	public static final ResourceLocation RIPOSTE = Constants.rl("melee/riposte");
	public static final ResourceLocation DISARM = Constants.rl("melee/disarm");
	public static final ResourceLocation COMBO_FINISHER = Constants.rl("melee/combo_finisher");
	public static final ResourceLocation THOUSAND_CUTS = Constants.rl("melee/thousand_cuts");
	public static final ResourceLocation MARK_OF_DEATH = Constants.rl("melee/mark_of_death");

	// Branch IDs
	public static final ResourceLocation BERSERKER_BRANCH = Constants.rl("melee/berserker");
	public static final ResourceLocation DUELIST_BRANCH = Constants.rl("melee/duelist");

	// --- Trait Keys ---
	// Berserker
	public static final TraitKey BLEEDING_EDGE_CHANCE = TraitKey.of(BLEEDING_EDGE, "chance");
	public static final TraitKey ADRENALINE_DAMAGE = TraitKey.of(ADRENALINE, "damage_bonus");
	public static final TraitKey CRITICAL_FURY_BONUS = TraitKey.of(CRITICAL_FURY, "bonus");
	public static final TraitKey BLOODLUST_SPEED = TraitKey.of(BLOODLUST, "speed_bonus");
	public static final TraitKey GROUND_SLAM_CHANCE = TraitKey.of(GROUND_SLAM, "chance");
	public static final TraitKey WRATH_THRESHOLD = TraitKey.of(WRATH, "threshold");

	// Duelist
	public static final TraitKey LIFE_STEAL_PERCENT = TraitKey.of(LIFE_STEAL, "percentage");
	public static final TraitKey RIPOSTE_CHANCE = TraitKey.of(RIPOSTE, "chance");
	public static final TraitKey DISARM_CHANCE = TraitKey.of(DISARM, "chance");
	public static final TraitKey COMBO_FINISHER_DAMAGE = TraitKey.of(COMBO_FINISHER, "damage_bonus");
	public static final TraitKey THOUSAND_CUTS_DOT = TraitKey.of(THOUSAND_CUTS, "dot_damage");
	public static final TraitKey MARK_OF_DEATH_BONUS = TraitKey.of(MARK_OF_DEATH, "bonus");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.melee.name"))
				.description(Component.translatable("jel.skill.melee.description"))
				.icon(Items.DIAMOND_SWORD)
				.color(0xCC3333)
				.maxLevel(50)
				.xpFormula(XpFormula.quadratic(50, 40, 5))

				// --- Base Attributes ---
				// L0=0.6x damage, L15≈1.0x, L50≈1.93x
				.base(Attributes.ATTACK_DAMAGE, LevelBasedValue.perLevel(0.6f, 0.0267f))
				// L0=2.5 speed (sluggish), L15≈3.6 (vanilla), L50≈5.65
				.base(Attributes.ATTACK_SPEED, LevelBasedValue.perLevel(2.5f, 0.063f))
				// L0=2.5 range (short), L15≈2.9, L50≈3.83
				.base(Attributes.ENTITY_INTERACTION_RANGE, LevelBasedValue.perLevel(2.5f, 0.0267f))

				// --- Modifier Unlocks ---
				.modifier(Attributes.SWEEPING_DAMAGE_RATIO, AttributeModifier.Operation.ADD_VALUE,
						LevelBasedValue.perLevel(0, 0.015f), 12)
				.modifier(Attributes.ATTACK_KNOCKBACK, AttributeModifier.Operation.ADD_VALUE,
						LevelBasedValue.perLevel(0, 0.03f), 20)

				// --- Cross-Skill Requirements ---
				.requiresSkill(30,
						Component.translatable("jel.skill.melee.req.defense_15"),
						ModSkills.DEFENSE.location(), 15)
				.requiresSkill(40,
						Component.translatable("jel.skill.melee.req.defense_20"),
						ModSkills.DEFENSE.location(), 20)

				// --- Berserker Traits ---
				.trait(BLEEDING_EDGE, BLEEDING_EDGE_CHANCE,
						LevelBasedValue.perLevel(0, 0.0075f), 10)
				.trait(ADRENALINE, ADRENALINE_DAMAGE,
						LevelBasedValue.perLevel(0, 0.015f), 18)
				.trait(CRITICAL_FURY, CRITICAL_FURY_BONUS,
						LevelBasedValue.perLevel(0, 0.008f), 25)
				.trait(BLOODLUST, BLOODLUST_SPEED,
						LevelBasedValue.perLevel(0, 0.01f), 32)
				.trait(GROUND_SLAM, GROUND_SLAM_CHANCE,
						LevelBasedValue.perLevel(0, 0.006f), 40)
				.trait(WRATH, WRATH_THRESHOLD,
						LevelBasedValue.perLevel(0.15f, 0), 48)

				// --- Duelist Traits ---
				.trait(LIFE_STEAL, LIFE_STEAL_PERCENT,
						LevelBasedValue.perLevel(0, 0.004f), 10)
				.trait(RIPOSTE, RIPOSTE_CHANCE,
						LevelBasedValue.perLevel(0, 0.005f), 18)
				.trait(DISARM, DISARM_CHANCE,
						LevelBasedValue.perLevel(0, 0.005f), 25)
				.trait(COMBO_FINISHER, COMBO_FINISHER_DAMAGE,
						LevelBasedValue.perLevel(0, 0.01f), 32)
				.trait(THOUSAND_CUTS, THOUSAND_CUTS_DOT,
						LevelBasedValue.perLevel(0, 0.02f), 40)
				.trait(MARK_OF_DEATH, MARK_OF_DEATH_BONUS,
						LevelBasedValue.perLevel(0, 0.02f), 48)
				.build();
	}

	private Melee() {
	}
}
