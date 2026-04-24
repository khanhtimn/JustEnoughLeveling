package dev.khanhtimn.jel.api.perk;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import net.minecraft.resources.ResourceLocation;

/**
 * Registry of perk types for codec dispatch.
 * <p>
 * Each perk type maps a {@link ResourceLocation} to a {@link MapCodec} for
 * serialization. JEL ships with built-in types; third-party mods can register
 * additional types via {@link #register}.
 * <p>
 *
 * <h2>Extension example</h2>
 * <pre>{@code
 * public static final PerkType<MyPerk> MY_PERK =
 *     PerkType.register(
 *         ResourceLocation.fromNamespaceAndPath("mymod", "custom"),
 *         MyPerk.CODEC
 *     );
 * }</pre>
 */
public record PerkType<T extends Perk>(ResourceLocation id, MapCodec<T> codec) {

	private static final Object2ReferenceLinkedOpenHashMap<ResourceLocation, PerkType<?>> BY_ID
			= new Object2ReferenceLinkedOpenHashMap<>();

	private static final Object2ObjectLinkedOpenHashMap<MapCodec<?>, ResourceLocation> BY_CODEC
			= new Object2ObjectLinkedOpenHashMap<>();

	public static final PerkType<TagPerk> TAG
			= register("jel", "tag", TagPerk.CODEC);

	public static final PerkType<TraitPerk> TRAIT
			= register("jel", "trait", TraitPerk.CODEC);

	public static final PerkType<FunctionPerk> FUNCTION
			= register("jel", "function", FunctionPerk.CODEC);

	public static final PerkType<EffectPerk> EFFECT
			= register("jel", "effect", EffectPerk.CODEC);

	public static final PerkType<CommandPerk> COMMAND
			= register("jel", "command", CommandPerk.CODEC);

	public static final PerkType<LootTablePerk> LOOT_TABLE
			= register("jel", "loot_table", LootTablePerk.CODEC);

	private static <T extends Perk> PerkType<T> register(String namespace, String path, MapCodec<T> codec) {
		return register(ResourceLocation.fromNamespaceAndPath(namespace, path), codec);
	}

	/**
	 * Register a perk type. Third-party mods call this during mod
	 * initialization to add custom perk types.
	 *
	 * @param id    unique type identifier (e.g. "mymod:custom_perk")
	 * @param codec the MapCodec for serialization
	 * @return the registered PerkType for static field assignment
	 * @throws IllegalStateException if a type with this ID is already
	 *                               registered
	 */
	public static <T extends Perk> PerkType<T> register(ResourceLocation id, MapCodec<T> codec) {
		if (BY_ID.containsKey(id)) {
			throw new IllegalStateException("Duplicate perk type: " + id);
		}
		PerkType<T> type = new PerkType<>(id, codec);
		BY_ID.put(id, type);
		BY_CODEC.put(codec, id);
		return type;
	}

	public static PerkType<?> get(ResourceLocation id) {
		return BY_ID.get(id);
	}

	public static Map<ResourceLocation, PerkType<?>> allTypes() {
		return Collections.unmodifiableMap(BY_ID);
	}

	static ResourceLocation getId(MapCodec<?> codec) {
		return BY_CODEC.get(codec);
	}

	static final Codec<Perk> DISPATCH_CODEC = new MapCodec<Perk>() {

		@Override
		public <T> DataResult<Perk> decode(DynamicOps<T> ops, MapLike<T> input) {
			T typeElem = input.get("type");
			if (typeElem == null) {
				return DataResult.error(() -> "Missing 'type' field for Perk");
			}
			return Codec.STRING.parse(ops, typeElem).flatMap(typeStr -> {
				ResourceLocation typeId = ResourceLocation.parse(typeStr);
				PerkType<?> type = BY_ID.get(typeId);
				if (type == null) {
					return DataResult.error(() -> "Unknown perk type: " + typeStr);
				}
				return type.codec().decode(ops, input).map(p -> p);
			});
		}

		@Override
		public <T> RecordBuilder<T> encode(Perk perk, DynamicOps<T> ops, RecordBuilder<T> builder) {
			@SuppressWarnings("unchecked")
			PerkType<Perk> type = (PerkType<Perk>) perk.type();
			ResourceLocation id = type.id();
			builder.add("type", ops.createString(id.toString()));
			return type.codec().encode(perk, ops, builder);
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("type"));
		}

		@Override
		public String toString() {
			return "Perk.DISPATCH_CODEC";
		}
	}.codec();
}
