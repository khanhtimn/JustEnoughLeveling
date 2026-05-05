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

public final class Defense {

	// --- Trait IDs (jel:defense/<trait>) ---
	// Fortress branch
	public static final ResourceLocation DAMAGE_REDUCTION = Constants.rl("defense/damage_reduction");
	public static final ResourceLocation ELEMENTAL_WARD = Constants.rl("defense/elemental_ward");
	public static final ResourceLocation FORTIFY = Constants.rl("defense/fortify");
	public static final ResourceLocation DEATH_GRACE = Constants.rl("defense/death_grace");
	public static final ResourceLocation THORNS_NOVA = Constants.rl("defense/thorns_nova");
	public static final ResourceLocation IRON_CURTAIN = Constants.rl("defense/iron_curtain");

	// Sentinel branch
	public static final ResourceLocation PARRY = Constants.rl("defense/parry");
	public static final ResourceLocation THORNS_REFLECT = Constants.rl("defense/thorns_reflect");
	public static final ResourceLocation SHIELD_BASH = Constants.rl("defense/shield_bash");
	public static final ResourceLocation SECOND_WIND = Constants.rl("defense/second_wind");
	public static final ResourceLocation RALLYING_CRY = Constants.rl("defense/rallying_cry");
	public static final ResourceLocation VENGEFUL_SPIRIT = Constants.rl("defense/vengeful_spirit");

	// Branch IDs
	public static final ResourceLocation FORTRESS_BRANCH = Constants.rl("defense/fortress");
	public static final ResourceLocation SENTINEL_BRANCH = Constants.rl("defense/sentinel");

	// --- Trait Keys ---
	// Fortress
	public static final TraitKey DAMAGE_REDUCTION_PERCENT = TraitKey.of(DAMAGE_REDUCTION, "percent");
	public static final TraitKey ELEMENTAL_WARD_PERCENT = TraitKey.of(ELEMENTAL_WARD, "percent");
	public static final TraitKey FORTIFY_ARMOR = TraitKey.of(FORTIFY, "armor_bonus");
	public static final TraitKey DEATH_GRACE_CHANCE = TraitKey.of(DEATH_GRACE, "chance");
	public static final TraitKey THORNS_NOVA_DAMAGE = TraitKey.of(THORNS_NOVA, "damage");
	public static final TraitKey IRON_CURTAIN_REDUCTION = TraitKey.of(IRON_CURTAIN, "reduction");

	// Sentinel
	public static final TraitKey PARRY_CHANCE = TraitKey.of(PARRY, "chance");
	public static final TraitKey THORNS_REFLECT_PERCENT = TraitKey.of(THORNS_REFLECT, "percent");
	public static final TraitKey SHIELD_BASH_DAMAGE = TraitKey.of(SHIELD_BASH, "damage");
	public static final TraitKey SECOND_WIND_HEAL = TraitKey.of(SECOND_WIND, "heal_percent");
	public static final TraitKey RALLYING_CRY_RADIUS = TraitKey.of(RALLYING_CRY, "radius");
	public static final TraitKey VENGEFUL_SPIRIT_DAMAGE = TraitKey.of(VENGEFUL_SPIRIT, "damage_bonus");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.defense.name"))
				.description(Component.translatable("jel.skill.defense.description"))
				.icon(Items.SHIELD)
				.color(0x4488CC)
				.maxLevel(50)
				.xpFormula(XpFormula.quadratic(50, 40, 5))

				// --- Base Attributes ---
				// L0=-2 armor (glass cannon start), L15≈0 (vanilla), L50≈+4.67
				.base(Attributes.ARMOR, LevelBasedValue.perLevel(-2.0f, 0.1333f))
				// L0=0 knockback resist, L15≈0.15, L50=0.5
				.base(Attributes.KNOCKBACK_RESISTANCE, LevelBasedValue.perLevel(0.0f, 0.01f))

				// --- Modifier Unlocks ---
				.modifier(Attributes.ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_VALUE,
						LevelBasedValue.perLevel(0, 0.08f), 8)

				// --- Cross-Skill Requirements ---
				// TODO: Constitution skill key — update when Constitution is redesigned
				.requiresSkill(30,
						Component.translatable("jel.skill.defense.req.constitution_10"),
						ModSkills.CONSTITUTION.location(), 10)
				.requiresSkill(40,
						Component.translatable("jel.skill.defense.req.constitution_20"),
						ModSkills.CONSTITUTION.location(), 20)

				// --- Fortress Traits ---
				.trait(DAMAGE_REDUCTION, DAMAGE_REDUCTION_PERCENT,
						LevelBasedValue.perLevel(0, 0.004f), 10)
				.trait(ELEMENTAL_WARD, ELEMENTAL_WARD_PERCENT,
						LevelBasedValue.perLevel(0, 0.005f), 18)
				.trait(FORTIFY, FORTIFY_ARMOR,
						LevelBasedValue.perLevel(0, 0.06f), 25)
				.trait(DEATH_GRACE, DEATH_GRACE_CHANCE,
						LevelBasedValue.perLevel(0, 0.0067f), 32)
				.trait(THORNS_NOVA, THORNS_NOVA_DAMAGE,
						LevelBasedValue.perLevel(0, 0.04f), 40)
				.trait(IRON_CURTAIN, IRON_CURTAIN_REDUCTION,
						LevelBasedValue.perLevel(0.75f, 0), 48)

				// --- Sentinel Traits ---
				.trait(PARRY, PARRY_CHANCE,
						LevelBasedValue.perLevel(0, 0.006f), 10)
				.trait(THORNS_REFLECT, THORNS_REFLECT_PERCENT,
						LevelBasedValue.perLevel(0, 0.0065f), 18)
				.trait(SHIELD_BASH, SHIELD_BASH_DAMAGE,
						LevelBasedValue.perLevel(0, 0.04f), 25)
				.trait(SECOND_WIND, SECOND_WIND_HEAL,
						LevelBasedValue.perLevel(0, 0.005f), 32)
				.trait(RALLYING_CRY, RALLYING_CRY_RADIUS,
						LevelBasedValue.perLevel(5.0f, 0.1f), 40)
				.trait(VENGEFUL_SPIRIT, VENGEFUL_SPIRIT_DAMAGE,
						LevelBasedValue.perLevel(0, 0.01f), 48)
				.build();
	}

	private Defense() {
	}
}
