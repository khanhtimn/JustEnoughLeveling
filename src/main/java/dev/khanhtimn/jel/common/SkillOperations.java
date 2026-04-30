package dev.khanhtimn.jel.common;

import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.loot.JelLootContexts;
import dev.khanhtimn.jel.api.skill.Requirement;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.SkillProgress;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Internal orchestration extracted from the public {@code JelSkills} facade.
 * Not part of the public API.
 */
public final class SkillOperations {

    private SkillOperations() {
    }

    /**
     * The single source of truth for committing a level change.
     * All methods that mutate skill progress route through here.
     */
    public static void commitLevelChange(
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

    public static boolean tryLevelUpOnceInternal(
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

        // Requirement gate: check before spending vanilla XP
        int targetLevel = currentLevel + 1;
        Optional<Requirement> req = def.requirementForLevel(targetLevel);
        if (req.isPresent()) {
            LootContext ctx = JelLootContexts.entity(
                    player.serverLevel(), player, currentLevel);
            if (!req.get().test(ctx)) {
                return false;
            }
        }

        int currentSkillXp = current.xp();
        int neededSkillXp = costNext - currentSkillXp;

        if (neededSkillXp > 0) {
            int vanillaCost = def.skillToVanillaXp(neededSkillXp, currentLevel);
            if (!VanillaXpHelper.subtract(player, vanillaCost)) {
                return false;
            }

            return addSkillXp(player, tracker, skillKey, def, neededSkillXp) > 0;
        }

        SkillProgress after = applySkillXp(current, def, 0, player);
        if (after.level() > currentLevel) {
            commitLevelChange(player, tracker, skillKey, def, current, after);
            return true;
        }
        return false;
    }

    public static int addSkillXp(
            ServerPlayer player,
            PlayerSkillData tracker,
            ResourceKey<SkillDefinition> skillKey,
            SkillDefinition def,
            int skillXpDelta
    ) {
        if (skillXpDelta <= 0) {
            return 0;
        }

        SkillProgress before = tracker.getProgress(skillKey);
        SkillProgress after = applySkillXp(before, def, skillXpDelta, player);

        int levelsGained = after.level() - before.level();
        commitLevelChange(player, tracker, skillKey, def, before, after);
        return levelsGained;
    }

    /**
     * @param player if non-null, requirements are checked and XP is capped
     *               at the requirement boundary. Null bypasses requirement
     *               checks (used by admin commands).
     */
    public static SkillProgress applySkillXp(
            SkillProgress current, SkillDefinition def,
            int skillXpDelta, @Nullable ServerPlayer player
    ) {
        int level = current.level();
        int xp = Math.max(0, current.xp() + skillXpDelta);

        while (level < def.maxLevel()) {
            int costNext = def.xpCostForNextLevel(level);
            if (costNext <= 0 || xp < costNext) {
                break;
            }

            // Requirement gate
            if (player != null) {
                int targetLevel = level + 1;
                Optional<Requirement> req = def.requirementForLevel(targetLevel);
                if (req.isPresent()) {
                    LootContext ctx = JelLootContexts.entity(
                            player.serverLevel(), player, level);
                    if (!req.get().test(ctx)) {
                        xp = Math.min(xp, costNext - 1);
                        break;
                    }
                }
            }

            xp -= costNext;
            level++;
        }

        return new SkillProgress(level, xp);
    }

    public static SkillDefinition lookupDefinition(ServerPlayer player, ResourceKey<SkillDefinition> skillKey) {
        RegistryAccess access = player.level().registryAccess();
        Registry<SkillDefinition> registry = access.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);
        return registry.get(skillKey);
    }
}
