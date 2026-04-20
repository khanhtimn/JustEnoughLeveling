package dev.khanhtimn.jel.common.skill;

import dev.khanhtimn.jel.core.ModRegistries;
import dev.khanhtimn.jel.core.ModSyncedDataKeys;
import dev.khanhtimn.jel.data.skill.SkillsTracker;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Orchestrator for skill operations: leveling, XP gain, refunds.
 * <p>
 * This class enforces all game rules (max level, XP formulas, passive recomputation)
 * and is the <b>only</b> external-facing API for mutating skill state.
 * Do not modify {@link SkillsTracker} directly from outside this package.
 */
public final class SkillLogic {

	private SkillLogic() {}

	// ---- Accessor ----

	/**
	 * Get the live {@link SkillsTracker} for a player.
	 * On server: authoritative copy, mutations trigger Framework sync.
	 * On client: synced read-only copy.
	 */
	public static SkillsTracker getSkills(Player player) {
		return ModSyncedDataKeys.PLAYER_SKILLS.getValue(player);
	}

	// ---- Convenience overloads (auto-resolve tracker) ----

	public static boolean tryLevelUpOnce(ServerPlayer player, ResourceKey<SkillDefinition> skillKey) {
		return tryLevelUp(player, getSkills(player), skillKey, 1) > 0;
	}

	public static int tryLevelUp(ServerPlayer player, ResourceKey<SkillDefinition> skillKey, int levelsToGain) {
		return tryLevelUp(player, getSkills(player), skillKey, levelsToGain);
	}

	public static int addSkillXp(ServerPlayer player, ResourceKey<SkillDefinition> skillKey, int skillXpDelta) {
		return addSkillXp(player, getSkills(player), skillKey, skillXpDelta);
	}

	public static int addSkillXpFromVanilla(ServerPlayer player, ResourceKey<SkillDefinition> skillKey, int maxVanillaXpToSpend) {
		return addSkillXpFromVanilla(player, getSkills(player), skillKey, maxVanillaXpToSpend);
	}

	public static int refundSkill(ServerPlayer player, ResourceKey<SkillDefinition> skillKey) {
		return refundSkill(player, getSkills(player), skillKey);
	}

	// ---- Core operations ----

	/**
	 * Try to level up by up to {@code levelsToGain} levels,
	 * spending vanilla XP to cover any missing skill XP.
	 *
	 * @return number of levels actually gained
	 */
	public static int tryLevelUp(
			ServerPlayer player,
			SkillsTracker tracker,
			ResourceKey<SkillDefinition> skillKey,
			int levelsToGain
	) {
		if (levelsToGain <= 0) return 0;

		SkillDefinition def = lookupDefinition(player, skillKey);
		if (def == null) return 0;

		int gained = 0;
		for (int i = 0; i < levelsToGain; i++) {
			if (!tryLevelUpOnceInternal(player, tracker, skillKey, def)) break;
			gained++;
		}
		return gained;
	}

	/**
	 * Add pure skill XP (no vanilla XP involved). Auto-levels using the XP formula.
	 *
	 * @return number of levels gained
	 */
	public static int addSkillXp(
			ServerPlayer player,
			SkillsTracker tracker,
			ResourceKey<SkillDefinition> skillKey,
			int skillXpDelta
	) {
		if (skillXpDelta <= 0) return 0;

		SkillDefinition def = lookupDefinition(player, skillKey);
		if (def == null) return 0;

		SkillProgress before = tracker.getProgress(skillKey);
		SkillProgress after = applySkillXp(before, def, skillXpDelta);
		tracker.setProgress(skillKey, after);

		int levelsGained = after.level() - before.level();
		if (levelsGained != 0) {
			PassiveApplier.recomputeAll(player, tracker, player.level().registryAccess());
		}
		return levelsGained;
	}

	/**
	 * Add skill XP by spending vanilla XP, using this skill's {@link XpConversion}.
	 *
	 * @param maxVanillaXpToSpend maximum vanilla XP points to consume
	 * @return number of levels gained
	 */
	public static int addSkillXpFromVanilla(
			ServerPlayer player,
			SkillsTracker tracker,
			ResourceKey<SkillDefinition> skillKey,
			int maxVanillaXpToSpend
	) {
		if (maxVanillaXpToSpend <= 0) return 0;

		SkillDefinition def = lookupDefinition(player, skillKey);
		if (def == null) return 0;

		int available = VanillaXpHelper.getTotalXp(player);
		if (available <= 0) return 0;

		int toSpend = Math.min(available, maxVanillaXpToSpend);
		if (!VanillaXpHelper.subtract(player, toSpend)) return 0;

		int currentLevel = tracker.getLevel(skillKey);
		int skillXp = def.vanillaToSkillXp(toSpend, currentLevel);
		if (skillXp <= 0) {
			// Conversion yielded nothing — refund everything
			VanillaXpHelper.add(player, toSpend);
			return 0;
		}

		// Refund unconverted remainder to the player
		int actualCost = def.skillToVanillaXp(skillXp, currentLevel);
		int remainder = toSpend - actualCost;
		if (remainder > 0) {
			VanillaXpHelper.add(player, remainder);
		}

		return addSkillXp(player, tracker, skillKey, skillXp);
	}

	/**
	 * Refund all XP invested in a skill back to the vanilla XP bar,
	 * then reset the skill to level 0 / 0 XP.
	 *
	 * @return vanilla XP points refunded
	 */
	public static int refundSkill(
			ServerPlayer player,
			SkillsTracker tracker,
			ResourceKey<SkillDefinition> skillKey
	) {
		RegistryAccess access = player.level().registryAccess();
		SkillDefinition def = lookupDefinition(player, skillKey);
		SkillProgress prog = tracker.getProgress(skillKey);

		if (def == null) {
			tracker.setProgress(skillKey, SkillProgress.ZERO);
			PassiveApplier.recomputeAll(player, tracker, access);
			return 0;
		}

		int level = prog.level();
		int overflow = prog.xp();
		if (level <= 0 && overflow <= 0) return 0;

		// Precise per-level refund: uses the conversion rate the player
		// was at when they earned each level, not their current (worst) rate.
		int vanillaRefund = 0;
		for (int lvl = 1; lvl <= level; lvl++) {
			int skillXpForLevel = def.xpCostForLevel(lvl);
			// When earning level lvl, the player was at skill level (lvl - 1)
			vanillaRefund += def.skillToVanillaXp(skillXpForLevel, lvl - 1);
		}
		// Refund overflow XP at current level's rate
		if (overflow > 0) {
			vanillaRefund += def.skillToVanillaXp(overflow, level);
		}

		VanillaXpHelper.add(player, vanillaRefund);

		tracker.setProgress(skillKey, SkillProgress.ZERO);
		PassiveApplier.recomputeAll(player, tracker, access);

		return vanillaRefund;
	}

	/**
	 * Recompute all passive effects. Delegates to {@link PassiveApplier}.
	 * Called from event handlers on login/respawn.
	 */
	public static void recomputeAll(
			ServerPlayer player,
			SkillsTracker tracker,
			RegistryAccess access
	) {
		PassiveApplier.recomputeAll(player, tracker, access);
	}

	// ---- Internal helpers ----

	private static boolean tryLevelUpOnceInternal(
			ServerPlayer player,
			SkillsTracker tracker,
			ResourceKey<SkillDefinition> skillKey,
			SkillDefinition def
	) {
		SkillProgress current = tracker.getProgress(skillKey);
		int currentLevel = current.level();

		if (def.isMaxLevel(currentLevel)) return false;

		int costNext = def.xpCostForNextLevel(currentLevel);
		if (costNext <= 0) return false;

		int currentSkillXp = current.xp();
		int neededSkillXp = costNext - currentSkillXp;

		if (neededSkillXp > 0) {
			// Convert needed skill XP to vanilla XP cost
			int vanillaCost = def.skillToVanillaXp(neededSkillXp, currentLevel);
			if (!VanillaXpHelper.subtract(player, vanillaCost)) return false;

			// Add the missing skill XP and let applySkillXp handle the level-up
			return addSkillXp(player, tracker, skillKey, neededSkillXp) > 0;
		}

		// Already have enough XP — just flush the level-up
		SkillProgress after = applySkillXp(current, def, 0);
		if (after.level() > currentLevel) {
			tracker.setProgress(skillKey, after);
			PassiveApplier.recomputeAll(player, tracker, player.level().registryAccess());
			return true;
		}
		return false;
	}

	/**
	 * Apply skill XP delta to progress, auto-leveling using the definition's formula.
	 */
	static SkillProgress applySkillXp(SkillProgress current, SkillDefinition def, int skillXpDelta) {
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

	private static SkillDefinition lookupDefinition(ServerPlayer player, ResourceKey<SkillDefinition> skillKey) {
		RegistryAccess access = player.level().registryAccess();
		Registry<SkillDefinition> registry = access.registryOrThrow(ModRegistries.SKILL_REGISTRY_KEY);
		return registry.get(skillKey);
	}
}
