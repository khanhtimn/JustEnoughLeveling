package dev.khanhtimn.jel.common.skill.impl.perk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.khanhtimn.jel.common.effect.JelDisplacedEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Grants a mob effect with infinite duration while the skill meets the unlock
 * level.
 * <p>
 * Uses {@link ApplyMode#STATE} — re-applied every recompute cycle. Idempotent:
 * re-adding an existing effect refreshes rather than stacks.
 * <p>
 * <h2>JSON</h2>
 * <pre>{@code
 * { "type": "jel:effect", "effect": "minecraft:regeneration",
 *   "amplifier": 0, "unlock_level": 10 }
 * }</pre>
 */
public record EffectPerk(
        int unlockLevel,
        ResourceLocation effect,
        int amplifier,
        boolean showParticles
        ) implements Perk {

    public EffectPerk(int unlockLevel, ResourceLocation effect, int amplifier) {
        this(unlockLevel, effect, amplifier, false);
    }

    public static final MapCodec<EffectPerk> CODEC
            = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("unlock_level")
                    .forGetter(EffectPerk::unlockLevel),
            ResourceLocation.CODEC.fieldOf("effect")
                    .forGetter(EffectPerk::effect),
            Codec.INT.optionalFieldOf("amplifier", 0)
                    .forGetter(EffectPerk::amplifier),
            Codec.BOOL.optionalFieldOf("show_particles", false)
                    .forGetter(EffectPerk::showParticles)
    ).apply(instance, EffectPerk::new));

    public static EffectPerk of(Holder<MobEffect> effect, int amplifier, int unlockLevel) {
        return new EffectPerk(unlockLevel, effect.unwrapKey().orElseThrow().location(), amplifier);
    }

    public static EffectPerk of(Holder<MobEffect> effect, int amplifier, boolean showParticles, int unlockLevel) {
        return new EffectPerk(unlockLevel, effect.unwrapKey().orElseThrow().location(), amplifier, showParticles);
    }

    public static EffectPerk of(String effect, int amplifier, int unlockLevel) {
        return new EffectPerk(unlockLevel, ResourceLocation.parse(effect), amplifier);
    }

    public static EffectPerk of(ResourceLocation effect, int amplifier, int unlockLevel) {
        return new EffectPerk(unlockLevel, effect, amplifier);
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
        MobEffectInstance existing = player.getEffect(holder);

        // Snapshot existing non-JEL effect before vanilla's addEffect() might discard it.
        // Must copy — vanilla's update() mutates the existing instance in-place.
        if (existing != null && !isOwned(existing)) {
            ((JelDisplacedEffects) player).jel$saveDisplacedEffect(holder, new MobEffectInstance(existing));
        }

        player.addEffect(new MobEffectInstance(
                holder,
                MobEffectInstance.INFINITE_DURATION,
                amplifier,
                false,
                showParticles,
                true
        ));
    }

    @Override
    public void revoke(PerkContext ctx) {
        var holder = resolveEffect();
        if (holder == null) {
            return;
        }

        ServerPlayer player = ctx.player();
        MobEffectInstance active = player.getEffect(holder);

        if (active != null && isOwned(active)) {
            player.removeEffect(holder);
        }

        MobEffectInstance displaced
                = ((JelDisplacedEffects) player).jel$popDisplacedEffect(holder);
        if (displaced != null) {
            player.addEffect(displaced);
        }
    }

    private boolean isOwned(MobEffectInstance instance) {
        return instance.isInfiniteDuration()
                && !instance.isAmbient()
                && instance.getAmplifier() == amplifier;
    }

    private Holder<MobEffect> resolveEffect() {
        return BuiltInRegistries.MOB_EFFECT
                .getHolder(effect)
                .orElse(null);
    }
}
