package dev.khanhtimn.jel.api;

import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.SkillProgress;
import dev.khanhtimn.jel.common.ModSyncedDataKeys;
import dev.khanhtimn.jel.common.PlayerSkillData;
import dev.khanhtimn.jel.common.SkillEffectApplier;
import dev.khanhtimn.jel.common.SkillOperations;
import dev.khanhtimn.jel.common.VanillaXpHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Map;

/**
 * Public API for querying and mutating skill state.
 * <p>
 * This is the primary entry point for external mods interacting with JEL's
 * skill system. All mutation methods enforce game rules (max level, XP formulas,
 * effect recomputation).
 * <p>
 * For trait queries, see {@link JelTraits}.
 * For perk extension, see {@link dev.khanhtimn.jel.api.perk.PerkType}.
 */
public final class JelSkills {

	private JelSkills() {
	}

	// --- Query API ---

	public static int getLevel(Player player, ResourceKey<SkillDefinition> skillKey) {
		return getSkillData(player).getLevel(skillKey);
	}

	public static int getXp(Player player, ResourceKey<SkillDefinition> skillKey) {
		return getSkillData(player).getXp(skillKey);
	}

	public static SkillProgress getProgress(Player player, ResourceKey<SkillDefinition> skillKey) {
		return getSkillData(player).getProgress(skillKey);
	}

	public static Map<ResourceLocation, SkillProgress> getAllProgress(Player player) {
		return Collections.unmodifiableMap(getSkillData(player).getAllSkills());
	}

	public static boolean hasSkill(Player player, ResourceKey<SkillDefinition> skillKey) {
		return getSkillData(player).hasSkill(skillKey);
	}

	// --- Mutation API ---

	public static boolean tryLevelUpOnce(ServerPlayer player, ResourceKey<SkillDefinition> skillKey) {
		return tryLevelUp(player, skillKey, 1) > 0;
	}

	public static int tryLevelUp(ServerPlayer player, ResourceKey<SkillDefinition> skillKey, int levelsToGain) {
		if (levelsToGain <= 0) {
			return 0;
		}
		SkillDefinition def = SkillOperations.lookupDefinition(player, skillKey);
		if (def == null) {
			return 0;
		}
		PlayerSkillData tracker = getSkillData(player);
		int gained = 0;
		for (int i = 0; i < levelsToGain; i++) {
			if (!SkillOperations.tryLevelUpOnceInternal(player, tracker, skillKey, def)) {
				break;
			}
			gained++;
		}
		return gained;
	}

	public static int addSkillXp(ServerPlayer player, ResourceKey<SkillDefinition> skillKey, int skillXpDelta) {
		if (skillXpDelta <= 0) {
			return 0;
		}
		SkillDefinition def = SkillOperations.lookupDefinition(player, skillKey);
		if (def == null) {
			return 0;
		}
		return SkillOperations.addSkillXp(player, getSkillData(player), skillKey, def, skillXpDelta);
	}

	public static int addSkillXpFromVanilla(ServerPlayer player, ResourceKey<SkillDefinition> skillKey, int maxVanillaXpToSpend) {
		if (maxVanillaXpToSpend <= 0) {
			return 0;
		}
		SkillDefinition def = SkillOperations.lookupDefinition(player, skillKey);
		if (def == null) {
			return 0;
		}
		int available = VanillaXpHelper.getTotalXp(player);
		if (available <= 0) {
			return 0;
		}
		PlayerSkillData tracker = getSkillData(player);
		int toSpend = Math.min(available, maxVanillaXpToSpend);
		if (!VanillaXpHelper.subtract(player, toSpend)) {
			return 0;
		}
		int currentLevel = tracker.getLevel(skillKey);
		int skillXp = def.vanillaToSkillXp(toSpend, currentLevel);
		if (skillXp <= 0) {
			VanillaXpHelper.add(player, toSpend);
			return 0;
		}
		int actualCost = def.skillToVanillaXp(skillXp, currentLevel);
		int remainder = toSpend - actualCost;
		if (remainder > 0) {
			VanillaXpHelper.add(player, remainder);
		}
		return SkillOperations.addSkillXp(player, tracker, skillKey, def, skillXp);
	}

	public static int refundSkill(ServerPlayer player, ResourceKey<SkillDefinition> skillKey) {
		SkillDefinition def = SkillOperations.lookupDefinition(player, skillKey);
		PlayerSkillData tracker = getSkillData(player);
		SkillProgress prog = tracker.getProgress(skillKey);

		if (def == null) {
			SkillOperations.commitLevelChange(player, tracker, skillKey, null, prog, SkillProgress.ZERO);
			return 0;
		}

		int level = prog.level();
		int overflow = prog.xp();
		if (level <= 0 && overflow <= 0) {
			return 0;
		}

		int vanillaRefund = 0;
		for (int lvl = 1; lvl <= level; lvl++) {
			int skillXpForLevel = def.xpCostForLevel(lvl);
			vanillaRefund += def.skillToVanillaXp(skillXpForLevel, lvl - 1);
		}
		if (overflow > 0) {
			vanillaRefund += def.skillToVanillaXp(overflow, level);
		}

		VanillaXpHelper.add(player, vanillaRefund);
		SkillOperations.commitLevelChange(player, tracker, skillKey, def, prog, SkillProgress.ZERO);
		return vanillaRefund;
	}

	public static void setSkillLevel(ServerPlayer player, ResourceKey<SkillDefinition> skillKey, int level) {
		SkillDefinition def = SkillOperations.lookupDefinition(player, skillKey);
		if (def == null) {
			return;
		}
		PlayerSkillData tracker = getSkillData(player);
		SkillProgress before = tracker.getProgress(skillKey);
		int clamped = def.clampLevel(level);
		SkillProgress after = new SkillProgress(clamped, 0);
		SkillOperations.commitLevelChange(player, tracker, skillKey, def, before, after);
	}

	public static void setSkillXp(ServerPlayer player, ResourceKey<SkillDefinition> skillKey, int xp) {
		SkillDefinition def = SkillOperations.lookupDefinition(player, skillKey);
		if (def == null) {
			return;
		}
		PlayerSkillData tracker = getSkillData(player);
		SkillProgress before = tracker.getProgress(skillKey);
		int nextCost = def.xpCostForNextLevel(before.level());
		int clampedXp = nextCost > 0 ? Math.min(xp, nextCost - 1) : 0;
		SkillProgress after = new SkillProgress(before.level(), Math.max(0, clampedXp));
		SkillOperations.commitLevelChange(player, tracker, skillKey, def, before, after);
	}

	public static int addLevelsFree(ServerPlayer player, ResourceKey<SkillDefinition> skillKey, int levels) {
		if (levels <= 0) {
			return 0;
		}
		SkillDefinition def = SkillOperations.lookupDefinition(player, skillKey);
		if (def == null) {
			return 0;
		}
		PlayerSkillData tracker = getSkillData(player);
		SkillProgress before = tracker.getProgress(skillKey);
		int newLevel = def.clampLevel(before.level() + levels);
		int gained = newLevel - before.level();
		if (gained <= 0) {
			return 0;
		}
		SkillProgress after = new SkillProgress(newLevel, 0);
		SkillOperations.commitLevelChange(player, tracker, skillKey, def, before, after);
		return gained;
	}

	public static void resetSkill(ServerPlayer player, ResourceKey<SkillDefinition> skillKey) {
		PlayerSkillData tracker = getSkillData(player);
		SkillDefinition def = SkillOperations.lookupDefinition(player, skillKey);
		SkillProgress before = tracker.getProgress(skillKey);
		if (before.level() <= 0 && before.xp() <= 0) {
			return;
		}
		if (def != null) {
			SkillEffectApplier.firePerkEvents(player, tracker, skillKey, def, before.level(), 0);
		}
		SkillOperations.commitLevelChange(player, tracker, skillKey, def, before, SkillProgress.ZERO);
	}

	public static void resetAll(ServerPlayer player) {
		PlayerSkillData data = getSkillData(player);
		RegistryAccess access = player.level().registryAccess();
		Registry<SkillDefinition> registry = access.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);

		for (var entry : registry.entrySet()) {
			ResourceKey<SkillDefinition> skillKey = entry.getKey();
			SkillDefinition def = entry.getValue();
			int oldLevel = data.getLevel(skillKey);
			if (oldLevel > 0) {
				SkillEffectApplier.firePerkEvents(player, data, skillKey, def, oldLevel, 0);
			}
		}

		data.clearSkills();
		SkillEffectApplier.recomputeAll(player, data, access);
	}

	public static void recomputeAll(ServerPlayer player) {
		SkillEffectApplier.recomputeAll(player, getSkillData(player), player.level().registryAccess());
	}

	// --- Internal accessor ---

	public static PlayerSkillData getSkillData(Player player) {
		return ModSyncedDataKeys.PLAYER_SKILLS.getValue(player);
	}
}
