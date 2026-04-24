package dev.khanhtimn.jel.api.perk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import dev.khanhtimn.jel.common.EffectTracker;

/**
 * Grants a mob effect with infinite duration while the skill meets the unlock
 * level. The amplifier can scale with level via {@link LevelBasedValue}.
 * <p>
 * Uses {@link ApplyMode#STATE} — re-applied every recompute cycle. Idempotent:
 * re-adding an existing effect refreshes rather than stacks.
 *
 * <h2>JSON</h2>
 * <pre>{@code
 * // Constant amplifier
 * { "type": "jel:effect", "effect": "minecraft:regeneration",
 *   "amplifier": 0, "unlock_level": 10 }
 *
 * // Scaling amplifier
 * { "type": "jel:effect", "effect": "minecraft:regeneration",
 *   "amplifier": { "type": "minecraft:linear", "base": 0, "per_level_above_first": 0.1 },
 *   "unlock_level": 10 }
 * }</pre>
 */
public record EffectPerk(
		int unlockLevel,
		ResourceLocation effect,
		LevelBasedValue amplifier,
		boolean ambient,
		boolean showParticles,
		boolean showIcon
) implements Perk {

	public static final MapCodec<EffectPerk> CODEC
			= RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.fieldOf("unlock_level")
					.forGetter(EffectPerk::unlockLevel),
			ResourceLocation.CODEC.fieldOf("effect")
					.forGetter(EffectPerk::effect),
			LevelBasedValue.CODEC.optionalFieldOf("amplifier", LevelBasedValue.constant(0))
					.forGetter(EffectPerk::amplifier),
			Codec.BOOL.optionalFieldOf("ambient", false)
					.forGetter(EffectPerk::ambient),
			Codec.BOOL.optionalFieldOf("show_particles", false)
					.forGetter(EffectPerk::showParticles),
			Codec.BOOL.optionalFieldOf("show_icon", false)
					.forGetter(EffectPerk::showIcon)
	).apply(instance, EffectPerk::new));

	static ResourceLocation unwrap(Holder<MobEffect> effect) {
		return effect.unwrapKey().orElseThrow().location();
	}


	public static EffectPerk of(Holder<MobEffect> effect, int amplifier, int unlockLevel) {
		return new EffectPerk(unlockLevel, unwrap(effect), LevelBasedValue.constant(amplifier), false, false, false);
	}

	public static EffectPerk of(Holder<MobEffect> effect, LevelBasedValue amplifier, int unlockLevel) {
		return new EffectPerk(unlockLevel, unwrap(effect), amplifier, false, false, false);
	}

	public static EffectPerk of(Holder<MobEffect> effect, int amplifier, boolean ambient, boolean showParticles, int unlockLevel) {
		return new EffectPerk(unlockLevel, unwrap(effect), LevelBasedValue.constant(amplifier), ambient, showParticles, false);
	}

	public static EffectPerk of(Holder<MobEffect> effect, LevelBasedValue amplifier, boolean ambient, boolean showParticles, int unlockLevel) {
		return new EffectPerk(unlockLevel, unwrap(effect), amplifier, ambient, showParticles, false);
	}

	public static EffectPerk of(Holder<MobEffect> effect, int amplifier, boolean ambient, boolean showParticles, boolean showIcon, int unlockLevel) {
		return new EffectPerk(unlockLevel, unwrap(effect), LevelBasedValue.constant(amplifier), ambient, showParticles, showIcon);
	}

	public static EffectPerk of(Holder<MobEffect> effect, LevelBasedValue amplifier, boolean ambient, boolean showParticles, boolean showIcon, int unlockLevel) {
		return new EffectPerk(unlockLevel, unwrap(effect), amplifier, ambient, showParticles, showIcon);
	}


	public static EffectPerk of(ResourceLocation effect, int amplifier, int unlockLevel) {
		return new EffectPerk(unlockLevel, effect, LevelBasedValue.constant(amplifier), false, false, false);
	}

	public static EffectPerk of(ResourceLocation effect, LevelBasedValue amplifier, int unlockLevel) {
		return new EffectPerk(unlockLevel, effect, amplifier, false, false, false);
	}


	public static EffectPerk of(String effect, int amplifier, int unlockLevel) {
		return new EffectPerk(unlockLevel, ResourceLocation.parse(effect), LevelBasedValue.constant(amplifier), false, false, false);
	}

	public static EffectPerk of(String effect, LevelBasedValue amplifier, int unlockLevel) {
		return new EffectPerk(unlockLevel, ResourceLocation.parse(effect), amplifier, false, false, false);
	}

	@Override
	public PerkType<?> type() {
		return PerkType.EFFECT;
	}

	@Override
	public void apply(PerkContext ctx, int currentLevel) {
		var holder = resolveEffect();
		if (holder == null) {
			return;
		}

		ServerPlayer player = ctx.player();
		var tracker = (EffectTracker) player;
		MobEffectInstance existing = player.getEffect(holder);

		// Snapshot existing non-JEL effect before vanilla's addEffect() might discard it.
		MobEffectInstance displaced = (existing != null && !tracker.jel$ownsEffect(holder))
				? new MobEffectInstance(existing)
				: null;

		int amp = Math.max(0, (int) amplifier.calculate(effectiveLevel(currentLevel)));

		player.addEffect(new MobEffectInstance(
				holder,
				MobEffectInstance.INFINITE_DURATION,
				amp,
				ambient,
				showParticles,
				showIcon
		));
		tracker.jel$claimEffect(holder, displaced);
	}

	@Override
	public void revoke(PerkContext ctx) {
		var holder = resolveEffect();
		if (holder == null) {
			return;
		}

		ServerPlayer player = ctx.player();
		var tracker = (EffectTracker) player;

		if (tracker.jel$ownsEffect(holder)) {
			MobEffectInstance active = player.getEffect(holder);
			// Only remove if the active effect is still infinite-duration.
			// If another source (potion, command) overwrote ours with a finite
			// effect, we must not nuke it — just release ownership.
			if (active != null && active.isInfiniteDuration()) {
				player.removeEffect(holder);
			}

			MobEffectInstance displaced = tracker.jel$releaseEffect(holder);
			if (displaced != null) {
				player.addEffect(displaced);
			}
		}
	}

	private Holder<MobEffect> resolveEffect() {
		return BuiltInRegistries.MOB_EFFECT
				.getHolder(effect)
				.orElse(null);
	}
}
