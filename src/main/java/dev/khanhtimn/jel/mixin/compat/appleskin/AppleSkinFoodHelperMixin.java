package dev.khanhtimn.jel.mixin.compat.appleskin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.khanhtimn.jel.misc.JelFoodDataAccess;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("appleskin")
@Mixin(targets = "squeek.appleskin.helpers.FoodHelper")
public abstract class AppleSkinFoodHelperMixin {

	@Unique
	private static final int VANILLA_MAX_FOOD = 20;

	@Unique
	private static int jel$maxFood() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return VANILLA_MAX_FOOD;
		return ((JelFoodDataAccess) mc.player.getFoodData()).jel$getMaxFoodLevel();
	}

	@Unique
	private static int jel$regenThreshold() {
		int max = jel$maxFood();
		if (max == VANILLA_MAX_FOOD) return 18;
		return Math.max(0, max - (int) Math.ceil(2.0 * max / VANILLA_MAX_FOOD));
	}

	@ModifyExpressionValue(
			method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/food/FoodProperties;)F",
			at = @At(value = "CONSTANT", args = "intValue=20"),
			require = 0
	)
	private static int jel$healthIncrementFoodCap(int original) {
		return jel$maxFood();
	}

	@ModifyExpressionValue(
			method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/food/FoodProperties;)F",
			at = @At(value = "CONSTANT", args = "floatValue=18.0"),
			require = 0
	)
	private static float jel$healthIncrementRegenThreshold(float original) {
		return (float) jel$regenThreshold();
	}

	@ModifyExpressionValue(
			method = "getEstimatedHealthIncrement(IFF)F",
			at = @At(value = "CONSTANT", args = "intValue=18"),
			require = 0
	)
	private static int jel$simRegenThreshold(int original) {
		return jel$regenThreshold();
	}

	@ModifyExpressionValue(
			method = "getEstimatedHealthIncrement(IFF)F",
			at = @At(value = "CONSTANT", args = "intValue=20"),
			require = 0
	)
	private static int jel$simFastRegenThreshold(int original) {
		return jel$maxFood();
	}
}
