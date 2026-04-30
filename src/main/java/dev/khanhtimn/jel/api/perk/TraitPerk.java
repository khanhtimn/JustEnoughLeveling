package dev.khanhtimn.jel.api.perk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import dev.khanhtimn.jel.api.skill.Requirement;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class TraitPerk implements Perk {

	private final int unlockLevel;
	private final ResourceLocation trait;
	@Nullable
	private final Map<String, ParamEntry> parameters;
	@Nullable
	private final Requirement requirement;

	/**
	 * Pre-computed at construction. Null for flag traits.
	 * Array for cache-friendly iteration in the hot path.
	 */
	private final FlatParam[] flatParams;

	private record FlatParam(
			ResourceLocation key,
			LevelBasedValue formula,
			int unlockLevel,
			@Nullable LootItemCondition condition
	) {
	}

	/**
	 * Serializable per-parameter entry used by the codec.
	 */
	public record ParamEntry(
			LevelBasedValue formula,
			int unlockLevel,
			@Nullable LootItemCondition condition
	) {
		public static final Codec<ParamEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				LevelBasedValue.CODEC.fieldOf("formula")
						.forGetter(ParamEntry::formula),
				Codec.INT.fieldOf("unlock_level")
						.forGetter(ParamEntry::unlockLevel),
				LootItemCondition.DIRECT_CODEC.optionalFieldOf("condition")
						.forGetter(entry -> Optional.ofNullable(entry.condition()))
		).apply(instance, (formula, unlockLevel, condition) -> new ParamEntry(formula, unlockLevel, condition.orElse(null))));
	}

	/**
	 * Codec constructor — used for JSON deserialization.
	 */
	public TraitPerk(int unlockLevel, ResourceLocation trait,
	                 @Nullable Map<String, ParamEntry> parameters,
	                 @Nullable Requirement requirement) {
		this.unlockLevel = unlockLevel;
		this.trait = trait;
		this.parameters = parameters;
		this.requirement = requirement;

		if (parameters != null && !parameters.isEmpty()) {
			String ns = trait.getNamespace();
			String basePath = trait.getPath();
			FlatParam[] flat = new FlatParam[parameters.size()];
			int i = 0;
			for (var entry : parameters.entrySet()) {
				ParamEntry pe = entry.getValue();
				flat[i++] = new FlatParam(
						ResourceLocation.fromNamespaceAndPath(ns, basePath + "." + entry.getKey()),
						pe.formula(),
						pe.unlockLevel(),
						pe.condition()
				);
			}
			this.flatParams = flat;
		} else {
			this.flatParams = null;
		}
	}

	/**
	 * Per-param constructor — used by the Java API via {@link #ofParams}.
	 */
	TraitPerk(ResourceLocation trait, FlatParam[] flatParams,
	          @Nullable Map<String, ParamEntry> codecParams, int derivedUnlockLevel,
	          @Nullable Requirement requirement) {
		this.unlockLevel = derivedUnlockLevel;
		this.trait = trait;
		this.parameters = codecParams;
		this.flatParams = flatParams;
		this.requirement = requirement;
	}

	static TraitPerk ofParams(ResourceLocation trait, TraitParam[] params) {
		FlatParam[] flat = new FlatParam[params.length];
		Map<String, ParamEntry> codecParams = new LinkedHashMap<>(params.length);
		int minUnlock = Integer.MAX_VALUE;
		String ns = trait.getNamespace();
		String basePath = trait.getPath();

		for (int i = 0; i < params.length; i++) {
			TraitParam p = params[i];
			String paramName = p.key().paramName();
			if (paramName == null) {
				throw new IllegalArgumentException(
						"TraitKey " + p.key() + " has no parameter name — cannot be used in a parameterized trait");
			}
			flat[i] = new FlatParam(
					ResourceLocation.fromNamespaceAndPath(ns, basePath + "." + paramName),
					p.formula(),
					p.unlockLevel(),
					p.condition()
			);
			codecParams.put(paramName, new ParamEntry(p.formula(), p.unlockLevel(), p.condition()));
			minUnlock = Math.min(minUnlock, p.unlockLevel());
		}

		return new TraitPerk(trait, flat, codecParams, minUnlock, null);
	}

	public static final MapCodec<TraitPerk> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.optionalFieldOf("unlock_level", 1)
					.forGetter(TraitPerk::unlockLevel),
			ResourceLocation.CODEC.fieldOf("trait")
					.forGetter(TraitPerk::trait),
			Codec.unboundedMap(Codec.STRING, ParamEntry.CODEC).optionalFieldOf("parameters")
					.forGetter(TraitPerk::parameters),
			Requirement.CODEC.optionalFieldOf("requirement")
					.forGetter(TraitPerk::requirement)
	).apply(instance, (unlockLevel, trait, parameters, requirement) ->
			new TraitPerk(unlockLevel, trait, parameters.orElse(null), requirement.orElse(null))));

	@Override
	public int unlockLevel() {
		return unlockLevel;
	}

	public ResourceLocation trait() {
		return trait;
	}

	public Optional<Map<String, ParamEntry>> parameters() {
		return Optional.ofNullable(parameters);
	}

	@Override
	public Optional<Requirement> requirement() {
		return Optional.ofNullable(requirement);
	}

	@Override
	public PerkType<?> type() {
		return PerkType.TRAIT;
	}

	@Override
	public void apply(PerkContext ctx, int currentLevel) {
		if (flatParams != null) {
			for (FlatParam p : flatParams) {
				if (currentLevel < p.unlockLevel) continue;

				if (p.condition != null && !p.condition.test(ctx.lootContext())) continue;

				int effective = currentLevel - p.unlockLevel + 1;
				ctx.tracker().setTraitValue(p.key, p.formula.calculate(effective));
			}
		} else {
			ctx.tracker().setTraitValue(trait, 1.0f);
		}
	}

	@Override
	public void revoke(PerkContext ctx) {
		// Intentional no-op. Traits are bulk-cleared by SkillEffectApplier.
	}
}
