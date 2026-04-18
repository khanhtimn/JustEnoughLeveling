package dev.khanhtimn.jel.registry.skill;

import dev.khanhtimn.jel.JustEnoughLeveling;
import dev.khanhtimn.jel.component.SkillsComponent;
import dev.khanhtimn.jel.core.ModSyncedDataKeys;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;


public final class SkillLogic {

	private SkillLogic() {
	}

	// ---- Live SyncedDataKey accessor ----

	/**
	 * Get the live, mutable {@link SkillsComponent} for a player.
	 * On the server this is the authoritative copy; mutations automatically
	 * trigger Framework sync via {@link SkillsComponent#markDirty()}.
	 * On the client this is the synced read-only copy.
	 */
	public static SkillsComponent getSkills(Player player) {
		return ModSyncedDataKeys.PLAYER_SKILLS.getValue(player);
	}

	// ---- Convenience overloads (auto-resolve component) ----

	/**
	 * @see #tryLevelUpOnce(ServerPlayer, SkillsComponent, ResourceKey)
	 */
	public static boolean tryLevelUpOnce(
			ServerPlayer player,
			ResourceKey<SkillDefinition> skillKey
	) {
		return tryLevelUpOnce(player, getSkills(player), skillKey);
	}

	/**
	 * @see #tryLevelUp(ServerPlayer, SkillsComponent, ResourceKey, int)
	 */
	public static int tryLevelUp(
			ServerPlayer player,
			ResourceKey<SkillDefinition> skillKey,
			int levelsToGain
	) {
		return tryLevelUp(player, getSkills(player), skillKey, levelsToGain);
	}

	/**
	 * @see #addSkillXp(ServerPlayer, SkillsComponent, ResourceKey, int)
	 */
	public static int addSkillXp(
			ServerPlayer player,
			ResourceKey<SkillDefinition> skillKey,
			int skillXpDelta
	) {
		return addSkillXp(player, getSkills(player), skillKey, skillXpDelta);
	}

	/**
	 * @see #addSkillXpFromVanilla(ServerPlayer, SkillsComponent, ResourceKey, int)
	 */
	public static int addSkillXpFromVanilla(
			ServerPlayer player,
			ResourceKey<SkillDefinition> skillKey,
			int maxVanillaXpToSpend
	) {
		return addSkillXpFromVanilla(player, getSkills(player), skillKey, maxVanillaXpToSpend);
	}

	/**
	 * @see #refundSkill(ServerPlayer, SkillsComponent, ResourceKey)
	 */
	public static int refundSkill(
			ServerPlayer player,
			ResourceKey<SkillDefinition> skillKey
	) {
		return refundSkill(player, getSkills(player), skillKey);
	}

	/**
	 * Try to level up the given skill by exactly 1 skill level, using vanilla XP
	 * as needed to cover the missing skill XP for the next level.
	 *
	 * @return true if the skill level increased by at least 1.
	 */
	public static boolean tryLevelUpOnce(
			ServerPlayer player,
			SkillsComponent component,
			ResourceKey<SkillDefinition> skillKey
	) {
		return tryLevelUp(player, component, skillKey, 1) > 0;
	}

	/**
	 * Try to level up the given skill by up to {@code levelsToGain} times.
	 * For each level:
	 * - Compute required skill XP for next level (using SkillDefinition.xp).
	 * - Spend missing skill XP by converting vanilla XP via subtractXpFromVanilla.
	 * - Apply skill XP and perform level-up(s).
	 *
	 * @return how many levels were actually gained.
	 */
	public static int tryLevelUp(
			ServerPlayer player,
			SkillsComponent component,
			ResourceKey<SkillDefinition> skillKey,
			int levelsToGain
	) {
		if (levelsToGain <= 0) return 0;

		int gainedTotal = 0;
		for (int i = 0; i < levelsToGain; i++) {
			if (!tryLevelUpInternalOnce(player, component, skillKey)) {
				break;
			}
			gainedTotal++;
		}
		return gainedTotal;
	}

	/**
	 * Add pure skill XP to a skill (no vanilla XP involved).
	 *
	 * @return levels gained.
	 */
	public static int addSkillXp(
			ServerPlayer player,
			SkillsComponent component,
			ResourceKey<SkillDefinition> skillKey,
			int skillXpDelta
	) {
		if (skillXpDelta <= 0) return 0;

		RegistryAccess access = player.level().registryAccess();
		Registry<SkillDefinition> skillRegistry =
				access.registryOrThrow(SkillRegistry.SKILL_REGISTRY_KEY);

		SkillDefinition def = skillRegistry.get(skillKey);
		if (def == null) {
			return 0;
		}

		SkillProgress before = component.getProgress(skillKey);
		SkillProgress after = applySkillXp(before, def, skillXpDelta);

		component.setProgress(skillKey, after);

		int levelsGained = after.level() - before.level();
		if (levelsGained != 0) {
			recomputeAll(player, component, access);
		}

		return levelsGained;
	}

	/**
	 * Add skill XP to a skill by spending vanilla XP points.
	 *
	 * @param maxVanillaXpToSpend maximum vanilla XP points to consume
	 * @return levels gained.
	 */
	public static int addSkillXpFromVanilla(
			ServerPlayer player,
			SkillsComponent component,
			ResourceKey<SkillDefinition> skillKey,
			int maxVanillaXpToSpend
	) {
		if (maxVanillaXpToSpend <= 0) return 0;

		int available = getTotalXp(player);
		if (available <= 0) return 0;

		int toSpend = Math.min(available, maxVanillaXpToSpend);
		if (!subtractXpFromVanilla(player, toSpend)) {
			return 0;
		}

		int skillXp = computeSkillXpFromVanilla(skillKey, toSpend);
		return addSkillXp(player, component, skillKey, skillXp);
	}

	/**
	 * Refund all XP invested into a specific skill back into the player's
	 * vanilla XP bar, and reset that skill to level 0 and 0 skill XP.
	 *
	 * @return refunded vanilla XP points.
	 */
	public static int refundSkill(
			ServerPlayer player,
			SkillsComponent component,
			ResourceKey<SkillDefinition> skillKey
	) {
		RegistryAccess access = player.level().registryAccess();
		Registry<SkillDefinition> skillRegistry =
				access.registryOrThrow(SkillRegistry.SKILL_REGISTRY_KEY);

		SkillDefinition def = skillRegistry.get(skillKey);
		SkillProgress prog = component.getProgress(skillKey);

		if (def == null) {
			// No definition; just zero out for safety.
			component.setProgress(skillKey, SkillProgress.ZERO);
			recomputeAll(player, component, access);
			return 0;
		}

		int level = prog.level();
		int overflow = prog.xp();

		if (level <= 0 && overflow <= 0) {
			return 0;
		}

		// Total skill XP invested = cost to reach current level + overflow
		int totalSkillXp =
				def.totalXpCostToReachLevel(level) + overflow;

		int vanillaRefund = computeVanillaXpFromSkill(skillKey, totalSkillXp);

		int currentTotal = getTotalXp(player);
		setTotalXp(player, currentTotal + vanillaRefund);

		component.setProgress(skillKey, SkillProgress.ZERO);
		recomputeAll(player, component, access);

		return vanillaRefund;
	}

	/**
	 * Recompute ability flags and attribute modifiers for all skills
	 * currently in the component.
	 */
	public static void recomputeAll(
			ServerPlayer player,
			SkillsComponent component,
			RegistryAccess access
	) {
		recomputeAbilityFlags(access, component);
		recomputeAttributes(access, player, component);
	}

	private static boolean tryLevelUpInternalOnce(
			ServerPlayer player,
			SkillsComponent component,
			ResourceKey<SkillDefinition> skillKey
	) {
		RegistryAccess access = player.level().registryAccess();
		Registry<SkillDefinition> skillRegistry =
				access.registryOrThrow(SkillRegistry.SKILL_REGISTRY_KEY);

		SkillDefinition def = skillRegistry.get(skillKey);
		if (def == null) {
			return false;
		}

		SkillProgress current = component.getProgress(skillKey);
		int currentLevel = current.level();

		// If already above or at max, still allow "flush" if overflow is enough
		if (def.isMaxLevel(currentLevel)) {
			SkillProgress after = applySkillXp(current, def, 0);
			if (after.level() > currentLevel) {
				component.setProgress(skillKey, after);
				recomputeAll(player, component, access);
				return true;
			}
			return false;
		}

		int costNext = def.xpCostForNextLevel(currentLevel);
		if (costNext <= 0) {
			return false;
		}

		int currentSkillXp = current.xp();
		int neededSkillXp = Math.max(0, costNext - currentSkillXp);

		// If we already have enough skill XP, just apply with delta 0
		if (neededSkillXp == 0) {
			SkillProgress after = applySkillXp(current, def, 0);
			component.setProgress(skillKey, after);
			if (after.level() > currentLevel) {
				recomputeAll(player, component, access);
				return true;
			}
			return false;
		}

		// Otherwise, pay missing skill XP from vanilla XP bar
		int vanillaCost = computeVanillaXpFromSkill(skillKey, neededSkillXp);
		if (!subtractXpFromVanilla(player, vanillaCost)) {
			return false;
		}

		int levelsGained = addSkillXp(player, component, skillKey, neededSkillXp);
		return levelsGained > 0;
	}

	/**
	 * Apply skill XP to a SkillProgress using a SkillDefinition.
	 */
	private static SkillProgress applySkillXp(
			SkillProgress current,
			SkillDefinition def,
			int skillXpDelta
	) {
		int level = current.level();
		int xp = Math.max(0, current.xp() + skillXpDelta);

		while (level < def.maxLevel()) {
			int costNext = def.xpCostForNextLevel(level);
			if (costNext <= 0 || xp < costNext) break;
			xp -= costNext;
			level++;
		}

		return new SkillProgress(level, xp);
	}

	private static void recomputeAbilityFlags(
			RegistryAccess access,
			SkillsComponent component
	) {
		component.clearAbilityFlags();

		Registry<SkillDefinition> skillRegistry =
				access.registryOrThrow(SkillRegistry.SKILL_REGISTRY_KEY);

		for (Map.Entry<ResourceLocation, SkillProgress> entry :
				component.getAllSkills().entrySet()) {

			ResourceLocation skillId = entry.getKey();
			int level = entry.getValue().level();
			if (level <= 0) continue;

			ResourceKey<SkillDefinition> skillKey =
					ResourceKey.create(SkillRegistry.SKILL_REGISTRY_KEY, skillId);

			SkillDefinition def = skillRegistry.get(skillKey);
			if (def == null) continue;

			for (SkillPassive passive : def.passives()) {
				if (passive instanceof SkillPassive.AbilityFlag(
						int unlockLevel, ResourceLocation flagId
				)) {
					if (level >= unlockLevel) {
						component.grantAbilityFlag(flagId);
					}
				}
			}
		}
	}

	private static void recomputeAttributes(
			RegistryAccess access,
			ServerPlayer player,
			SkillsComponent component
	) {
		var attributeMap = player.getAttributes();

		// 1. Remove old skill-based modifiers
		attributeMap.getSyncableAttributes().forEach(instance -> {
			var modsSnapshot = List.copyOf(instance.getModifiers());
			for (AttributeModifier mod : modsSnapshot) {
				if (isOurSkillModifier(mod)) {
					instance.removeModifier(mod.id());
				}
			}
		});

		Registry<SkillDefinition> skillRegistry =
				access.registryOrThrow(SkillRegistry.SKILL_REGISTRY_KEY);
		Registry<Attribute> attributeRegistry =
				access.registryOrThrow(Registries.ATTRIBUTE);

		// 2. Re-add modifiers from AttributeBonus passives of all skills
		for (Map.Entry<ResourceLocation, SkillProgress> entry :
				component.getAllSkills().entrySet()) {

			ResourceLocation skillId = entry.getKey();
			int level = entry.getValue().level();
			if (level <= 0) continue;

			ResourceKey<SkillDefinition> skillKey =
					ResourceKey.create(SkillRegistry.SKILL_REGISTRY_KEY, skillId);

			SkillDefinition def = skillRegistry.get(skillKey);
			if (def == null) continue;

			int passiveIndex = -1;
			for (SkillPassive passive : def.passives()) {
				passiveIndex++;
				if (!(passive instanceof SkillPassive.AttributeBonus(
						int unlockLevel,
						ResourceLocation attributeId,
						double amountPerLevel,
						AttributeModifier.Operation operation
				))) {
					continue;
				}

				if (level < unlockLevel) continue;

				ResourceKey<Attribute> attrKey =
						ResourceKey.create(Registries.ATTRIBUTE, attributeId);
				var attrHolderOpt = attributeRegistry.getHolder(attrKey);
				if (attrHolderOpt.isEmpty()) continue;
				Holder<Attribute> attrHolder = attrHolderOpt.get();

				AttributeInstance instance = attributeMap.getInstance(attrHolder);
				if (instance == null) continue;

				// Levels above unlock threshold contribute
				int effectiveLevels = Math.max(0, level - unlockLevel + 1);
				double amount = effectiveLevels * amountPerLevel;
				if (amount == 0.0) continue;

				ResourceLocation id = computeModifierId(skillId, passiveIndex);

				AttributeModifier modifier = new AttributeModifier(
						id,
						amount,
						operation
				);
				instance.addPermanentModifier(modifier);
			}
		}
	}

	private static ResourceLocation computeModifierId(
			ResourceLocation skillId,
			int passiveIndex
	) {
		// justenoughleveling:skill/<skillNamespace>/<skillPath>/<index>
		String path =
				"skill/" + skillId.getNamespace()
						+ "/" + skillId.getPath()
						+ "/" + passiveIndex;
		return ResourceLocation.fromNamespaceAndPath(JustEnoughLeveling.MOD_ID, path);
	}

	private static boolean isOurSkillModifier(AttributeModifier mod) {
		ResourceLocation id = mod.id();
		return JustEnoughLeveling.MOD_ID.equals(id.getNamespace())
				&& id.getPath().startsWith("skill/");
	}

	/**
	 * Try to subtract exactly {@code amount} vanilla XP points from the player.
	 *
	 * @return true if XP was successfully subtracted; false if not enough XP.
	 */
	public static boolean subtractXpFromVanilla(ServerPlayer player, int amount) {
		if (amount <= 0) {
			return false;
		}
		int total = getTotalXp(player);
		if (total < amount) {
			return false;
		}
		setTotalXp(player, total - amount);
		return true;
	}

	/**
	 * Total XP points currently represented by the player's level and
	 * fractional progress.
	 */
	public static int getTotalXp(ServerPlayer player) {
		int level = player.experienceLevel;
		float progress = player.experienceProgress;
		int xpForCurrentLevel = xpNeededForLevel(level);
		int xp = xpAtLevel(level);
		xp += (int) (progress * xpForCurrentLevel);
		return xp;
	}

	/**
	 * Set the player's XP bar (levels + progress) to represent {@code totalXp}
	 * points. This resets their level and re-applies XP via giveExperiencePoints.
	 */
	public static void setTotalXp(ServerPlayer player, int totalXp) {
		totalXp = Math.max(0, totalXp);
		player.experienceLevel = 0;
		player.experienceProgress = 0.0F;
		player.totalExperience = 0;
		player.giveExperiencePoints(totalXp);
	}

	/**
	 * Default conversion: 1 skill XP unit == 1 vanilla XP point.
	 * Override or adjust for skill-specific rates.
	 */
	private static int computeSkillXpFromVanilla(
			ResourceKey<SkillDefinition> skillKey,
			int vanillaXpSpent
	) {
		return vanillaXpSpent;
	}

	private static int computeVanillaXpFromSkill(
			ResourceKey<SkillDefinition> skillKey,
			int skillXp
	) {
		return skillXp;
	}

	/**
	 * XP needed to go from this level to the next level (vanilla curve).
	 */
	private static int xpNeededForLevel(int level) {
		if (level >= 30) {
			return 112 + (level - 30) * 9;
		} else if (level >= 15) {
			return 37 + (level - 15) * 5;
		} else {
			return 7 + level * 2;
		}
	}

	/**
	 * Total XP points required to reach {@code level} from 0 (vanilla curve).
	 */
	private static int xpAtLevel(int level) {
		int xp = 0;
		for (int i = 0; i < level; ++i) {
			xp += xpNeededForLevel(i);
		}
		return xp;
	}
}
