package dev.khanhtimn.jel.common.skill;

import net.minecraft.server.level.ServerPlayer;

/**
 * Utility methods for querying and manipulating a player's vanilla experience
 * bar.
 * <p>
 * This is pure Minecraft math with no JEL domain knowledge.
 */
public final class VanillaXpHelper {

    private VanillaXpHelper() {
    }

    /**
     * Total XP points currently represented by the player's level and
     * fractional progress bar.
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
     * points. Resets level/progress and re-applies via giveExperiencePoints.
     */
    public static void setTotalXp(ServerPlayer player, int totalXp) {
        totalXp = Math.max(0, totalXp);
        player.experienceLevel = 0;
        player.experienceProgress = 0.0F;
        player.totalExperience = 0;
        player.giveExperiencePoints(totalXp);
    }

    /**
     * Try to subtract exactly {@code amount} vanilla XP points from the player.
     *
     * @return true if XP was successfully subtracted; false if not enough XP.
     */
    public static boolean subtract(ServerPlayer player, int amount) {
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
     * Add vanilla XP points to the player. Uses giveExperiencePoints directly
     * for efficiency (avoids full XP bar reset).
     */
    public static void add(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return;
        }
        player.giveExperiencePoints(amount);
    }

    /**
     * XP needed to go from {@code level} to {@code level + 1} (vanilla curve).
     */
    public static int xpNeededForLevel(int level) {
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
    public static int xpAtLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level >= 32) {
            // 4.5*level^2 - 162.5*level + 2220
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level >= 17) {
            // 2.5*level^2 - 40.5*level + 360
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            // level^2 + 6*level
            return level * level + 6 * level;
        }
    }
}
