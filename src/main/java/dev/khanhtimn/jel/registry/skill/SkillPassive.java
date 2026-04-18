package dev.khanhtimn.jel.registry.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.stream.Stream;

public sealed interface SkillPassive
		permits SkillPassive.AttributeBonus, SkillPassive.AbilityFlag {

	MapCodec<SkillPassive> MAP_CODEC = new MapCodec<>() {
		@Override
		public <T> DataResult<SkillPassive> decode(DynamicOps<T> ops, MapLike<T> input) {
			T typeElem = input.get("type");
			if (typeElem == null) {
				return DataResult.error(() -> "Missing 'type' field for SkillPassive");
			}
			return Codec.STRING.parse(ops, typeElem).flatMap(type -> switch (type) {
				case "attribute" -> AttributeBonus.MAP_CODEC.decode(ops, input).map(v -> v);
				case "ability_flag" -> AbilityFlag.MAP_CODEC.decode(ops, input).map(v -> v);
				default -> DataResult.error(() -> "Unknown SkillPassive type: " + type);
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
			return builder.withErrorsFrom(DataResult.error(() -> "Unsupported SkillPassive implementation: " + input.getClass().getName()));
		}

		@Override
		public String toString() {
			return "SkillPassive.MAP_CODEC";
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			// We at least advertise the discriminant key
			return Stream.of(ops.createString("type"));
		}
	};

	Codec<SkillPassive> CODEC = MAP_CODEC.codec();

	String type();

	record AttributeBonus(
			int unlockLevel,
			ResourceLocation attribute,
			double amountPerLevel,
			AttributeModifier.Operation operation
	) implements SkillPassive {
		public static final MapCodec<AttributeBonus> MAP_CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance.group(
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
	}

	record AbilityFlag(
			int unlockLevel,
			ResourceLocation flagId
	) implements SkillPassive {
		public static final MapCodec<AbilityFlag> MAP_CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance.group(
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
	}
}
