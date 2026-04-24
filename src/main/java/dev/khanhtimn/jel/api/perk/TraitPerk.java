package dev.khanhtimn.jel.api.perk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * Grants a named, level-scaled parameter when the skill reaches the unlock level.
 * <p>
 * Traits are the primary mechanism for JEL core and third-party mods to
 * gate and scale logic based on skill progression. Query via
 * {@link dev.khanhtimn.jel.api.JelTraits#has JelTraits.has()} and
 * {@link dev.khanhtimn.jel.api.JelTraits#value JelTraits.value()}.
 * <p>
 * When {@code value} is omitted (defaults to 1.0), the trait behaves as a
 * pure boolean gate. When a {@link LevelBasedValue} formula is provided,
 * the computed value scales with the player's skill level.
 *
 * <h2>JSON</h2>
 * <pre>{@code
 * // Boolean trait
 * { "type": "jel:trait", "trait": "jel:double_jump", "unlock_level": 15 }
 *
 * // Valued trait (scales with level)
 * { "type": "jel:trait", "trait": "jel:bonus_regen",
 *   "value": { "type": "minecraft:linear", "base": 0, "per_level_above_first": 0.025 },
 *   "unlock_level": 10 }
 * }</pre>
 */
public record TraitPerk(
		int unlockLevel,
		ResourceLocation trait,
		LevelBasedValue value
) implements Perk {

	public static final MapCodec<TraitPerk> CODEC
			= RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.fieldOf("unlock_level")
					.forGetter(TraitPerk::unlockLevel),
			ResourceLocation.CODEC.fieldOf("trait")
					.forGetter(TraitPerk::trait),
			LevelBasedValue.CODEC.optionalFieldOf("value", LevelBasedValue.constant(1f))
					.forGetter(TraitPerk::value)
	).apply(instance, TraitPerk::new));

	@Override
	public PerkType<?> type() {
		return PerkType.TRAIT;
	}

	@Override
	public void apply(PerkContext ctx, int currentLevel) {
		float computed = value.calculate(effectiveLevel(currentLevel));
		ctx.tracker().setTraitValue(trait, computed);
	}

	@Override
	public void revoke(PerkContext ctx) {
		ctx.tracker().removeTraitValue(trait);
	}
}
