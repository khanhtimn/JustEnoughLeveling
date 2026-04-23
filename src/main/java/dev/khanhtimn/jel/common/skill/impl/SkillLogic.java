package dev.khanhtimn.jel.common.skill.impl;

import dev.khanhtimn.jel.core.ModRegistries;
import dev.khanhtimn.jel.core.ModSyncedDataKeys;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Orchestrator for skill operations: leveling, XP gain, refunds.
 * <p>
 * Enforces all game rules (max level, XP formulas, effect recomputation) and is
 * the <b>only</b> external-facing API for mutating skill state. Do not modify
 * {@link PlayerSkillData} directly from outside this package.
 */
public final class SkillLogic {

    private SkillLogic() {
    }

    /**
     * Get the live {@link PlayerSkillData} for a player. On server:
     * authoritative copy, mutations trigger Framework sync. On client: synced
     * read-only copy.
     */
    public static PlayerSkillData getSkills(Player player) {
        return ModSyncedDataKeys.PLAYER_SKILLS.getValue(player);
    }

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

    public static int tryLevelUp(
            ServerPlayer player,
            PlayerSkillData tracker,
            ResourceKey<SkillDefinition> skillKey,
            int levelsToGain
    ) {
        if (levelsToGain <= 0) {
            return 0;
        }

        SkillDefinition def = lookupDefinition(player, skillKey);
        if (def == null) {
            return 0;
        }

        int gained = 0;
        for (int i = 0; i < levelsToGain; i++) {
            if (!tryLevelUpOnceInternal(player, tracker, skillKey, def)) {
                break;
            }
            gained++;
        }
        return gained;
    }

    public static int addSkillXp(
            ServerPlayer player,
            PlayerSkillData tracker,
            ResourceKey<SkillDefinition> skillKey,
            int skillXpDelta
    ) {
        if (skillXpDelta <= 0) {
            return 0;
        }

        SkillDefinition def = lookupDefinition(player, skillKey);
        if (def == null) {
            return 0;
        }

        SkillProgress before = tracker.getProgress(skillKey);
        SkillProgress after = applySkillXp(before, def, skillXpDelta);

        int levelsGained = after.level() - before.level();
        commitLevelChange(player, tracker, skillKey, def, before, after);
        return levelsGained;
    }

    public static int addSkillXpFromVanilla(
            ServerPlayer player,
            PlayerSkillData tracker,
            ResourceKey<SkillDefinition> skillKey,
            int maxVanillaXpToSpend
    ) {
        if (maxVanillaXpToSpend <= 0) {
            return 0;
        }

        SkillDefinition def = lookupDefinition(player, skillKey);
        if (def == null) {
            return 0;
        }

        int available = VanillaXpHelper.getTotalXp(player);
        if (available <= 0) {
            return 0;
        }

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

        return addSkillXp(player, tracker, skillKey, skillXp);
    }

    public static int refundSkill(
            ServerPlayer player,
            PlayerSkillData tracker,
            ResourceKey<SkillDefinition> skillKey
    ) {
        SkillDefinition def = lookupDefinition(player, skillKey);
        SkillProgress prog = tracker.getProgress(skillKey);

        if (def == null) {
            commitLevelChange(player, tracker, skillKey, null, prog, SkillProgress.ZERO);
            return 0;
        }

        int level = prog.level();
        int overflow = prog.xp();
        if (level <= 0 && overflow <= 0) {
            return 0;
        }

        // Precise per-level refund: uses the conversion rate the player
        // was at when they earned each level, not their current (worst) rate.
        int vanillaRefund = 0;
        for (int lvl = 1; lvl <= level; lvl++) {
            int skillXpForLevel = def.xpCostForLevel(lvl);
            // When earning level lvl, the player was at skill level (lvl - 1)
            vanillaRefund += def.skillToVanillaXp(skillXpForLevel, lvl - 1);
        }
        if (overflow > 0) {
            vanillaRefund += def.skillToVanillaXp(overflow, level);
        }

        VanillaXpHelper.add(player, vanillaRefund);

        commitLevelChange(player, tracker, skillKey, def, prog, SkillProgress.ZERO);

        return vanillaRefund;
    }

    public static void resetAll(ServerPlayer player) {
        PlayerSkillData data = getSkills(player);
        RegistryAccess access = player.level().registryAccess();
        Registry<SkillDefinition> registry = access.registryOrThrow(ModRegistries.SKILL_REGISTRY_KEY);

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

    public static void recomputeAll(
            ServerPlayer player,
            PlayerSkillData tracker,
            RegistryAccess access
    ) {
        SkillEffectApplier.recomputeAll(player, tracker, access);
    }

    /**
     * The <b>single source of truth</b> for committing a level change.
     * <p>
     * All methods that mutate skill progress route through this method. It
     * handles:
     * <ol>
     * <li>Updating the progress in the tracker</li>
     * <li>Recomputing STATE perks + attributes</li>
     * <li>Firing EVENT perks for crossed thresholds</li>
     * </ol>
     *
     * @param def may be null if the skill definition was deleted/missing
     */
    private static void commitLevelChange(
            ServerPlayer player,
            PlayerSkillData tracker,
            ResourceKey<SkillDefinition> skillKey,
            SkillDefinition def,
            SkillProgress oldProgress,
            SkillProgress newProgress
    ) {
        tracker.setProgress(skillKey, newProgress);
        SkillEffectApplier.recomputeAll(player, tracker, player.level().registryAccess());

        int oldLevel = oldProgress.level();
        int newLevel = newProgress.level();
        if (def != null && oldLevel != newLevel) {
            SkillEffectApplier.firePerkEvents(player, tracker, skillKey, def, oldLevel, newLevel);
        }
    }

    private static boolean tryLevelUpOnceInternal(
            ServerPlayer player,
            PlayerSkillData tracker,
            ResourceKey<SkillDefinition> skillKey,
            SkillDefinition def
    ) {
        SkillProgress current = tracker.getProgress(skillKey);
        int currentLevel = current.level();

        if (def.isMaxLevel(currentLevel)) {
            return false;
        }

        int costNext = def.xpCostForNextLevel(currentLevel);
        if (costNext <= 0) {
            return false;
        }

        int currentSkillXp = current.xp();
        int neededSkillXp = costNext - currentSkillXp;

        if (neededSkillXp > 0) {
            int vanillaCost = def.skillToVanillaXp(neededSkillXp, currentLevel);
            if (!VanillaXpHelper.subtract(player, vanillaCost)) {
                return false;
            }

            return addSkillXp(player, tracker, skillKey, neededSkillXp) > 0;
        }

        SkillProgress after = applySkillXp(current, def, 0);
        if (after.level() > currentLevel) {
            commitLevelChange(player, tracker, skillKey, def, current, after);
            return true;
        }
        return false;
    }

    static SkillProgress applySkillXp(SkillProgress current, SkillDefinition def, int skillXpDelta) {
        int level = current.level();
        int xp = Math.max(0, current.xp() + skillXpDelta);

        while (level < def.maxLevel()) {
            int costNext = def.xpCostForNextLevel(level);
            if (costNext <= 0 || xp < costNext) {
                break;
            }
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
