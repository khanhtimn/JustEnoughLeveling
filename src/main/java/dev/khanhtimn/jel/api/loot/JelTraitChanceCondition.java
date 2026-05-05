package dev.khanhtimn.jel.api.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.trait.TraitKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

/**
 * Evaluates at loot-generation time: reads the trait value for the context player
 * and rolls RNG against it. Supports both branch-gated and non-branched traits.
 * The {@code trait} field is the base trait ResourceLocation (e.g. {@code jel:farming/seed_savant}).
 * The {@code perk} field is the parameter name (e.g. {@code chance}).
 * Together they form the flattened TraitKey {@code jel:farming/seed_savant.chance}.
 */
public record JelTraitChanceCondition(
		ResourceLocation trait,
		String perk,
		ResourceLocation skill,
		ResourceLocation branch
) implements LootItemCondition {

	private static final ResourceLocation EMPTY_RL = ResourceLocation.withDefaultNamespace("");

	public static final MapCodec<JelTraitChanceCondition> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("trait")
							.forGetter(JelTraitChanceCondition::trait),
					com.mojang.serialization.Codec.STRING.fieldOf("perk")
							.forGetter(JelTraitChanceCondition::perk),
					ResourceLocation.CODEC.optionalFieldOf("skill", EMPTY_RL)
							.forGetter(JelTraitChanceCondition::skill),
					ResourceLocation.CODEC.optionalFieldOf("branch", EMPTY_RL)
							.forGetter(JelTraitChanceCondition::branch)
			).apply(instance, JelTraitChanceCondition::new)
	);

	public static JelTraitChanceCondition ofTrait(TraitKey key) {
		ResourceLocation baseId = extractBaseId(key);
		String param = key.paramName();
		return new JelTraitChanceCondition(baseId, param != null ? param : "active", EMPTY_RL, EMPTY_RL);
	}

	public static JelTraitChanceCondition ofBranch(
			ResourceKey<SkillDefinition> skill, ResourceLocation branch, TraitKey key) {
		ResourceLocation baseId = extractBaseId(key);
		String param = key.paramName();
		return new JelTraitChanceCondition(baseId, param != null ? param : "active",
				skill.location(), branch);
	}

	@Override
	public boolean test(LootContext ctx) {
		Entity entity = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
		if (!(entity instanceof ServerPlayer player)) return false;

		float value;
		TraitKey key = TraitKey.of(trait, perk);

		if (hasBranch()) {
			ResourceKey<SkillDefinition> skillKey = ResourceKey.create(
					JelRegistries.SKILL_REGISTRY_KEY, skill);
			value = JelTraits.branchValue(player, skillKey, branch, key);
		} else {
			value = JelTraits.value(player, key);
		}

		return value > 0 && ctx.getRandom().nextFloat() < value;
	}

	private boolean hasBranch() {
		return !branch.getPath().isEmpty();
	}

	/**
	 * Extracts the base trait ID from a flattened TraitKey.
	 * E.g. {@code jel:farming/seed_savant.chance} → {@code jel:farming/seed_savant}
	 */
	private static ResourceLocation extractBaseId(TraitKey key) {
		ResourceLocation id = key.id();
		String path = id.getPath();
		int dot = path.lastIndexOf('.');
		if (dot >= 0) {
			return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), path.substring(0, dot));
		}
		return id;
	}

	@NotNull
	@Override
	public LootItemConditionType getType() {
		return JelLootItemConditions.TRAIT_CHANCE;
	}
}
