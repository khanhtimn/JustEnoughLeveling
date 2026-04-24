package dev.khanhtimn.jel.api.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

/**
 * A single attribute effect within a skill definition.
 * <p>
 * Unifies base value overrides and modifier operations into one schema.
 * The {@link Operation} field determines behavior:
 * <ul>
 *   <li>{@code base} — sets the attribute's base value via {@code setBaseValue()}.
 *       Always active from level 0; {@code unlock_level} is ignored.</li>
 *   <li>{@code add_value} — adds a flat amount via {@link AttributeModifier}</li>
 *   <li>{@code add_multiplied_base} — adds {@code base × amount}</li>
 *   <li>{@code add_multiplied_total} — multiplies the final computed value</li>
 * </ul>
 *
 * <h2>JSON examples</h2>
 * <pre>{@code
 * // Set base health per level (always active from level 0)
 * { "attribute": "minecraft:generic.max_health", "operation": "base",
 *   "value": { "type": "minecraft:lookup", "values": [6, 8, 10, 12, 14, 16, 17, 18, 19, 20], "fallback": 20 } }
 *
 * // Add flat damage, unlocks at skill level 1
 * { "attribute": "minecraft:generic.attack_damage", "operation": "add_value",
 *   "value": { "type": "minecraft:linear", "base": 0.5, "per_level_above_first": 0.5 },
 *   "unlock_level": 1 }
 *
 * // Multiply break speed, unlocks at skill level 1
 * { "attribute": "minecraft:player.block_break_speed", "operation": "add_multiplied_base",
 *   "value": 0.1, "unlock_level": 1 }
 * }</pre>
 */
public record AttributeEffect(
		ResourceLocation attribute,
		Operation operation,
		LevelBasedValue value,
		int unlockLevel
) {

	public static final Codec<AttributeEffect> CODEC =
			RecordCodecBuilder.create(instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("attribute")
							.forGetter(AttributeEffect::attribute),
					Operation.CODEC.fieldOf("operation")
							.forGetter(AttributeEffect::operation),
					LevelBasedValue.CODEC.fieldOf("value")
							.forGetter(AttributeEffect::value),
					Codec.INT.optionalFieldOf("unlock_level", 0)
							.forGetter(AttributeEffect::unlockLevel)
			).apply(instance, AttributeEffect::new));

	public boolean isBaseOverride() {
		return operation == Operation.BASE;
	}

	public AttributeModifier.Operation toModifierOperation() {
		return switch (operation) {
			case ADD_VALUE -> AttributeModifier.Operation.ADD_VALUE;
			case ADD_MULTIPLIED_BASE -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
			case ADD_MULTIPLIED_TOTAL -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
			case BASE -> throw new IllegalStateException("BASE operation has no modifier equivalent");
		};
	}


	public static AttributeEffect base(String attribute, LevelBasedValue value) {
		return new AttributeEffect(ResourceLocation.parse(attribute), Operation.BASE, value, 0);
	}

	public static AttributeEffect base(ResourceLocation attribute, LevelBasedValue value) {
		return new AttributeEffect(attribute, Operation.BASE, value, 0);
	}

	public static AttributeEffect base(ResourceKey<Attribute> attribute, LevelBasedValue value) {
		return new AttributeEffect(attribute.location(), Operation.BASE, value, 0);
	}

	public static AttributeEffect base(Holder<Attribute> attribute, LevelBasedValue value) {
		return new AttributeEffect(attribute.unwrapKey().orElseThrow().location(), Operation.BASE, value, 0);
	}


	public static AttributeEffect modifier(
			String attribute,
			AttributeModifier.Operation operation,
			LevelBasedValue value,
			int unlockLevel
	) {
		return new AttributeEffect(
				ResourceLocation.parse(attribute), fromVanilla(operation), value, unlockLevel);
	}

	public static AttributeEffect modifier(
			ResourceLocation attribute,
			AttributeModifier.Operation operation,
			LevelBasedValue value,
			int unlockLevel
	) {
		return new AttributeEffect(attribute, fromVanilla(operation), value, unlockLevel);
	}

	public static AttributeEffect modifier(
			ResourceKey<Attribute> attribute,
			AttributeModifier.Operation operation,
			LevelBasedValue value,
			int unlockLevel
	) {
		return new AttributeEffect(
				attribute.location(), fromVanilla(operation), value, unlockLevel);
	}

	public static AttributeEffect modifier(
			Holder<Attribute> attribute,
			AttributeModifier.Operation operation,
			LevelBasedValue value,
			int unlockLevel
	) {
		return new AttributeEffect(
				attribute.unwrapKey().orElseThrow().location(), fromVanilla(operation), value, unlockLevel);
	}


	private static Operation fromVanilla(AttributeModifier.Operation op) {
		return switch (op) {
			case ADD_VALUE -> Operation.ADD_VALUE;
			case ADD_MULTIPLIED_BASE -> Operation.ADD_MULTIPLIED_BASE;
			case ADD_MULTIPLIED_TOTAL -> Operation.ADD_MULTIPLIED_TOTAL;
		};
	}


	public enum Operation implements StringRepresentable {
		BASE("base"),
		ADD_VALUE("add_value"),
		ADD_MULTIPLIED_BASE("add_multiplied_base"),
		ADD_MULTIPLIED_TOTAL("add_multiplied_total");

		public static final Codec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);

		private final String serializedName;

		Operation(String serializedName) {
			this.serializedName = serializedName;
		}

		@Override
		public @NotNull String getSerializedName() {
			return serializedName;
		}
	}
}
