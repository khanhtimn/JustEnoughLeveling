package dev.khanhtimn.jel.mixin.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.khanhtimn.jel.misc.JelFoodDataAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

	@Unique
	private static final int VANILLA_SPRINT_THRESHOLD = 6;
	@Unique
	private static final int VANILLA_MAX_FOOD = 20;

	@ModifyExpressionValue(
			method = "hasEnoughFoodToStartSprinting",
			at = @At(
					value = "CONSTANT",
					args = "floatValue=6.0"
			)
	)
	private float jel$sprintThreshold(float original) {
		Player self = (Player) (Object) this;
		int maxFood = ((JelFoodDataAccess) self.getFoodData()).jel$getMaxFoodLevel();
		if (maxFood == VANILLA_MAX_FOOD) {
			return original;
		}
		return (float) Math.ceil((double) VANILLA_SPRINT_THRESHOLD / VANILLA_MAX_FOOD * maxFood);
	}
}
