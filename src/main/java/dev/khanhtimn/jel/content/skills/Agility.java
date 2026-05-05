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

public final class Agility {

	// --- Trait IDs ---
	// Parkour branch
	public static final ResourceLocation WALL_KICK = Constants.rl("agility/wall_kick");
	public static final ResourceLocation FEATHER_LANDING = Constants.rl("agility/feather_landing");
	public static final ResourceLocation MOMENTUM = Constants.rl("agility/momentum");
	public static final ResourceLocation ACROBAT = Constants.rl("agility/acrobat");
	public static final ResourceLocation SPRINT_SURGE = Constants.rl("agility/sprint_surge");
	public static final ResourceLocation WINDRUNNER = Constants.rl("agility/windrunner");

	// Explorer branch
	public static final ResourceLocation AQUATIC_AFFINITY = Constants.rl("agility/aquatic_affinity");
	public static final ResourceLocation SURE_FOOTED = Constants.rl("agility/sure_footed");
	public static final ResourceLocation NIGHT_EYES = Constants.rl("agility/night_eyes");
	public static final ResourceLocation THICK_SKIN = Constants.rl("agility/thick_skin");
	public static final ResourceLocation GLIDER = Constants.rl("agility/glider");
	public static final ResourceLocation NOMAD = Constants.rl("agility/nomad");

	// Legacy trait IDs kept for backward compat with existing handlers
	public static final ResourceLocation MOB_COLLIDE = Constants.rl("mob_collide");

	// Branch IDs
	public static final ResourceLocation PARKOUR_BRANCH = Constants.rl("agility/parkour");
	public static final ResourceLocation EXPLORER_BRANCH = Constants.rl("agility/explorer");

	// --- Trait Keys ---
	// Parkour
	public static final TraitKey WALL_KICK_BONUS = TraitKey.of(WALL_KICK, "bonus");
	public static final TraitKey FEATHER_LANDING_REDUCTION = TraitKey.of(FEATHER_LANDING, "reduction");
	public static final TraitKey MOMENTUM_BONUS = TraitKey.of(MOMENTUM, "bonus");
	public static final TraitKey MOMENTUM_THRESHOLD = TraitKey.of(MOMENTUM, "threshold");
	public static final TraitKey ACROBAT_CHANCE = TraitKey.of(ACROBAT, "chance");
	public static final TraitKey SPRINT_SURGE_DURATION = TraitKey.of(SPRINT_SURGE, "duration");
	public static final TraitKey SPRINT_SURGE_COOLDOWN = TraitKey.of(SPRINT_SURGE, "cooldown");

	// Explorer
	public static final TraitKey AQUATIC_AFFINITY_BONUS = TraitKey.of(AQUATIC_AFFINITY, "bonus");
	public static final TraitKey SURE_FOOTED_REDUCTION = TraitKey.of(SURE_FOOTED, "reduction");
	public static final TraitKey NIGHT_EYES_THRESHOLD = TraitKey.of(NIGHT_EYES, "threshold");
	public static final TraitKey THICK_SKIN_REDUCTION = TraitKey.of(THICK_SKIN, "reduction");
	public static final TraitKey GLIDER_BONUS = TraitKey.of(GLIDER, "bonus");

	// Legacy (non-branched, always active)
	public static final TraitKey MOB_COLLIDE_AVOID = TraitKey.of(MOB_COLLIDE, "avoid_chance");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.agility.name"))
				.description(Component.translatable("jel.skill.agility.description"))
				.icon(Items.LEATHER_BOOTS)
				.color(0x55CC55)
				.maxLevel(50)
				.xpFormula(XpFormula.quadratic(50, 40, 5))

				// --- Base Attributes (Lookup tables) ---
				// MOVEMENT_SPEED: L0=0.07, L15=0.10 (vanilla), L50=0.12
				.base(Attributes.MOVEMENT_SPEED, movementSpeedLookup())
				// JUMP_STRENGTH: L0=0.42 (vanilla), L15=0.42 (vanilla), L50=0.55
				.base(Attributes.JUMP_STRENGTH, jumpStrengthLookup())
				// SAFE_FALL_DISTANCE: L0=2.0, L15=3.0 (vanilla), L50=6.0
				.base(Attributes.SAFE_FALL_DISTANCE, safeFallLookup())
				// SNEAKING_SPEED: L0=0.2, L15=0.30 (vanilla), L50=0.45
				.base(Attributes.SNEAKING_SPEED, sneakSpeedLookup())
				// OXYGEN_BONUS: L0=0.0 (short breath), L15=2.0, L50=10.0
				.base(Attributes.OXYGEN_BONUS, oxygenLookup())

				// --- Modifier Unlocks ---
				.modifier(Attributes.STEP_HEIGHT, AttributeModifier.Operation.ADD_VALUE,
						LevelBasedValue.perLevel(0, 0.015f), 12)
				.modifier(Attributes.FALL_DAMAGE_MULTIPLIER, AttributeModifier.Operation.ADD_VALUE,
						LevelBasedValue.perLevel(0, -0.012f), 15)
				.modifier(Attributes.MOVEMENT_EFFICIENCY, AttributeModifier.Operation.ADD_VALUE,
						LevelBasedValue.perLevel(0, 0.012f), 18)
				.modifier(Attributes.WATER_MOVEMENT_EFFICIENCY, AttributeModifier.Operation.ADD_VALUE,
						LevelBasedValue.perLevel(0, 0.015f), 12)
				.modifier(Attributes.SUBMERGED_MINING_SPEED, AttributeModifier.Operation.ADD_VALUE,
						LevelBasedValue.perLevel(0, 0.01f), 15)

				// --- Cross-Skill Requirements ---
				.requiresSkill(30,
						Component.translatable("jel.skill.agility.req.constitution_12"),
						ModSkills.CONSTITUTION.location(), 12)
				.requiresSkill(40,
						Component.translatable("jel.skill.agility.req.melee_or_archery_15"),
						ModSkills.MELEE.location(), 15)

				// --- Non-branched Trait (always active) ---
				.trait(MOB_COLLIDE, MOB_COLLIDE_AVOID,
						LevelBasedValue.perLevel(0, 0.015f), 10)

				// --- Parkour Traits ---
				.trait(WALL_KICK, WALL_KICK_BONUS,
						LevelBasedValue.perLevel(0, 0.02f), 8)
				.trait(FEATHER_LANDING, FEATHER_LANDING_REDUCTION,
						LevelBasedValue.perLevel(0, 0.02f), 15)
				.trait(MOMENTUM, MOMENTUM_BONUS,
						LevelBasedValue.perLevel(0, 0.008f), 22)
				.trait(MOMENTUM, MOMENTUM_THRESHOLD,
						LevelBasedValue.constant(60), 22)
				.trait(ACROBAT, ACROBAT_CHANCE,
						LevelBasedValue.perLevel(0, 0.006f), 28)
				.trait(SPRINT_SURGE, SPRINT_SURGE_DURATION,
						LevelBasedValue.constant(80), 36)
				.trait(SPRINT_SURGE, SPRINT_SURGE_COOLDOWN,
						LevelBasedValue.constant(300), 36)
				.trait(WINDRUNNER, TraitKey.of(WINDRUNNER, "active"),
						LevelBasedValue.constant(1), 46)

				// --- Explorer Traits ---
				.trait(AQUATIC_AFFINITY, AQUATIC_AFFINITY_BONUS,
						LevelBasedValue.perLevel(0, 0.02f), 8)
				.trait(SURE_FOOTED, SURE_FOOTED_REDUCTION,
						LevelBasedValue.perLevel(0, 0.02f), 15)
				.trait(NIGHT_EYES, NIGHT_EYES_THRESHOLD,
						LevelBasedValue.constant(4), 22)
				.trait(THICK_SKIN, THICK_SKIN_REDUCTION,
						LevelBasedValue.perLevel(0, 0.012f), 28)
				.trait(GLIDER, GLIDER_BONUS,
						LevelBasedValue.perLevel(0, 0.008f), 36)
				.trait(NOMAD, TraitKey.of(NOMAD, "active"),
						LevelBasedValue.constant(1), 46)

				.build();
	}

	private static LevelBasedValue movementSpeedLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 49},
						new float[]{0.07f, 0.08f, 0.09f, 0.10f, 0.105f, 0.11f, 0.112f, 0.115f, 0.117f, 0.119f, 0.12f},
						50
				),
				LevelBasedValue.constant(0.12f)
		);
	}

	private static LevelBasedValue jumpStrengthLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 49},
						new float[]{0.42f, 0.42f, 0.42f, 0.42f, 0.44f, 0.46f, 0.48f, 0.50f, 0.52f, 0.53f, 0.55f},
						50
				),
				LevelBasedValue.constant(0.55f)
		);
	}

	private static LevelBasedValue safeFallLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 49},
						new float[]{2.0f, 2.3f, 2.6f, 3.0f, 3.3f, 3.6f, 4.0f, 4.4f, 4.8f, 5.4f, 6.0f},
						50
				),
				LevelBasedValue.constant(6.0f)
		);
	}

	private static LevelBasedValue sneakSpeedLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 49},
						new float[]{0.2f, 0.22f, 0.24f, 0.27f, 0.30f, 0.33f, 0.36f, 0.39f, 0.42f, 0.44f, 0.45f},
						50
				),
				LevelBasedValue.constant(0.45f)
		);
	}

	private static LevelBasedValue oxygenLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 49},
						new float[]{0.0f, 0.5f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 10.0f},
						50
				),
				LevelBasedValue.constant(10.0f)
		);
	}

	private Agility() {
	}
}
