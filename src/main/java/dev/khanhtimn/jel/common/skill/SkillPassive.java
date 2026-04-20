package dev.khanhtimn.jel.common.skill;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.data.skill.SkillsTracker;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * A passive effect granted by a skill at a certain level.
 * <p>
 * Each variant defines its own {@link #apply} and {@link #revoke} behavior,
 * making it trivial to add new passive types without modifying apply logic
 * elsewhere.
 */
public sealed interface SkillPassive
        permits SkillPassive.AttributeBonus, SkillPassive.AbilityFlag {

    static SkillPassive attributeBonus(int unlockLevel, String attribute, double amountPerLevel, AttributeModifier.Operation operation) {
        return new AttributeBonus(unlockLevel, ResourceLocation.parse(attribute), amountPerLevel, operation);
    }

    static SkillPassive abilityFlag(int unlockLevel, String flagId) {
        return new AbilityFlag(unlockLevel, ResourceLocation.parse(flagId));
    }

    // ---- Polymorphic codec ----
    MapCodec<SkillPassive> MAP_CODEC = new MapCodec<>() {
        @Override
        public <T> DataResult<SkillPassive> decode(DynamicOps<T> ops, MapLike<T> input) {
            T typeElem = input.get("type");
            if (typeElem == null) {
                return DataResult.error(() -> "Missing 'type' field for SkillPassive");
            }
            return Codec.STRING.parse(ops, typeElem).flatMap(type -> switch (type) {
                case "attribute" ->
                    AttributeBonus.MAP_CODEC.decode(ops, input).map(v -> v);
                case "ability_flag" ->
                    AbilityFlag.MAP_CODEC.decode(ops, input).map(v -> v);
                default ->
                    DataResult.error(() -> "Unknown SkillPassive type: " + type);
            });
        }

        @Override
        public <T> RecordBuilder<T> encode(SkillPassive input, DynamicOps<T> ops, RecordBuilder<T> builder) {
            builder.add("type", ops.createString(input.type()));
            if (input instanceof AttributeBonus attr) {
                return AttributeBonus.MAP_CODEC.encode(attr, ops, builder);
            } else if (input instanceof AbilityFlag flag) {
                return AbilityFlag.MAP_CODEC.encode(flag, ops, builder);
            }
            return builder.withErrorsFrom(DataResult.error(() -> "Unsupported SkillPassive: " + input.getClass().getName()));
        }

        @Override
        public String toString() {
            return "SkillPassive.MAP_CODEC";
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("type"));
        }
    };

    Codec<SkillPassive> CODEC = MAP_CODEC.codec();

    // ---- Interface ----
    String type();

    /**
     * The skill level at which this passive unlocks.
     */
    int unlockLevel();

    /**
     * Apply this passive's effect for the given context. Called when the
     * player's skill level is {@code >= unlockLevel()}.
     */
    void apply(Context ctx, int currentLevel);

    /**
     * Remove this passive's effect. Called before recomputation to ensure a
     * clean slate.
     */
    void revoke(Context ctx);

    /**
     * Context passed to {@link #apply} and {@link #revoke}. Provides full
     * server-side access for maximum extensibility.
     */
    record Context(
            ServerPlayer player,
            SkillsTracker tracker,
            RegistryAccess registryAccess,
            ResourceLocation skillId,
            int passiveIndex
            ) {

        /**
         * Compute a stable, unique resource location for attribute modifiers
         * created by this passive.
         */
        public ResourceLocation modifierId() {
            String path = "skill/" + skillId.getNamespace()
                    + "/" + skillId.getPath()
                    + "/" + passiveIndex;
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
        }
    }

    // ---- Implementations ----
    record AttributeBonus(
            int unlockLevel,
            ResourceLocation attribute,
            double amountPerLevel,
            AttributeModifier.Operation operation
            ) implements SkillPassive {

        public static final MapCodec<AttributeBonus> MAP_CODEC
                = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("unlock_level")
                        .forGetter(AttributeBonus::unlockLevel),
                ResourceLocation.CODEC.fieldOf("attribute")
                        .forGetter(AttributeBonus::attribute),
                Codec.DOUBLE.fieldOf("amount_per_level")
                        .forGetter(AttributeBonus::amountPerLevel),
                AttributeModifier.Operation.CODEC.fieldOf("operation")
                        .forGetter(AttributeBonus::operation)
        ).apply(instance, AttributeBonus::new));

        public static final Codec<AttributeBonus> CODEC = MAP_CODEC.codec();

        @Override
        public String type() {
            return "attribute";
        }

        @Override
        public void apply(Context ctx, int currentLevel) {
            if (currentLevel < unlockLevel) {
                return;
            }

            Registry<Attribute> attrRegistry
                    = ctx.registryAccess().registryOrThrow(Registries.ATTRIBUTE);

            ResourceKey<Attribute> attrKey
                    = ResourceKey.create(Registries.ATTRIBUTE, attribute);
            var attrHolderOpt = attrRegistry.getHolder(attrKey);
            if (attrHolderOpt.isEmpty()) {
                return;
            }
            Holder<Attribute> attrHolder = attrHolderOpt.get();

            AttributeInstance instance = ctx.player().getAttributes().getInstance(attrHolder);
            if (instance == null) {
                return;
            }

            int effectiveLevels = Math.max(0, currentLevel - unlockLevel + 1);
            double amount = effectiveLevels * amountPerLevel;
            if (amount == 0.0) {
                return;
            }

            AttributeModifier modifier = new AttributeModifier(
                    ctx.modifierId(),
                    amount,
                    operation
            );
            // Remove existing modifier first to ensure idempotency
            instance.removeModifier(ctx.modifierId());
            instance.addPermanentModifier(modifier);
        }

        @Override
        public void revoke(Context ctx) {
            Registry<Attribute> attrRegistry
                    = ctx.registryAccess().registryOrThrow(Registries.ATTRIBUTE);

            ResourceKey<Attribute> attrKey
                    = ResourceKey.create(Registries.ATTRIBUTE, attribute);
            var attrHolderOpt = attrRegistry.getHolder(attrKey);
            if (attrHolderOpt.isEmpty()) {
                return;
            }
            Holder<Attribute> attrHolder = attrHolderOpt.get();

            AttributeInstance instance = ctx.player().getAttributes().getInstance(attrHolder);
            if (instance != null) {
                instance.removeModifier(ctx.modifierId());
            }
        }
    }

    record AbilityFlag(
            int unlockLevel,
            ResourceLocation flagId
            ) implements SkillPassive {

        public static final MapCodec<AbilityFlag> MAP_CODEC
                = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("unlock_level")
                        .forGetter(AbilityFlag::unlockLevel),
                ResourceLocation.CODEC.fieldOf("flag")
                        .forGetter(AbilityFlag::flagId)
        ).apply(instance, AbilityFlag::new));

        public static final Codec<AbilityFlag> CODEC = MAP_CODEC.codec();

        @Override
        public String type() {
            return "ability_flag";
        }

        @Override
        public void apply(Context ctx, int currentLevel) {
            if (currentLevel >= unlockLevel) {
                ctx.tracker().grantAbilityFlag(flagId);
            }
        }

        @Override
        public void revoke(Context ctx) {
            // Flags are cleared in bulk before recompute, so nothing to do here.
            // Individual revocation would require reference counting.
        }
    }
}
