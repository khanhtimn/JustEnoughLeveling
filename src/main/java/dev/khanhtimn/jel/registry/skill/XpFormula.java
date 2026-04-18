package dev.khanhtimn.jel.registry.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.*;
import net.minecraft.util.Mth;

import java.util.stream.Stream;

public sealed interface XpFormula
		permits XpFormula.Vanilla, XpFormula.Constant,
		XpFormula.Linear, XpFormula.Exponential {

	// Polymorphic codec + "type" discriminator
	MapCodec<XpFormula> MAP_CODEC = new MapCodec<>() {
		@Override
		public <T> DataResult<XpFormula> decode(
				DynamicOps<T> ops,
				MapLike<T> input
		) {
			T typeElem = input.get("type");
			if (typeElem == null) {
				// Default: vanilla-shaped skill XP curve
				return DataResult.success(Vanilla.INSTANCE);
			}
			return Codec.STRING.parse(ops, typeElem).flatMap(type -> switch (type) {
				case "vanilla" -> Vanilla.MAP_CODEC.decode(ops, input).map(v -> v);
				case "constant" -> Constant.MAP_CODEC.decode(ops, input).map(v -> v);
				case "linear" -> Linear.MAP_CODEC.decode(ops, input).map(v -> v);
				case "exponential" -> Exponential.MAP_CODEC.decode(ops, input).map(v -> v);
				default -> DataResult.error(() -> "Unknown XpFormula type: " + type);
			});
		}

		@Override
		public <T> RecordBuilder<T> encode(
				XpFormula input,
				DynamicOps<T> ops,
				RecordBuilder<T> builder
		) {
			builder.add("type", ops.createString(input.type()));
			return switch (input) {
				case Vanilla vanilla -> Vanilla.MAP_CODEC.encode(vanilla, ops, builder);
				case Constant constant -> Constant.MAP_CODEC.encode(constant, ops, builder);
				case Linear linear -> Linear.MAP_CODEC.encode(linear, ops, builder);
				case Exponential exponential -> Exponential.MAP_CODEC.encode(exponential, ops, builder);
			};
		}

		@Override
		public String toString() {
			return "XpFormula.MAP_CODEC";
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("type"));
		}
	};

	Codec<XpFormula> CODEC = MAP_CODEC.codec();

	String type();

	/**
	 * Skill XP required to go from skill level (level - 1) -> level.
	 */
	int costForLevel(int level);

	default int totalCostToLevel(int targetLevel) {
		if (targetLevel <= 0) return 0;
		long total = 0L;
		for (int lvl = 1; lvl <= targetLevel; lvl++) {
			total += costForLevel(lvl);
			if (total > Integer.MAX_VALUE) {
				return Integer.MAX_VALUE;
			}
		}
		return (int) total;
	}

	record Vanilla() implements XpFormula {
		public static final Vanilla INSTANCE = new Vanilla();
		public static final MapCodec<Vanilla> MAP_CODEC =
				MapCodec.unit(INSTANCE);

		@Override
		public String type() {
			return "vanilla";
		}

		@Override
		public int costForLevel(int level) {
			if (level <= 0) return 0;
			// Use vanilla player XP cost for (level - 1) -> level
			return vanillaXpNeededForLevel(level - 1);
		}
	}

	record Constant(int xpPerLevel) implements XpFormula {
		public static final MapCodec<Constant> MAP_CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance.group(
						Codec.INT.fieldOf("xp_per_level")
								.forGetter(Constant::xpPerLevel)
				).apply(instance, Constant::new));

		public Constant {
			xpPerLevel = Math.max(0, xpPerLevel);
		}

		@Override
		public String type() {
			return "constant";
		}

		@Override
		public int costForLevel(int level) {
			return level <= 0 ? 0 : xpPerLevel;
		}
	}

	record Linear(int base, int increment) implements XpFormula {
		public static final MapCodec<Linear> MAP_CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance.group(
						Codec.INT.fieldOf("base")
								.forGetter(Linear::base),
						Codec.INT.fieldOf("increment")
								.forGetter(Linear::increment)
				).apply(instance, Linear::new));

		@Override
		public String type() {
			return "linear";
		}

		@Override
		public int costForLevel(int level) {
			if (level <= 0) return 0;
			long cost = (long) base + (long) (level - 1) * increment;
			return (int) Mth.clamp(cost, 0, Integer.MAX_VALUE);
		}
	}

	record Exponential(int base, double multiplier) implements XpFormula {
		public static final MapCodec<Exponential> MAP_CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance.group(
						Codec.INT.fieldOf("base")
								.forGetter(Exponential::base),
						Codec.DOUBLE.fieldOf("multiplier")
								.forGetter(Exponential::multiplier)
				).apply(instance, Exponential::new));

		public Exponential {
			base = Math.max(0, base);
			if (multiplier <= 0.0) multiplier = 1.0;
		}

		@Override
		public String type() {
			return "exponential";
		}

		@Override
		public int costForLevel(int level) {
			if (level <= 0) return 0;
			double raw = base * Math.pow(multiplier, level - 1);
			int value = (int) Math.round(raw);
			return Math.max(0, value);
		}
	}

	private static int vanillaXpNeededForLevel(int level) {
		if (level >= 30) {
			return 112 + (level - 30) * 9;
		} else if (level >= 15) {
			return 37 + (level - 15) * 5;
		} else {
			return 7 + level * 2;
		}
	}
}
