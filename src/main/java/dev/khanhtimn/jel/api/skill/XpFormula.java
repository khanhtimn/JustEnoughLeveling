package dev.khanhtimn.jel.api.skill;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.khanhtimn.jel.common.VanillaXpHelper;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * Data-driven XP cost formula for leveling up skills.
 * <p>
 * Three variants:
 * <ul>
 *   <li>{@link Vanilla} — Minecraft's piecewise XP curve</li>
 *   <li>{@link Exponential} — {@code base × multiplier^(level-1)}</li>
 *   <li>{@link LevelBased} — delegates to any {@link LevelBasedValue}
 *       (constant, linear, lookup, clamped, fraction, levels_squared)</li>
 * </ul>
 *
 * <h2>Factory methods</h2>
 * <pre>{@code
 * XpFormula.vanilla()                                    // MC curve
 * XpFormula.exponential(100, 1.5)                        // base × 1.5^(level-1)
 * XpFormula.of(LevelBasedValue.constant(200))            // 200 per level
 * XpFormula.of(LevelBasedValue.perLevel(100, 50))        // 100, 150, 200, ...
 * XpFormula.of(LevelBasedValue.lookup(values, fallback)) // explicit per-level
 * }</pre>
 *
 * <h2>JSON examples</h2>
 * <pre>{@code
 * // Omitted → defaults to vanilla
 * // "xp": "vanilla"
 * // "xp": 200                          → constant
 * // "xp": {"type": "linear", ...}      → LBV linear
 * // "xp": {"type": "exponential", ...} → Exponential
 * // "xp": {"type": "lookup", ...}      → LBV lookup
 * }</pre>
 */
public sealed interface XpFormula
		permits XpFormula.Vanilla, XpFormula.Exponential, XpFormula.LevelBased {


	static XpFormula vanilla() {
		return Vanilla.INSTANCE;
	}

	static XpFormula exponential(int base, double multiplier) {
		return new Exponential(base, multiplier);
	}

	static XpFormula of(LevelBasedValue cost) {
		return new LevelBased(cost);
	}


	/**
	 * Codec handling all JSON forms:
	 * <ul>
	 *   <li>{@code "vanilla"} (string) → {@link Vanilla}</li>
	 *   <li>{@code 200} (bare number) → {@link LevelBased}({@link LevelBasedValue.Constant})</li>
	 *   <li>{@code {"type": "exponential", ...}} → {@link Exponential}</li>
	 *   <li>{@code {"type": "linear"|"lookup"|...}} → {@link LevelBased} via LBV dispatch</li>
	 *   <li>{@code {}} (empty object, no type) → {@link Vanilla}</li>
	 * </ul>
	 */
	Codec<XpFormula> CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<XpFormula, T>> decode(DynamicOps<T> ops, T input) {
			// 1. Try as string ("vanilla")
			var strResult = Codec.STRING.decode(ops, input);
			if (strResult.isSuccess()) {
				return strResult.flatMap(pair ->
						"vanilla".equals(pair.getFirst())
								? DataResult.success(pair.mapFirst(s -> Vanilla.INSTANCE))
								: DataResult.error(() -> "Unknown XpFormula string: " + pair.getFirst())
				);
			}

			// 2. Try as bare number → LevelBased(Constant)
			var numResult = Codec.FLOAT.decode(ops, input);
			if (numResult.isSuccess()) {
				return numResult.map(pair ->
						pair.mapFirst(f -> new LevelBased(LevelBasedValue.constant(f)))
				);
			}

			// 3. Object: inspect "type" field for routing
			return ops.getMap(input).flatMap(map -> {
				T typeElem = map.get("type");
				if (typeElem == null) {
					// No type field → vanilla
					return DataResult.success(Pair.of(Vanilla.INSTANCE, input));
				}
				return Codec.STRING.parse(ops, typeElem).flatMap(type -> switch (type) {
					case "vanilla" -> DataResult.success(Pair.of(Vanilla.INSTANCE, input));
					case "exponential" -> Exponential.MAP_CODEC.decode(ops, map)
							.map(e -> Pair.of(e, input));
					default ->
						// Fall through to vanilla LBV dispatch (linear, lookup, clamped, etc.)
						// Use the raw input (not MapLike) since DISPATCH_CODEC is a Codec, not MapDecoder
							LevelBasedValue.CODEC.decode(ops, input)
									.map(pair -> pair.mapFirst(LevelBased::new));
				});
			});
		}

		@Override
		public <T> DataResult<T> encode(XpFormula input, DynamicOps<T> ops, T prefix) {
			return switch (input) {
				case Vanilla v -> Codec.STRING.encode("vanilla", ops, prefix);
				case Exponential e -> Exponential.CODEC.encode(e, ops, prefix);
				case LevelBased lb -> LevelBasedValue.CODEC.encode(lb.cost(), ops, prefix);
			};
		}
	};


	int costForLevel(int level);

	default int totalCostToLevel(int targetLevel) {
		if (targetLevel <= 0) {
			return 0;
		}
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

		@Override
		public int costForLevel(int level) {
			if (level <= 0) {
				return 0;
			}
			return VanillaXpHelper.xpNeededForLevel(level - 1);
		}
	}

	record Exponential(int base, double multiplier) implements XpFormula {

		public static final MapCodec<Exponential> MAP_CODEC
				= RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.INT.fieldOf("base")
						.forGetter(Exponential::base),
				Codec.DOUBLE.fieldOf("multiplier")
						.forGetter(Exponential::multiplier)
		).apply(instance, Exponential::new));

		public static final Codec<Exponential> CODEC = MAP_CODEC.codec();

		public Exponential {
			base = Math.max(0, base);
			if (multiplier <= 0.0) {
				multiplier = 1.0;
			}
		}

		@Override
		public int costForLevel(int level) {
			if (level <= 0) {
				return 0;
			}
			double raw = base * Math.pow(multiplier, level - 1);
			int value = (int) Math.round(raw);
			return Math.max(0, value);
		}
	}

	record LevelBased(LevelBasedValue cost) implements XpFormula {

		@Override
		public int costForLevel(int level) {
			if (level <= 0) {
				return 0;
			}
			return Math.max(0, Math.round(cost.calculate(level)));
		}
	}
}
