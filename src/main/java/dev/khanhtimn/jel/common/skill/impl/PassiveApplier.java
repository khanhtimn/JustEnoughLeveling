package dev.khanhtimn.jel.common.skill.impl;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.core.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies and revokes all passive effects from skill definitions
 * based on the player's current skill levels.
 * <p>
 * Uses a collect-then-apply pattern for deterministic conflict
 * resolution when multiple skills target the same attribute.
 */
public final class PassiveApplier {

	private PassiveApplier() {
	}

	/**
	 * Full recompute: revoke all passives, then re-apply based on current levels.
	 * <p>
	 * After recomputation, clamps the player's health if max_health was reduced.
	 */
	public static void recomputeAll(
			ServerPlayer player,
			PlayerSkillData tracker,
			RegistryAccess access
	) {
		revokeAll(player, tracker);
		applyAll(player, tracker, access);

		// Post-recompute: clamp health if max_health was reduced
		float maxHealth = player.getMaxHealth();
		if (player.getHealth() > maxHealth) {
			player.setHealth(maxHealth);
		}
	}

	/**
	 * Revoke all skill-granted effects:
	 * <ol>
	 *   <li>Clear ability flags (bulk)</li>
	 *   <li>Remove all skill-based attribute modifiers</li>
	 *   <li>Restore original base values for overridden attributes</li>
	 * </ol>
	 */
	private static void revokeAll(ServerPlayer player, PlayerSkillData tracker) {
		// 1. Bulk-clear ability flags (they'll be re-granted in applyAll)
		tracker.clearAbilityFlags();

		// 2. Remove all attribute modifiers owned by this mod's skill system
		player.getAttributes().getSyncableAttributes().forEach(instance -> {
			var modsSnapshot = List.copyOf(instance.getModifiers());
			for (AttributeModifier mod : modsSnapshot) {
				ResourceLocation id = mod.id();
				if (Constants.MOD_ID.equals(id.getNamespace())
						&& id.getPath().startsWith("skill/")) {
					instance.removeModifier(id);
				}
			}
		});

		// 3. Restore original base values
		Map<ResourceLocation, Double> originals = tracker.getOriginalBases();
		if (!originals.isEmpty()) {
			Registry<Attribute> attrRegistry = player.level().registryAccess()
					.registryOrThrow(Registries.ATTRIBUTE);

			for (Map.Entry<ResourceLocation, Double> entry : originals.entrySet()) {
				ResourceKey<Attribute> attrKey = ResourceKey.create(
						Registries.ATTRIBUTE, entry.getKey());
				var holderOpt = attrRegistry.getHolder(attrKey);
				if (holderOpt.isPresent()) {
					AttributeInstance instance = player.getAttributes()
							.getInstance(holderOpt.get());
					if (instance != null) {
						instance.setBaseValue(entry.getValue());
					}
				}
			}
		}
		tracker.clearOriginalBases();
	}

	/**
	 * Apply passives for ALL registered skills, including those the player
	 * has no progress in (level 0). This ensures initial restrictions apply
	 * immediately.
	 * <p>
	 * Uses a two-pass approach:
	 * <ol>
	 *   <li>Collect all base overrides and merge conflicts</li>
	 *   <li>Apply merged base overrides, then modifiers and flags</li>
	 * </ol>
	 */
	private static void applyAll(
			ServerPlayer player,
			PlayerSkillData tracker,
			RegistryAccess access
	) {
		Registry<SkillDefinition> skillRegistry =
				access.registryOrThrow(ModRegistries.SKILL_REGISTRY_KEY);

		// ---- Pass 1: Collect base overrides with conflict merge ----
		// Key = attribute RL, Value = minimum computed base across all skills
		Map<ResourceLocation, Double> baseMerge = new HashMap<>();

		// We also need to track which passives need standard apply
		// (AttributeBonus, AbilityFlag). Collect contexts for pass 2.

		for (var entry : skillRegistry.entrySet()) {
			ResourceKey<SkillDefinition> skillKey = entry.getKey();
			SkillDefinition def = entry.getValue();
			int level = tracker.getLevel(skillKey);

			for (SkillPassive passive : def.passives()) {
				if (passive instanceof SkillPassive.AttributeBase(
						ResourceLocation attribute, net.minecraft.world.item.enchantment.LevelBasedValue value
				)) {
					// Compute the base value for this skill's level
					double computedBase = value.calculate(level + 1);
					// Conflict resolution: minimum wins (strictest restriction)
					baseMerge.merge(attribute, computedBase, Math::min);
				}
			}
		}

		// ---- Apply collected base overrides ----
		// Save originals BEFORE modifying, then set new base values.
		if (!baseMerge.isEmpty()) {
			Registry<Attribute> attrRegistry = access.registryOrThrow(Registries.ATTRIBUTE);

			for (Map.Entry<ResourceLocation, Double> baseEntry : baseMerge.entrySet()) {
				ResourceKey<Attribute> attrKey = ResourceKey.create(
						Registries.ATTRIBUTE, baseEntry.getKey());
				var holderOpt = attrRegistry.getHolder(attrKey);
				if (holderOpt.isEmpty()) continue;

				AttributeInstance instance = player.getAttributes()
						.getInstance(holderOpt.get());
				if (instance == null) continue;

				// Save original before overwriting
				tracker.saveOriginalBase(baseEntry.getKey(), instance.getBaseValue());
				instance.setBaseValue(baseEntry.getValue());
			}
		}

		// ---- Pass 2: Apply modifiers and flags ----
		for (var entry : skillRegistry.entrySet()) {
			ResourceKey<SkillDefinition> skillKey = entry.getKey();
			SkillDefinition def = entry.getValue();
			ResourceLocation skillId = skillKey.location();
			int level = tracker.getLevel(skillKey);

			int passiveIndex = -1;
			for (SkillPassive passive : def.passives()) {
				passiveIndex++;

				// Skip AttributeBase — already handled in pass 1
				if (passive instanceof SkillPassive.AttributeBase) continue;

				if (level >= passive.unlockLevel()) {
					SkillPassive.Context ctx = new SkillPassive.Context(
							player, tracker, access, skillId, passiveIndex
					);
					passive.apply(ctx, level);
				}
			}
		}
	}
}
