package dev.khanhtimn.jel.api.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

/**
 * A gate that must be satisfied before a skill can level up or a perk can
 * unlock. Evaluated once at the decision point.
 * <p>
 * The optional {@code description} is shown in the client UI as a hint
 * (e.g. "Kill 100 zombies"). The {@code condition} is evaluated server-side.
 *
 * <h2>JSON</h2>
 * <pre>{@code
 * // Full form
 * { "description": "Kill the Ender Dragon",
 *   "condition": { "condition": "minecraft:entity_properties", ... } }
 *
 * // Inline form (bare condition, no description)
 * { "condition": "minecraft:entity_properties", ... }
 * }</pre>
 */
public record Requirement(
		Optional<Component> description,
		LootItemCondition condition
) {

	public static final Codec<Requirement> FULL_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					ComponentSerialization.CODEC.optionalFieldOf("description")
							.forGetter(Requirement::description),
					LootItemCondition.DIRECT_CODEC.fieldOf("condition")
							.forGetter(Requirement::condition)
			).apply(instance, Requirement::new)
	);

	public static final Codec<Requirement> CODEC = Codec.withAlternative(
			FULL_CODEC,
			LootItemCondition.DIRECT_CODEC.xmap(
					c -> new Requirement(Optional.empty(), c),
					Requirement::condition
			)
	);

	public boolean test(LootContext ctx) {
		return condition.test(ctx);
	}

	public static Requirement of(LootItemCondition condition) {
		return new Requirement(Optional.empty(), condition);
	}

	public static Requirement of(Component description, LootItemCondition condition) {
		return new Requirement(Optional.of(description), condition);
	}

	public static Requirement skillLevel(ResourceLocation skill, int minLevel) {
		return of(LevelCondition.atLeast(skill, minLevel));
	}

	public static Requirement skillLevel(Component description, ResourceLocation skill, int minLevel) {
		return of(description, LevelCondition.atLeast(skill, minLevel));
	}

	public static Requirement skillLevel(ResourceKey<SkillDefinition> skill, int minLevel) {
		return of(LevelCondition.atLeast(skill, minLevel));
	}

	public static Requirement skillLevel(Component description, ResourceKey<SkillDefinition> skill, int minLevel) {
		return of(description, LevelCondition.atLeast(skill, minLevel));
	}
}
