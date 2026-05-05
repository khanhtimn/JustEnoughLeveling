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

import static dev.khanhtimn.jel.misc.Utils.interpolate;

public final class Mining {

	// --- Branch IDs ---
	public static final ResourceLocation EXCAVATOR_BRANCH = Constants.rl("mining/excavator");
	public static final ResourceLocation GEOMANCER_BRANCH = Constants.rl("mining/geomancer");

	// --- Trait IDs: Excavator ---
	public static final ResourceLocation QUARRY_STRIKE = Constants.rl("mining/quarry_strike");
	public static final ResourceLocation UNBREAKING_GRIP = Constants.rl("mining/unbreaking_grip");
	public static final ResourceLocation PROSPECTORS_EYE = Constants.rl("mining/prospectors_eye");
	public static final ResourceLocation MOTHER_LODE = Constants.rl("mining/mother_lode");
	public static final ResourceLocation TREMOR = Constants.rl("mining/tremor");
	public static final ResourceLocation EARTHSHAPER = Constants.rl("mining/earthshaper");

	// --- Trait IDs: Geomancer ---
	public static final ResourceLocation GEM_FINDER = Constants.rl("mining/gem_finder");
	public static final ResourceLocation ORE_SENSE = Constants.rl("mining/ore_sense");
	public static final ResourceLocation MIDAS_TOUCH = Constants.rl("mining/midas_touch");
	public static final ResourceLocation ENCHANTED_HARVEST = Constants.rl("mining/enchanted_harvest");
	public static final ResourceLocation SEISMIC_PULSE = Constants.rl("mining/seismic_pulse");
	public static final ResourceLocation HEART_OF_MOUNTAIN = Constants.rl("mining/heart_of_mountain");

	// --- Trait ID: Non-branched ---
	public static final ResourceLocation BONUS_MINING_DROP = Constants.rl("mining/bonus_drop");

	// --- Trait Keys: Excavator ---
	public static final TraitKey QUARRY_STRIKE_CHANCE = TraitKey.of(QUARRY_STRIKE, "chance");
	public static final TraitKey QUARRY_STRIKE_EXTRA = TraitKey.of(QUARRY_STRIKE, "extra_blocks");
	public static final TraitKey UNBREAKING_GRIP_REDUCTION = TraitKey.of(UNBREAKING_GRIP, "reduction");
	public static final TraitKey PROSPECTORS_EYE_RADIUS = TraitKey.of(PROSPECTORS_EYE, "radius");
	public static final TraitKey MOTHER_LODE_CHANCE = TraitKey.of(MOTHER_LODE, "chance");
	public static final TraitKey TREMOR_RADIUS = TraitKey.of(TREMOR, "radius");
	public static final TraitKey TREMOR_COOLDOWN = TraitKey.of(TREMOR, "cooldown");
	public static final TraitKey EARTHSHAPER_ACTIVE = TraitKey.of(EARTHSHAPER, "active");

	// --- Trait Keys: Geomancer ---
	public static final TraitKey GEM_FINDER_CHANCE = TraitKey.of(GEM_FINDER, "chance");
	public static final TraitKey ORE_SENSE_BONUS = TraitKey.of(ORE_SENSE, "bonus");
	public static final TraitKey MIDAS_TOUCH_CHANCE = TraitKey.of(MIDAS_TOUCH, "chance");
	public static final TraitKey ENCHANTED_HARVEST_CHANCE = TraitKey.of(ENCHANTED_HARVEST, "chance");
	public static final TraitKey SEISMIC_PULSE_RADIUS = TraitKey.of(SEISMIC_PULSE, "radius");
	public static final TraitKey SEISMIC_PULSE_COOLDOWN = TraitKey.of(SEISMIC_PULSE, "cooldown");
	public static final TraitKey HEART_OF_MOUNTAIN_ACTIVE = TraitKey.of(HEART_OF_MOUNTAIN, "active");

	// --- Trait Key: Non-branched ---
	public static final TraitKey BONUS_DROP_CHANCE = TraitKey.of(BONUS_MINING_DROP, "chance");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.mining.name"))
				.description(Component.translatable("jel.skill.mining.description"))
				.icon(Items.DIAMOND_PICKAXE)
				.color(0x55FFFF)
				.maxLevel(50)
				.xpFormula(XpFormula.quadratic(50, 40, 5))

				// --- Base Attributes (Lookup tables) ---
				.base(Attributes.BLOCK_BREAK_SPEED, breakSpeedLookup())
				.base(Attributes.MINING_EFFICIENCY, miningEfficiencyLookup())

				// --- Modifier Unlocks ---
				.modifier(Attributes.BLOCK_INTERACTION_RANGE,
						AttributeModifier.Operation.ADD_VALUE,
						LevelBasedValue.perLevel(0, 0.01f), 12)

				// --- Cross-Skill Requirements ---
				.requiresSkill(30,
						Component.translatable("jel.skill.mining.req.constitution_12"),
						ModSkills.CONSTITUTION.location(), 12)
				.requiresSkill(40,
						Component.translatable("jel.skill.mining.req.defense_15"),
						ModSkills.DEFENSE.location(), 15)

				// --- Non-branched Trait ---
				.trait(BONUS_MINING_DROP, BONUS_DROP_CHANCE,
						LevelBasedValue.perLevel(0, 0.02f), 10)

				// --- Excavator Traits ---
				.trait(QUARRY_STRIKE, QUARRY_STRIKE_CHANCE,
						LevelBasedValue.perLevel(0, 0.015f), 8)
				.trait(QUARRY_STRIKE, QUARRY_STRIKE_EXTRA,
						quarryStrikeExtraLookup(), 8)
				.trait(UNBREAKING_GRIP, UNBREAKING_GRIP_REDUCTION,
						LevelBasedValue.perLevel(0, 0.02f), 15)
				.trait(PROSPECTORS_EYE, PROSPECTORS_EYE_RADIUS,
						LevelBasedValue.constant(8), 22)
				.trait(MOTHER_LODE, MOTHER_LODE_CHANCE,
						LevelBasedValue.perLevel(0, 0.018f), 28)
				.trait(TREMOR, TREMOR_RADIUS,
						LevelBasedValue.constant(3), 36)
				.trait(TREMOR, TREMOR_COOLDOWN,
						LevelBasedValue.constant(200), 36)
				.trait(EARTHSHAPER, EARTHSHAPER_ACTIVE,
						LevelBasedValue.constant(1), 46)

				// --- Geomancer Traits ---
				.trait(GEM_FINDER, GEM_FINDER_CHANCE,
						LevelBasedValue.perLevel(0, 0.02f), 8)
				.trait(ORE_SENSE, ORE_SENSE_BONUS,
						LevelBasedValue.perLevel(0, 0.03f), 15)
				.trait(MIDAS_TOUCH, MIDAS_TOUCH_CHANCE,
						LevelBasedValue.perLevel(0, 0.015f), 22)
				.trait(ENCHANTED_HARVEST, ENCHANTED_HARVEST_CHANCE,
						LevelBasedValue.perLevel(0, 0.012f), 28)
				.trait(SEISMIC_PULSE, SEISMIC_PULSE_RADIUS,
						LevelBasedValue.constant(5), 36)
				.trait(SEISMIC_PULSE, SEISMIC_PULSE_COOLDOWN,
						LevelBasedValue.constant(400), 36)
				.trait(HEART_OF_MOUNTAIN, HEART_OF_MOUNTAIN_ACTIVE,
						LevelBasedValue.constant(1), 46)

				.build();
	}

	private static LevelBasedValue breakSpeedLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 49},
						new float[]{0.5f, 0.55f, 0.6f, 0.75f, 1.0f, 1.05f, 1.1f, 1.15f, 1.2f, 1.25f, 1.3f},
						50
				),
				LevelBasedValue.constant(1.3f)
		);
	}

	private static LevelBasedValue miningEfficiencyLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 49},
						new float[]{0.0f, 0.2f, 0.5f, 1.0f, 2.0f, 2.8f, 3.5f, 4.5f, 5.0f, 6.0f, 7.0f},
						50
				),
				LevelBasedValue.constant(7.0f)
		);
	}

	private static LevelBasedValue quarryStrikeExtraLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 8, 25, 39, 40, 49},
						new float[]{1, 1, 1, 1, 2, 2},
						50
				),
				LevelBasedValue.constant(2)
		);
	}

	private Mining() {
	}
}
