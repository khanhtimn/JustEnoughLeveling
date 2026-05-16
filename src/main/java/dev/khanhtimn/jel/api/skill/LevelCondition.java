package dev.khanhtimn.jel.api.skill;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.loot.JelLootItemConditions;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

/**
 * Checks whether the context entity has a JEL skill at a specified level range.
 * Enables cross-skill requirements in datapacks.
 *
 * <h2>JSON</h2>
 * <pre>{@code
 * { "condition": "jel:skill_level_check",
 *   "skill": "jel:mining",
 *   "level": { "min": 3 } }
 * }</pre>
 */
public record LevelCondition(
		ResourceLocation skill,
		MinMaxBounds.Ints level
) implements LootItemCondition {

	public static final MapCodec<LevelCondition> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("skill")
							.forGetter(LevelCondition::skill),
					MinMaxBounds.Ints.CODEC.fieldOf("level")
							.forGetter(LevelCondition::level)
			).apply(instance, LevelCondition::new)
	);

	public static LevelCondition atLeast(ResourceLocation skill, int min) {
		return new LevelCondition(skill, MinMaxBounds.Ints.atLeast(min));
	}

	public static LevelCondition atLeast(ResourceKey<SkillDefinition> skill, int min) {
		return atLeast(skill.location(), min);
	}

	public static LevelCondition atMost(ResourceLocation skill, int max) {
		return new LevelCondition(skill, MinMaxBounds.Ints.atMost(max));
	}

	public static LevelCondition between(ResourceLocation skill, int min, int max) {
		return new LevelCondition(skill, MinMaxBounds.Ints.between(min, max));
	}

	public static LevelCondition exactly(ResourceLocation skill, int level) {
		return new LevelCondition(skill, MinMaxBounds.Ints.exactly(level));
	}

	@Override
	public boolean test(LootContext ctx) {
		Entity entity = ctx.getParam(LootContextParams.THIS_ENTITY);
		if (!(entity instanceof ServerPlayer player)) return false;
		ResourceKey<SkillDefinition> key = ResourceKey.create(
				JelRegistries.SKILL_REGISTRY_KEY, skill);
		return level.matches(JelSkills.getLevel(player, key));
	}

	@NotNull
	@Override
	public LootItemConditionType getType() {
		return JelLootItemConditions.SKILL_LEVEL_CHECK;
	}
}
