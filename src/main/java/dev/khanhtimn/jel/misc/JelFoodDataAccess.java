package dev.khanhtimn.jel.misc;

import net.minecraft.world.entity.player.Player;

public interface JelFoodDataAccess {
	void jel$setPlayer(Player player);

	int jel$getMaxFoodLevel();

	void jel$setSaturationLevel(float saturation);
}
