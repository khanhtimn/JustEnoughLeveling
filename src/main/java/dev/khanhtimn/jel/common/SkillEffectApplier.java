package dev.khanhtimn.jel.common;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.perk.ApplyMode;
import dev.khanhtimn.jel.api.perk.Perk;
import dev.khanhtimn.jel.api.perk.PerkContext;
import dev.khanhtimn.jel.api.skill.AttributeEffect;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
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
 * Applies and revokes all skill effects (attributes + perks)
 * based on the player's current skill levels.
 * <p>
 * Uses a collect-then-apply pattern for deterministic conflict
 * resolution when multiple skills target the same attribute base.
 */
public final class SkillEffectApplier {

	private SkillEffectApplier() {
	}

	public static void recomputeAll(
			ServerPlayer player,
			PlayerSkillData tracker,
			RegistryAccess access
	) {
		revokeAll(player, tracker, access);
		applyAll(player, tracker, access);

		float maxHealth = player.getMaxHealth();
		if (player.getHealth() > maxHealth) {
			player.setHealth(maxHealth);
		}
	}

	private static void revokeAll(
			ServerPlayer player,
			PlayerSkillData tracker,
			RegistryAccess access
	) {
		// 1. Revoke STATE perks
		Registry<SkillDefinition> skillRegistry =
				access.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);
		for (var entry : skillRegistry.entrySet()) {
			SkillDefinition def = entry.getValue();
			ResourceLocation skillId = entry.getKey().location();
			PerkContext ctx = new PerkContext(player, tracker, skillId);
			for (Perk perk : def.perks()) {
				if (perk.applyMode() == ApplyMode.STATE) {
					perk.revoke(ctx);
				}
			}
		}

		// 2. Bulk-clear traits
		tracker.clearTraits();

		// 3. Remove all attribute modifiers owned by this mod's skill system
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

		// 4. Restore original base values
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

	private static void applyAll(
			ServerPlayer player,
			PlayerSkillData tracker,
			RegistryAccess access
	) {
		Registry<SkillDefinition> skillRegistry =
				access.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);

		Map<ResourceLocation, Double> baseMerge = new HashMap<>();

		for (var entry : skillRegistry.entrySet()) {
			SkillDefinition def = entry.getValue();
			int level = tracker.getLevel(entry.getKey());

			for (AttributeEffect effect : def.attributes()) {
				if (effect.isBaseOverride()) {
					// +1 for LBV convention
					double computedBase = effect.value().calculate(level + 1);
					// Conflict resolution: minimum wins
					baseMerge.merge(effect.attribute(), computedBase, Math::min);
				}
			}
		}

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

				tracker.saveOriginalBase(baseEntry.getKey(), instance.getBaseValue());
				instance.setBaseValue(baseEntry.getValue());
			}
		}

		for (var entry : skillRegistry.entrySet()) {
			ResourceKey<SkillDefinition> skillKey = entry.getKey();
			SkillDefinition def = entry.getValue();
			ResourceLocation skillId = skillKey.location();
			int level = tracker.getLevel(skillKey);

			int attrIndex = -1;
			for (AttributeEffect effect : def.attributes()) {
				attrIndex++;

				if (effect.isBaseOverride()) continue;
				if (level < effect.unlockLevel()) continue;

				applyModifier(player, access, effect, level, skillId, attrIndex);
			}
		}

		for (var entry : skillRegistry.entrySet()) {
			ResourceKey<SkillDefinition> skillKey = entry.getKey();
			SkillDefinition def = entry.getValue();
			ResourceLocation skillId = skillKey.location();
			int level = tracker.getLevel(skillKey);

			PerkContext ctx = new PerkContext(player, tracker, skillId);
			for (Perk perk : def.perks()) {
				if (perk.applyMode() != ApplyMode.STATE) continue;
				if (level >= perk.unlockLevel()) {
					perk.apply(ctx, level);
				}
			}
		}
	}

	public static void firePerkEvents(
			ServerPlayer player,
			PlayerSkillData tracker,
			ResourceKey<SkillDefinition> skillKey,
			SkillDefinition def,
			int oldLevel,
			int newLevel
	) {
		PerkContext ctx = new PerkContext(player, tracker, skillKey.location());
		for (Perk perk : def.perks()) {
			if (perk.applyMode() != ApplyMode.EVENT) continue;
			int threshold = perk.unlockLevel();

			if (oldLevel < threshold && newLevel >= threshold) {
				perk.apply(ctx, newLevel);
			} else if (oldLevel >= threshold && newLevel < threshold) {
				perk.revoke(ctx);
			}
		}
	}

	private static void applyModifier(
			ServerPlayer player,
			RegistryAccess access,
			AttributeEffect effect,
			int level,
			ResourceLocation skillId,
			int attrIndex
	) {
		Registry<Attribute> attrRegistry = access.registryOrThrow(Registries.ATTRIBUTE);
		ResourceKey<Attribute> attrKey = ResourceKey.create(
				Registries.ATTRIBUTE, effect.attribute());
		var holderOpt = attrRegistry.getHolder(attrKey);
		if (holderOpt.isEmpty()) return;

		AttributeInstance instance = player.getAttributes()
				.getInstance(holderOpt.get());
		if (instance == null) return;

		// Effective levels above unlock, shifted +1 for vanilla LBV convention
		int effectiveLevel = level - effect.unlockLevel() + 1;
		double amount = effect.value().calculate(effectiveLevel);
		if (amount == 0.0) return;

		// Stable modifier ID: jel:skill/<namespace>/<path>/<index>
		String path = "skill/" + skillId.getNamespace()
				+ "/" + skillId.getPath()
				+ "/" + attrIndex;
		ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(
				Constants.MOD_ID, path);

		AttributeModifier modifier = new AttributeModifier(
				modifierId, amount, effect.toModifierOperation()
		);

		instance.removeModifier(modifierId);
		instance.addPermanentModifier(modifier);
	}
}
