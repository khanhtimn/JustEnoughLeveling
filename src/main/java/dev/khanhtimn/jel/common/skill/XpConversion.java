package dev.khanhtimn.jel.common.skill;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.stream.Stream;

/**
 * Data-driven conversion between vanilla XP and skill XP.
 * <p>
 * Configured per-skill via the {@code xp_conversion} field in the
 * {@link SkillDefinition} datapack JSON. If omitted, defaults to
 * {@link Identity} (1:1 ratio).
 *
 * <h2>JSON examples</h2>
 * <pre>{@code
 * // 1:1 (default)
 * "xp_conversion": { "type": "identity" }
 *
 * // Fixed ratio: 2 vanilla XP = 1 skill XP
 * "xp_conversion": { "type": "ratio", "vanilla_per_skill_xp": 2.0 }
 *
 * // Scaled: higher skills cost more vanilla XP per skill XP
 * "xp_conversion": { "type": "scaled", "base_rate": 1.0, "scale_per_level": 0.1 }
 * }</pre>
 */
public sealed interface XpConversion
        permits XpConversion.Identity, XpConversion.Ratio, XpConversion.Scaled {

    static XpConversion identity() {
        return Identity.INSTANCE;
    }

    static XpConversion ratio(double vanillaPerSkillXp) {
        return new Ratio(vanillaPerSkillXp);
    }

    static XpConversion scaled(double baseRate, double scalePerLevel) {
        return new Scaled(baseRate, scalePerLevel);
    }

    MapCodec<XpConversion> MAP_CODEC = new MapCodec<>() {
        @Override
        public <T> DataResult<XpConversion> decode(DynamicOps<T> ops, MapLike<T> input) {
            T typeElem = input.get("type");
            if (typeElem == null) {
                return DataResult.success(Identity.INSTANCE);
            }
            return Codec.STRING.parse(ops, typeElem).flatMap(type -> switch (type) {
                case "identity" ->
                    Identity.MAP_CODEC.decode(ops, input).map(v -> v);
                case "ratio" ->
                    Ratio.MAP_CODEC.decode(ops, input).map(v -> v);
                case "scaled" ->
                    Scaled.MAP_CODEC.decode(ops, input).map(v -> v);
                default ->
                    DataResult.error(() -> "Unknown XpConversion type: " + type);
            });
        }

        @Override
        public <T> RecordBuilder<T> encode(XpConversion input, DynamicOps<T> ops, RecordBuilder<T> builder) {
            builder.add("type", ops.createString(input.type()));
            return switch (input) {
                case Identity id ->
                    Identity.MAP_CODEC.encode(id, ops, builder);
                case Ratio ratio ->
                    Ratio.MAP_CODEC.encode(ratio, ops, builder);
                case Scaled scaled ->
                    Scaled.MAP_CODEC.encode(scaled, ops, builder);
            };
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("type"));
        }

        @Override
        public String toString() {
            return "XpConversion.MAP_CODEC";
        }
    };

    Codec<XpConversion> CODEC = MAP_CODEC.codec();

    String type();

    /**
     * Convert vanilla XP points to skill XP.
     *
     * @param vanillaXp amount of vanilla XP being spent
     * @param skillLevel the player's current level in this skill (for scaled
     * conversions)
     * @return skill XP gained
     */
    int vanillaToSkillXp(int vanillaXp, int skillLevel);

    /**
     * Convert skill XP back to vanilla XP (for refunds).
     *
     * @param skillXp amount of skill XP to convert back
     * @param skillLevel the player's current level in this skill
     * @return vanilla XP refunded
     */
    int skillToVanillaXp(int skillXp, int skillLevel);

    // ---- Implementations ----
    /**
     * 1:1 conversion (default).
     */
    record Identity() implements XpConversion {

        public static final Identity INSTANCE = new Identity();
        public static final MapCodec<Identity> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public String type() {
            return "identity";
        }

        @Override
        public int vanillaToSkillXp(int vanillaXp, int skillLevel) {
            return vanillaXp;
        }

        @Override
        public int skillToVanillaXp(int skillXp, int skillLevel) {
            return skillXp;
        }
    }

    /**
     * Fixed ratio: {@code vanillaPerSkillXp} vanilla XP = 1 skill XP.
     * <p>
     * Example: {@code vanillaPerSkillXp = 2.0} means spending 10 vanilla XP
     * grants 5 skill XP.
     */
    record Ratio(double vanillaPerSkillXp) implements XpConversion {

        public static final MapCodec<Ratio> MAP_CODEC
                = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("vanilla_per_skill_xp")
                        .forGetter(Ratio::vanillaPerSkillXp)
        ).apply(instance, Ratio::new));

        public Ratio {
            if (vanillaPerSkillXp <= 0) {
                vanillaPerSkillXp = 1.0;
            }
        }

        @Override
        public String type() {
            return "ratio";
        }

        @Override
        public int vanillaToSkillXp(int vanillaXp, int skillLevel) {
            return Math.max(0, (int) Math.round(vanillaXp / vanillaPerSkillXp));
        }

        @Override
        public int skillToVanillaXp(int skillXp, int skillLevel) {
            return Math.max(0, (int) Math.round(skillXp * vanillaPerSkillXp));
        }
    }

    /**
     * Level-scaled conversion: each skill level makes vanilla XP less
     * efficient.
     * <p>
     * Effective rate = {@code baseRate + (skillLevel * scalePerLevel)}. Higher
     * rate means more vanilla XP needed per skill XP.
     */
    record Scaled(double baseRate, double scalePerLevel) implements XpConversion {

        public static final MapCodec<Scaled> MAP_CODEC
                = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("base_rate")
                        .forGetter(Scaled::baseRate),
                Codec.DOUBLE.fieldOf("scale_per_level")
                        .forGetter(Scaled::scalePerLevel)
        ).apply(instance, Scaled::new));

        public Scaled {
            if (baseRate <= 0) {
                baseRate = 1.0;
            }
            if (scalePerLevel < 0) {
                scalePerLevel = 0.0;
            }
        }

        @Override
        public String type() {
            return "scaled";
        }

        private double effectiveRate(int skillLevel) {
            return Math.max(0.01, baseRate + skillLevel * scalePerLevel);
        }

        @Override
        public int vanillaToSkillXp(int vanillaXp, int skillLevel) {
            return Math.max(0, (int) Math.round(vanillaXp / effectiveRate(skillLevel)));
        }

        @Override
        public int skillToVanillaXp(int skillXp, int skillLevel) {
            return Math.max(0, (int) Math.round(skillXp * effectiveRate(skillLevel)));
        }
    }
}
