package dev.khanhtimn.jel.event;

import dev.khanhtimn.jel.core.ModAttributes;
import dev.khanhtimn.jel.misc.JelFoodDataAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.food.FoodData;

public final class FoodEvents {

	private static float respawnFoodRatio = 1.0f;

	public static float getRespawnFoodRatio() {
		return respawnFoodRatio;
	}

	public static void setRespawnFoodRatio(float ratio) {
		respawnFoodRatio = Mth.clamp(ratio, 0.0f, 1.0f);
	}

	public static void scaleFoodOnRespawn(ServerPlayer player) {
		AttributeInstance attr = player.getAttribute(ModAttributes.maxFoodLevel());
		if (attr == null) return;

		int maxFood = (int) attr.getValue();
		int scaledFood = Mth.ceil(maxFood * respawnFoodRatio);
		float scaledSaturation = Math.min(maxFood * respawnFoodRatio, scaledFood);

		FoodData food = player.getFoodData();
		food.setFoodLevel(scaledFood);
		((JelFoodDataAccess) food).jel$setSaturationLevel(scaledSaturation);
	}

	private FoodEvents() {
	}
}
