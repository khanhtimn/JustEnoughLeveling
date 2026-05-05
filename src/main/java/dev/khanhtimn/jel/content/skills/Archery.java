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

public final class Archery {

	// --- Trait IDs (jel:archery/<trait>) ---
	// Sniper branch
	public static final ResourceLocation BOW_DAMAGE = Constants.rl("archery/bow_damage");
	public static final ResourceLocation HEADSHOT = Constants.rl("archery/headshot");
	public static final ResourceLocation PIERCING_SHOT = Constants.rl("archery/piercing_shot");
	public static final ResourceLocation BOW_CRIT = Constants.rl("archery/bow_crit");
	public static final ResourceLocation FROZEN_ARROW = Constants.rl("archery/frozen_arrow");
	public static final ResourceLocation ARROW_RAIN = Constants.rl("archery/arrow_rain");

	// Volley branch
	public static final ResourceLocation CROSSBOW_DAMAGE = Constants.rl("archery/crossbow_damage");
	public static final ResourceLocation RAPID_RELOAD = Constants.rl("archery/rapid_reload");
	public static final ResourceLocation CROSSBOW_CRIT = Constants.rl("archery/crossbow_crit");
	public static final ResourceLocation CHAIN_BOLT = Constants.rl("archery/chain_bolt");
	public static final ResourceLocation EXPLOSIVE_BOLT = Constants.rl("archery/explosive_bolt");
	public static final ResourceLocation AUTO_LOADER = Constants.rl("archery/auto_loader");

	// Branch IDs
	public static final ResourceLocation SNIPER_BRANCH = Constants.rl("archery/sniper");
	public static final ResourceLocation VOLLEY_BRANCH = Constants.rl("archery/volley");

	// --- Trait Keys ---
	// Sniper
	public static final TraitKey BOW_DAMAGE_BONUS = TraitKey.of(BOW_DAMAGE, "bonus");
	public static final TraitKey HEADSHOT_BONUS = TraitKey.of(HEADSHOT, "bonus");
	public static final TraitKey PIERCING_SHOT_CHANCE = TraitKey.of(PIERCING_SHOT, "chance");
	public static final TraitKey BOW_CRIT_CHANCE = TraitKey.of(BOW_CRIT, "chance");
	public static final TraitKey FROZEN_ARROW_CHANCE = TraitKey.of(FROZEN_ARROW, "chance");
	public static final TraitKey ARROW_RAIN_RADIUS = TraitKey.of(ARROW_RAIN, "radius");

	// Volley
	public static final TraitKey CROSSBOW_DAMAGE_BONUS = TraitKey.of(CROSSBOW_DAMAGE, "bonus");
	public static final TraitKey RAPID_RELOAD_REDUCTION = TraitKey.of(RAPID_RELOAD, "reduction");
	public static final TraitKey CROSSBOW_CRIT_CHANCE = TraitKey.of(CROSSBOW_CRIT, "chance");
	public static final TraitKey CHAIN_BOLT_TARGETS = TraitKey.of(CHAIN_BOLT, "targets");
	public static final TraitKey EXPLOSIVE_BOLT_CHANCE = TraitKey.of(EXPLOSIVE_BOLT, "chance");
	public static final TraitKey AUTO_LOADER_CHANCE = TraitKey.of(AUTO_LOADER, "chance");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.archery.name"))
				.description(Component.translatable("jel.skill.archery.description"))
				.icon(Items.BOW)
				.color(0x55AA55)
				.maxLevel(50)
				.xpFormula(XpFormula.quadratic(50, 40, 5))

				// --- Modifier Unlocks ---
				.modifier(Attributes.ENTITY_INTERACTION_RANGE, AttributeModifier.Operation.ADD_VALUE,
						LevelBasedValue.perLevel(0, 0.02f), 5)

				// --- Cross-Skill Requirements ---
				.requiresSkill(25,
						Component.translatable("jel.skill.archery.req.melee_10"),
						ModSkills.MELEE.location(), 10)
				// TODO: Agility skill key — update when Agility is redesigned
				.requiresSkill(35,
						Component.translatable("jel.skill.archery.req.agility_10"),
						ModSkills.AGILITY.location(), 10)
				.requiresSkill(45,
						Component.translatable("jel.skill.archery.req.melee_20"),
						ModSkills.MELEE.location(), 20)

				// --- Sniper Traits ---
				.trait(BOW_DAMAGE, BOW_DAMAGE_BONUS,
						LevelBasedValue.perLevel(0, 0.05f), 5)
				.trait(HEADSHOT, HEADSHOT_BONUS,
						LevelBasedValue.perLevel(0.35f, 0), 15)
				.trait(PIERCING_SHOT, PIERCING_SHOT_CHANCE,
						LevelBasedValue.perLevel(0, 0.005f), 22)
				.trait(BOW_CRIT, BOW_CRIT_CHANCE,
						LevelBasedValue.perLevel(0, 0.004f), 30)
				.trait(FROZEN_ARROW, FROZEN_ARROW_CHANCE,
						LevelBasedValue.perLevel(0, 0.002f), 38)
				.trait(ARROW_RAIN, ARROW_RAIN_RADIUS,
						LevelBasedValue.perLevel(3.0f, 0), 45)

				// --- Volley Traits ---
				.trait(CROSSBOW_DAMAGE, CROSSBOW_DAMAGE_BONUS,
						LevelBasedValue.perLevel(0, 0.05f), 8)
				.trait(RAPID_RELOAD, RAPID_RELOAD_REDUCTION,
						LevelBasedValue.perLevel(0, 0.007f), 15)
				.trait(CROSSBOW_CRIT, CROSSBOW_CRIT_CHANCE,
						LevelBasedValue.perLevel(0, 0.005f), 22)
				.trait(CHAIN_BOLT, CHAIN_BOLT_TARGETS,
						LevelBasedValue.perLevel(2.0f, 0), 30)
				.trait(EXPLOSIVE_BOLT, EXPLOSIVE_BOLT_CHANCE,
						LevelBasedValue.perLevel(0, 0.0013f), 38)
				.trait(AUTO_LOADER, AUTO_LOADER_CHANCE,
						LevelBasedValue.perLevel(0, 0.0015f), 45)
				.build();
	}

	private Archery() {
	}
}
