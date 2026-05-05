package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpFormula;
import dev.khanhtimn.jel.api.trait.TraitKey;
import dev.khanhtimn.jel.content.ModSkills;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import static dev.khanhtimn.jel.misc.Utils.interpolate;

public final class Farming {

	// --- Branch IDs ---
	public static final ResourceLocation CULTIVATOR_BRANCH = Constants.rl("farming/cultivator");
	public static final ResourceLocation RANCHER_BRANCH = Constants.rl("farming/rancher");

	// --- Non-Branched Trait IDs ---
	public static final ResourceLocation HARVEST_YIELD = Constants.rl("farming/harvest_yield");
	public static final ResourceLocation BONEMEAL_FAILURE = Constants.rl("farming/bonemeal_failure");
	public static final ResourceLocation BREEDING_PENALTY = Constants.rl("farming/breeding_penalty");
	public static final ResourceLocation SEED_SAVANT = Constants.rl("farming/seed_savant");

	// --- Cultivator Trait IDs ---
	public static final ResourceLocation BOUNTIFUL_HARVEST = Constants.rl("farming/bountiful_harvest");
	public static final ResourceLocation GREEN_THUMB = Constants.rl("farming/green_thumb");
	public static final ResourceLocation FERTILE_TOUCH = Constants.rl("farming/fertile_touch");
	public static final ResourceLocation HARVEST_FESTIVAL = Constants.rl("farming/harvest_festival");
	public static final ResourceLocation PHOTOSYNTHESIS = Constants.rl("farming/photosynthesis");
	public static final ResourceLocation GAIAS_BLESSING = Constants.rl("farming/gaias_blessing");

	// --- Rancher Trait IDs ---
	public static final ResourceLocation BREED_TWIN = Constants.rl("farming/breed_twin");
	public static final ResourceLocation SHEPHERDS_CARE = Constants.rl("farming/shepherds_care");
	public static final ResourceLocation HERDERS_CALL = Constants.rl("farming/herders_call");
	public static final ResourceLocation BEASTMASTER = Constants.rl("farming/beastmaster");
	public static final ResourceLocation PACK_LEADER = Constants.rl("farming/pack_leader");
	public static final ResourceLocation ALPHA_OF_PACK = Constants.rl("farming/alpha_of_pack");

	// --- Angler Trait IDs (within Rancher branch) ---
	public static final ResourceLocation ANGLERS_INSTINCT = Constants.rl("farming/anglers_instinct");
	public static final ResourceLocation FISHERMANS_FORTUNE = Constants.rl("farming/fishermans_fortune");
	public static final ResourceLocation TREASURE_HUNTER = Constants.rl("farming/treasure_hunter");

	// --- Non-Branched Trait Keys ---
	public static final TraitKey HARVEST_YIELD_MULTIPLIER = TraitKey.of(HARVEST_YIELD, "multiplier");
	public static final TraitKey BONEMEAL_FAILURE_CHANCE = TraitKey.of(BONEMEAL_FAILURE, "chance");
	public static final TraitKey BREEDING_PENALTY_MULTIPLIER = TraitKey.of(BREEDING_PENALTY, "multiplier");
	public static final TraitKey SEED_SAVANT_CHANCE = TraitKey.of(SEED_SAVANT, "chance");

	// --- Cultivator Trait Keys ---
	public static final TraitKey BOUNTIFUL_HARVEST_CHANCE = TraitKey.of(BOUNTIFUL_HARVEST, "chance");
	public static final TraitKey GREEN_THUMB_CHANCE = TraitKey.of(GREEN_THUMB, "chance");
	public static final TraitKey FERTILE_TOUCH_BONUS = TraitKey.of(FERTILE_TOUCH, "bonus");
	public static final TraitKey HARVEST_FESTIVAL_RADIUS = TraitKey.of(HARVEST_FESTIVAL, "radius");
	public static final TraitKey HARVEST_FESTIVAL_COOLDOWN = TraitKey.of(HARVEST_FESTIVAL, "cooldown");
	public static final TraitKey PHOTOSYNTHESIS_SPEED = TraitKey.of(PHOTOSYNTHESIS, "speed_bonus");
	public static final TraitKey GAIAS_BLESSING_ACTIVE = TraitKey.of(GAIAS_BLESSING, "active");

	// --- Rancher Trait Keys ---
	public static final TraitKey BREED_TWIN_CHANCE = TraitKey.of(BREED_TWIN, "chance");
	public static final TraitKey SHEPHERDS_CARE_BONUS = TraitKey.of(SHEPHERDS_CARE, "bonus");
	public static final TraitKey HERDERS_CALL_RADIUS = TraitKey.of(HERDERS_CALL, "radius");
	public static final TraitKey BEASTMASTER_REDUCTION = TraitKey.of(BEASTMASTER, "reduction");
	public static final TraitKey PACK_LEADER_DAMAGE = TraitKey.of(PACK_LEADER, "damage_bonus");
	public static final TraitKey ALPHA_OF_PACK_ACTIVE = TraitKey.of(ALPHA_OF_PACK, "active");

	// --- Angler Trait Keys ---
	public static final TraitKey ANGLERS_INSTINCT_SPEED = TraitKey.of(ANGLERS_INSTINCT, "speed_bonus");
	public static final TraitKey FISHERMANS_FORTUNE_CHANCE = TraitKey.of(FISHERMANS_FORTUNE, "chance");
	public static final TraitKey TREASURE_HUNTER_BONUS = TraitKey.of(TREASURE_HUNTER, "bonus");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.farming.name"))
				.description(Component.translatable("jel.skill.farming.description"))
				.icon(Items.DIAMOND_HOE)
				.color(0x55FF55)
				.maxLevel(50)
				.xpFormula(XpFormula.quadratic(50, 40, 5))

				// --- Base Penalty Curves (active from L0) ---
				// Harvest Yield: L0=0.70, L15=1.00, L50=1.30
				.trait(HARVEST_YIELD, HARVEST_YIELD_MULTIPLIER,
						harvestYieldLookup(), 0)
				// Bone Meal Failure: L0=0.40, L15=0.05, L20+=0.00
				.trait(BONEMEAL_FAILURE, BONEMEAL_FAILURE_CHANCE,
						bonemealFailureLookup(), 0)
				// Breeding Cooldown Penalty: L0=1.50, L15=1.00, L20+=1.00
				.trait(BREEDING_PENALTY, BREEDING_PENALTY_MULTIPLIER,
						breedingPenaltyLookup(), 0)

				// --- Cross-Skill Requirements ---
				.requiresSkill(20,
						Component.translatable("jel.skill.farming.req.constitution_10"),
						ModSkills.CONSTITUTION.location(), 10)
				.requiresSkill(30,
						Component.translatable("jel.skill.farming.req.cooking_12"),
						ModSkills.COOKING.location(), 12)
				.requiresSkill(40,
						Component.translatable("jel.skill.farming.req.constitution_20_mining_15"),
						ModSkills.CONSTITUTION.location(), 20)
				.requiresSkill(40,
						Component.translatable("jel.skill.farming.req.mining_15"),
						ModSkills.MINING.location(), 15)
				.requiresSkill(48,
						Component.translatable("jel.skill.farming.req.cooking_25"),
						ModSkills.COOKING.location(), 25)

				// --- Non-Branched Trait ---
				.trait(SEED_SAVANT, SEED_SAVANT_CHANCE,
						LevelBasedValue.perLevel(0, 0.02f), 10)

				// --- Cultivator Traits ---
				.trait(BOUNTIFUL_HARVEST, BOUNTIFUL_HARVEST_CHANCE,
						LevelBasedValue.perLevel(0, 0.02f), 8)
				.trait(GREEN_THUMB, GREEN_THUMB_CHANCE,
						LevelBasedValue.perLevel(0, 0.015f), 15)
				.trait(FERTILE_TOUCH, FERTILE_TOUCH_BONUS,
						LevelBasedValue.perLevel(0, 0.02f), 22)
				.trait(HARVEST_FESTIVAL, HARVEST_FESTIVAL_RADIUS,
						LevelBasedValue.constant(3), 28)
				.trait(HARVEST_FESTIVAL, HARVEST_FESTIVAL_COOLDOWN,
						LevelBasedValue.constant(100), 28)
				.trait(PHOTOSYNTHESIS, PHOTOSYNTHESIS_SPEED,
						LevelBasedValue.perLevel(0, 0.015f), 36)
				.trait(GAIAS_BLESSING, GAIAS_BLESSING_ACTIVE,
						LevelBasedValue.constant(1), 46)

				// --- Rancher Traits ---
				.trait(BREED_TWIN, BREED_TWIN_CHANCE,
						LevelBasedValue.perLevel(0, 0.015f), 8)
				.trait(SHEPHERDS_CARE, SHEPHERDS_CARE_BONUS,
						LevelBasedValue.perLevel(0, 0.03f), 15)
				.trait(HERDERS_CALL, HERDERS_CALL_RADIUS,
						LevelBasedValue.constant(8), 22)
				.trait(BEASTMASTER, BEASTMASTER_REDUCTION,
						LevelBasedValue.perLevel(0, 0.02f), 28)
				.trait(PACK_LEADER, PACK_LEADER_DAMAGE,
						LevelBasedValue.perLevel(0, 0.02f), 36)
				.trait(ALPHA_OF_PACK, ALPHA_OF_PACK_ACTIVE,
						LevelBasedValue.constant(1), 46)

				// --- Angler Traits (within Rancher branch) ---
				.trait(ANGLERS_INSTINCT, ANGLERS_INSTINCT_SPEED,
						LevelBasedValue.perLevel(0, 0.005f), 12)
				.trait(FISHERMANS_FORTUNE, FISHERMANS_FORTUNE_CHANCE,
						LevelBasedValue.perLevel(0, 0.012f), 20)
				.trait(TREASURE_HUNTER, TREASURE_HUNTER_BONUS,
						LevelBasedValue.perLevel(0, 0.01f), 32)

				.build();
	}

	/**
	 * L0=0.70, L5=0.78, L10=0.88, L15=1.00, L20=1.05,
	 * L25=1.10, L30=1.15, L35=1.18, L40=1.22, L45=1.26, L50=1.30
	 */
	private static LevelBasedValue harvestYieldLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 49},
						new float[]{0.70f, 0.78f, 0.88f, 1.00f, 1.05f, 1.10f, 1.15f, 1.18f, 1.22f, 1.26f, 1.30f},
						50
				),
				LevelBasedValue.constant(1.30f)
		);
	}

	/**
	 * L0=0.40, L5=0.30, L10=0.15, L15=0.05, L20+=0.00
	 */
	private static LevelBasedValue bonemealFailureLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 5, 10, 15, 19, 49},
						new float[]{0.40f, 0.30f, 0.15f, 0.05f, 0.00f, 0.00f},
						50
				),
				LevelBasedValue.constant(0.00f)
		);
	}

	/**
	 * L0=1.50, L5=1.35, L10=1.15, L15=1.00, L20+=1.00
	 */
	private static LevelBasedValue breedingPenaltyLookup() {
		return LevelBasedValue.lookup(
				interpolate(
						new int[]{0, 5, 10, 15, 19, 49},
						new float[]{1.50f, 1.35f, 1.15f, 1.00f, 1.00f, 1.00f},
						50
				),
				LevelBasedValue.constant(1.00f)
		);
	}

	private Farming() {
	}
}
