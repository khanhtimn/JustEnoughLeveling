package dev.khanhtimn.jel.common.skill.impl;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class VanillaXpHelper {

	private VanillaXpHelper() {
	}

	public static int getTotalXp(Player player) {
		int level = player.experienceLevel;
		float progress = player.experienceProgress;
		int xpForCurrentLevel = xpNeededForLevel(level);
		int xp = xpAtLevel(level);
		xp += (int) (progress * xpForCurrentLevel);
		return xp;
	}

	public static void setTotalXp(ServerPlayer player, int totalXp) {
		totalXp = Math.max(0, totalXp);
		player.experienceLevel = 0;
		player.experienceProgress = 0.0F;
		player.totalExperience = 0;
		player.giveExperiencePoints(totalXp);
	}

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

	public static void add(ServerPlayer player, int amount) {
		if (amount <= 0) {
			return;
		}
		player.giveExperiencePoints(amount);
	}

	public static int xpNeededForLevel(int level) {
		if (level >= 30) {
			return 112 + (level - 30) * 9;
		} else if (level >= 15) {
			return 37 + (level - 15) * 5;
		} else {
			return 7 + level * 2;
		}
	}

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
