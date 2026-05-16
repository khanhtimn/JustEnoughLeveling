package dev.khanhtimn.jel.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.khanhtimn.jel.misc.JelFoodDataAccess;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

//? fabric {
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

@Mixin(Gui.class)
public abstract class GuiMixin {

	@Unique
	private static final int VANILLA_MAX_FOOD = 20;

	@Unique
	private static int jel$maxFood(Player player) {
		return ((JelFoodDataAccess) player.getFoodData()).jel$getMaxFoodLevel();
	}

	@ModifyConstant(method = "renderFood", constant = @Constant(intValue = 10))
	private int jel$expandLoopBound(int original, @Local(argsOnly = true, ordinal = 0) Player player) {
		int maxFood = jel$maxFood(player);
		if (maxFood == VANILLA_MAX_FOOD) return original;
		return Mth.ceil((float) maxFood / 2);
	}

	@WrapOperation(
			method = "renderFood",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V")
	)
	private void jel$adjustMultiRowCoords(
			GuiGraphics instance, ResourceLocation sprite, int x, int y, int w, int h,
			Operation<Void> original,
			@Local(argsOnly = true, ordinal = 0) Player player,
			@Local(ordinal = 3) int j
	) {
		int maxFood = jel$maxFood(player);
		if (maxFood > VANILLA_MAX_FOOD) {
			int col = j % 10;
			x += (j - col) * 8;
			y -= (j / 10) * 10;
		}
		original.call(instance, sprite, x, y, w, h);
	}

	@ModifyExpressionValue(
			method = "renderFood",
			at = @At(value = "CONSTANT", args = "intValue=3", ordinal = 0)
	)
	private int jel$normalizeJitterMultiplier(int original, @Local(ordinal = 0) FoodData foodData) {
		int maxFood = ((JelFoodDataAccess) foodData).jel$getMaxFoodLevel();
		if (maxFood <= VANILLA_MAX_FOOD) return original;
		int foodLevel = foodData.getFoodLevel();
		if (foodLevel == 0) return original;
		int normalizedLevel = Math.min(foodLevel, VANILLA_MAX_FOOD);
		return Math.max(1, normalizedLevel * original / foodLevel);
	}

	//? neoforge {
	/*@ModifyConstant(method = "renderFoodLevel", constant = @Constant(intValue = 10))
	private int jel$adjustRightHeight(int original, @Local(ordinal = 0) Player player) {
		int maxFood = jel$maxFood(player);
		if (maxFood <= VANILLA_MAX_FOOD) return original;
		int totalSlots = Mth.ceil((float) maxFood / 2);
		int rows = Mth.ceil((float) totalSlots / 10);
		return rows * 10;
	}
	*///?} else {
	@Inject(
			method = "renderPlayerHealth",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V",
					shift = At.Shift.AFTER)
	)
	private void jel$adjustFabricHeight(GuiGraphics graphics, CallbackInfo ci, @Local(ordinal = 0) Player player, @Local(ordinal = 8) LocalIntRef r) {
		int maxFood = jel$maxFood(player);
		if (maxFood <= VANILLA_MAX_FOOD) return;
		int totalSlots = Mth.ceil((float) maxFood / 2);
		int extraRows = (totalSlots - 1) / 10;
		r.set(r.get() - extraRows * 10);
	}
	//?}
}
