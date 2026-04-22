package dev.khanhtimn.jel.common.skill.impl;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.khanhtimn.jel.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * A passive effect granted by a skill at a certain level.
 * <p>
 * Each variant defines its own {@link #apply} and {@link #revoke} behavior,
 * making it trivial to add new passive types without modifying apply logic
 * elsewhere.
 * <p>
 * Value computation uses vanilla's {@link LevelBasedValue} interface, supporting
 * {@code constant}, {@code linear}, {@code lookup}, {@code clamped},
 * {@code fraction}, and {@code levels_squared} formulas out of the box.
 */
public sealed interface SkillPassive
		permits SkillPassive.AttributeBonus, SkillPassive.AbilityFlag, SkillPassive.AttributeBase {

	// ---- Factory methods ----

	/**
	 * Create an attribute modifier passive. Uses {@link LevelBasedValue} for
	 * flexible per-level computation.
	 *
	 * @param unlockLevel skill level at which this passive begins to apply
	 * @param attribute   the attribute to modify (e.g. "minecraft:generic.attack_damage")
	 * @param amount      level-based value computing the modifier amount
	 * @param operation   how the amount is applied (ADD_VALUE, ADD_MULTIPLIED_BASE, etc.)
	 */
	static SkillPassive attributeBonus(int unlockLevel, String attribute,
	                                   LevelBasedValue amount, AttributeModifier.Operation operation) {
		return new AttributeBonus(unlockLevel, ResourceLocation.parse(attribute), amount, operation);
	}

	/**
	 * Create an attribute base override passive. Always active from level 0.
	 * Sets the attribute's base value directly via {@code setBaseValue()}.
	 *
	 * @param attribute the attribute to control (e.g. "minecraft:generic.max_health")
	 * @param value     level-based value computing the new base
	 */
	static SkillPassive attributeBase(String attribute, LevelBasedValue value) {
		return new AttributeBase(ResourceLocation.parse(attribute), value);
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
				case "attribute_modifier" -> AttributeBonus.MAP_CODEC.decode(ops, input).map(v -> v);
				case "attribute_base" -> AttributeBase.MAP_CODEC.decode(ops, input).map(v -> v);
				case "flag" -> AbilityFlag.MAP_CODEC.decode(ops, input).map(v -> v);
				default -> DataResult.error(() -> "Unknown SkillPassive type: " + type);
			});
		}

		@Override
		public <T> RecordBuilder<T> encode(SkillPassive input, DynamicOps<T> ops, RecordBuilder<T> builder) {
			builder.add("type", ops.createString(input.type()));
			return switch (input) {
				case AttributeBonus attr -> AttributeBonus.MAP_CODEC.encode(attr, ops, builder);
				case AttributeBase base -> AttributeBase.MAP_CODEC.encode(base, ops, builder);
				case AbilityFlag flag -> AbilityFlag.MAP_CODEC.encode(flag, ops, builder);
			};
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
	 * For {@link AttributeBase}, this is always 0 (active from the start).
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
			PlayerSkillData tracker,
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

		/**
		 * Resolve an attribute holder from a ResourceLocation.
		 */
		public AttributeInstance resolveAttribute(ResourceLocation attribute) {
			Registry<Attribute> attrRegistry
					= registryAccess().registryOrThrow(Registries.ATTRIBUTE);
			ResourceKey<Attribute> attrKey
					= ResourceKey.create(Registries.ATTRIBUTE, attribute);
			var attrHolderOpt = attrRegistry.getHolder(attrKey);
			return attrHolderOpt.map(attributeReference -> player().getAttributes().getInstance(attributeReference)).orElse(null);
		}
	}

	// ---- Implementations ----

	/**
	 * Adds an {@link AttributeModifier} to the player, scaled by skill level.
	 * The modifier amount is computed via {@link LevelBasedValue}.
	 * <p>
	 * Note: skill levels are 0-based, but vanilla's {@link LevelBasedValue}
	 * is 1-based (enchantments start at level 1). We shift by +1 internally
	 * so that datapack semantics match vanilla enchantments.
	 */
	record AttributeBonus(
			int unlockLevel,
			ResourceLocation attribute,
			LevelBasedValue amount,
			AttributeModifier.Operation operation
	) implements SkillPassive {

		public static final MapCodec<AttributeBonus> MAP_CODEC
				= RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.INT.fieldOf("unlock_level")
						.forGetter(AttributeBonus::unlockLevel),
				ResourceLocation.CODEC.fieldOf("attribute")
						.forGetter(AttributeBonus::attribute),
				LevelBasedValue.CODEC.fieldOf("amount")
						.forGetter(AttributeBonus::amount),
				AttributeModifier.Operation.CODEC.fieldOf("operation")
						.forGetter(AttributeBonus::operation)
		).apply(instance, AttributeBonus::new));

		public static final Codec<AttributeBonus> CODEC = MAP_CODEC.codec();

		@Override
		public String type() {
			return "attribute_modifier";
		}

		@Override
		public void apply(Context ctx, int currentLevel) {
			if (currentLevel < unlockLevel) return;

			AttributeInstance instance = ctx.resolveAttribute(attribute);
			if (instance == null) return;

			// Effective levels above unlock, shifted +1 for vanilla LBV convention
			int effectiveLevel = currentLevel - unlockLevel + 1;
			double amt = amount.calculate(effectiveLevel);
			if (amt == 0.0) return;

			AttributeModifier modifier = new AttributeModifier(
					ctx.modifierId(), amt, operation
			);
			// Remove existing modifier first to ensure idempotency
			instance.removeModifier(ctx.modifierId());
			instance.addPermanentModifier(modifier);
		}

		@Override
		public void revoke(Context ctx) {
			AttributeInstance instance = ctx.resolveAttribute(attribute);
			if (instance != null) {
				instance.removeModifier(ctx.modifierId());
			}
		}
	}

	/**
	 * Sets an attribute's base value directly via {@code setBaseValue()},
	 * scaled by skill level. Always active from level 0.
	 * <p>
	 * Uses {@link LevelBasedValue} for the value computation. Skill level
	 * is shifted by +1 to match vanilla's 1-based convention.
	 * <p>
	 * The vanilla default base value is saved before overwriting (tracked
	 * in {@link PlayerSkillData}) and restored during revocation.
	 */
	record AttributeBase(
			ResourceLocation attribute,
			LevelBasedValue value
	) implements SkillPassive {

		public static final MapCodec<AttributeBase> MAP_CODEC
				= RecordCodecBuilder.mapCodec(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf("attribute")
						.forGetter(AttributeBase::attribute),
				LevelBasedValue.CODEC.fieldOf("value")
						.forGetter(AttributeBase::value)
		).apply(instance, AttributeBase::new));

		public static final Codec<AttributeBase> CODEC = MAP_CODEC.codec();

		@Override
		public String type() {
			return "attribute_base";
		}

		@Override
		public int unlockLevel() {
			return 0; // always active
		}

		@Override
		public void apply(Context ctx, int currentLevel) {
			AttributeInstance instance = ctx.resolveAttribute(attribute);
			if (instance == null) return;

			// Save vanilla default before overwriting
			ctx.tracker().saveOriginalBase(attribute, instance.getBaseValue());

			// Shift +1 for vanilla LBV convention (1-based)
			double computedBase = value.calculate(currentLevel + 1);
			instance.setBaseValue(computedBase);
		}

		@Override
		public void revoke(Context ctx) {
			// Restoration is handled by PassiveApplier.revokeAll()
			// which reads from tracker.getOriginalBases()
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
			return "flag";
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
