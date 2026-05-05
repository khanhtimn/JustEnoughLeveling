package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpFormula;
import dev.khanhtimn.jel.api.trait.TraitKey;
import dev.khanhtimn.jel.core.ModAttributes;
import dev.khanhtimn.jel.content.ModSkills;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import static dev.khanhtimn.jel.misc.Utils.interpolate;

public final class Constitution {

	// --- Trait IDs ---
	// Vitality branch
	public static final ResourceLocation NATURAL_REGEN = Constants.rl("constitution/natural_regen");
	public static final ResourceLocation SECOND_WIND = Constants.rl("constitution/second_wind");
	public static final ResourceLocation HEARTY = Constants.rl("constitution/hearty");
	public static final ResourceLocation FORTITUDE = Constants.rl("constitution/fortitude");
	public static final ResourceLocation UNDYING_WILL = Constants.rl("constitution/undying_will");
	public static final ResourceLocation AVATAR_OF_LIFE = Constants.rl("constitution/avatar_of_life");

	// Sustenance branch
	public static final ResourceLocation EFFICIENT_DIGESTION = Constants.rl("constitution/efficient_digestion");
	public static final ResourceLocation NOURISHING = Constants.rl("constitution/nourishing");
	public static final ResourceLocation IRON_STOMACH = Constants.rl("constitution/iron_stomach");
	public static final ResourceLocation RESPITE = Constants.rl("constitution/respite");
	public static final ResourceLocation FEAST = Constants.rl("constitution/feast");
	public static final ResourceLocation BOTTOMLESS_APPETITE = Constants.rl("constitution/bottomless_appetite");

	// Branch IDs
	public static final ResourceLocation VITALITY_BRANCH = Constants.rl("constitution/vitality");
	public static final ResourceLocation SUSTENANCE_BRANCH = Constants.rl("constitution/sustenance");

	// --- Trait Keys ---
	// Vitality
	public static final TraitKey REGEN_BONUS = TraitKey.of(NATURAL_REGEN, "bonus");
	public static final TraitKey SECOND_WIND_THRESHOLD = TraitKey.of(SECOND_WIND, "threshold");
	public static final TraitKey SECOND_WIND_COOLDOWN = TraitKey.of(SECOND_WIND, "cooldown");
	public static final TraitKey HEARTY_BONUS = TraitKey.of(HEARTY, "bonus");
	public static final TraitKey FORTITUDE_DR = TraitKey.of(FORTITUDE, "dr_bonus");
	public static final TraitKey UNDYING_WILL_THRESHOLD = TraitKey.of(UNDYING_WILL, "threshold");
	public static final TraitKey UNDYING_WILL_COOLDOWN = TraitKey.of(UNDYING_WILL, "cooldown");

	// Sustenance
	public static final TraitKey DIGESTION_REDUCTION = TraitKey.of(EFFICIENT_DIGESTION, "reduction");
	public static final TraitKey NOURISHING_BONUS = TraitKey.of(NOURISHING, "bonus");
	public static final TraitKey IRON_STOMACH_CHANCE = TraitKey.of(IRON_STOMACH, "chance");
	public static final TraitKey RESPITE_BONUS = TraitKey.of(RESPITE, "bonus");
	public static final TraitKey FEAST_DURATION = TraitKey.of(FEAST, "duration");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.constitution.name"))
				.description(Component.translatable("jel.skill.constitution.description"))
				.icon(Items.GOLDEN_APPLE)
				.color(0xFFAA00)
				.maxLevel(50)
				.xpFormula(XpFormula.quadratic(50, 40, 5))

				// --- Base Attributes (Lookup tables) ---
				// MAX_HEALTH: L0=6, L20=20, L50=40
				.base(Attributes.MAX_HEALTH, maxHealthLookup())
				// MAX_FOOD_LEVEL: L0=10, L20=20, L50=40
				.base(ModAttributes.maxFoodLevel(), maxFoodLookup())
				// MAX_ABSORPTION: L0=0, unlocks at L20, L50=10
				.base(Attributes.MAX_ABSORPTION, maxAbsorptionLookup())

				// --- Cross-Skill Requirements ---
				.requiresSkill(30,
						Component.translatable("jel.skill.constitution.req.agility_12"),
						ModSkills.AGILITY.location(), 12)
				.requiresSkill(40,
						Component.translatable("jel.skill.constitution.req.defense_15"),
						ModSkills.DEFENSE.location(), 15)

				// --- Vitality Traits ---
				.trait(NATURAL_REGEN, REGEN_BONUS,
						LevelBasedValue.perLevel(0, 0.02f), 8)
				.trait(SECOND_WIND, SECOND_WIND_THRESHOLD,
						LevelBasedValue.constant(0.15f), 15)
				.trait(SECOND_WIND, SECOND_WIND_COOLDOWN,
						LevelBasedValue.constant(2400), 15)
				.trait(HEARTY, HEARTY_BONUS,
						heartyLookup(), 22)
				.trait(FORTITUDE, FORTITUDE_DR,
						LevelBasedValue.perLevel(0, 0.005f), 28)
				.trait(UNDYING_WILL, UNDYING_WILL_THRESHOLD,
						LevelBasedValue.constant(0.1f), 36)
				.trait(UNDYING_WILL, UNDYING_WILL_COOLDOWN,
						LevelBasedValue.constant(6000), 36)
				.trait(AVATAR_OF_LIFE, TraitKey.of(AVATAR_OF_LIFE, "active"),
						LevelBasedValue.constant(1), 46)

				// --- Sustenance Traits ---
				.trait(EFFICIENT_DIGESTION, DIGESTION_REDUCTION,
						LevelBasedValue.perLevel(0, 0.025f), 8)
				.trait(NOURISHING, NOURISHING_BONUS,
						LevelBasedValue.perLevel(0, 0.04f), 15)
				.trait(IRON_STOMACH, IRON_STOMACH_CHANCE,
						LevelBasedValue.perLevel(0, 0.012f), 22)
				.trait(RESPITE, RESPITE_BONUS,
						LevelBasedValue.perLevel(0, 0.01f), 28)
				.trait(FEAST, FEAST_DURATION,
						feastDurationLookup(), 36)
				.trait(BOTTOMLESS_APPETITE, TraitKey.of(BOTTOMLESS_APPETITE, "active"),
						LevelBasedValue.constant(1), 46)

				// Permanent Regeneration I at max level — handled by Avatar of Life trait instead of .effect()
				.build();
	}

	/**
	 * MAX_HEALTH: 50 values for levels 0–49 (called as calculate(1)..calculate(50)).
	 * Key points: L0=6, L5=10, L10=14, L15=17, L20=20, L25=24, L30=28, L35=32, L40=36, L45=38, L50=40
	 */
	private static LevelBasedValue maxHealthLookup() {
		return LevelBasedValue.lookup(
				interpolate(new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 49},
						new float[]{6, 10, 14, 17, 20, 24, 28, 32, 36, 38, 40},
						50
				),
				LevelBasedValue.constant(40)
		);
	}

	/**
	 * MAX_FOOD_LEVEL: L0=10, L5=13, L10=16, L15=18, L20=20, L25=24, L30=28, L35=32, L40=36, L45=38, L50=40
	 */
	private static LevelBasedValue maxFoodLookup() {
		return LevelBasedValue.lookup(
				interpolate(new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 49},
						new float[]{10, 13, 16, 18, 20, 24, 28, 32, 36, 38, 40},
						50
				),
				LevelBasedValue.constant(40)
		);
	}

	/**
	 * MAX_ABSORPTION: stays 0 until L20, then scales linearly to 10 at L50.
	 */
	private static LevelBasedValue maxAbsorptionLookup() {
		return LevelBasedValue.lookup(
				interpolate(new int[]{0, 19, 20, 25, 30, 35, 40, 45, 49},
						new float[]{0, 0, 2, 3, 4, 6, 7, 9, 10},
						50
				),
				LevelBasedValue.constant(10)
		);
	}

	/**
	 * Hearty bonus: +% max HP multiplicative. Only active from unlock level.
	 * Key points at L22=0.02, L28=0.04, L33=0.07, L38=0.10, L43=0.14, L48=0.18
	 */
	private static LevelBasedValue heartyLookup() {
		return LevelBasedValue.lookup(
				interpolate(new int[]{0, 21, 22, 28, 33, 38, 43, 48, 49},
						new float[]{0, 0, 0.02f, 0.04f, 0.07f, 0.10f, 0.14f, 0.18f, 0.18f},
						50
				),
				LevelBasedValue.constant(0.18f)
		);
	}

	/**
	 * Feast duration: ticks of Strength I after eating.
	 * L36=60, L39=80, L42=100, L45=120, L48=140, L50=160
	 */
	private static LevelBasedValue feastDurationLookup() {
		return LevelBasedValue.lookup(
				interpolate(new int[]{0, 35, 36, 39, 42, 45, 48, 49},
						new float[]{0, 0, 60, 80, 100, 120, 140, 160},
						50
				),
				LevelBasedValue.constant(160)
		);
	}


	private Constitution() {
	}
}
