package dev.khanhtimn.jel.mixin.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.core.ModAttributes;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Constitution;
import dev.khanhtimn.jel.misc.JelFoodDataAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements JelFoodDataAccess {

	@Shadow
	private int foodLevel;

	@Shadow
	private float saturationLevel;

	@Unique
	private Player jel$player;

	@Override
	public void jel$setPlayer(Player player) {
		this.jel$player = player;
	}

	@Override
	public int jel$getMaxFoodLevel() {
		return jel$maxFood();
	}

	@Override
	public void jel$setSaturationLevel(float saturation) {
		this.saturationLevel = saturation;
	}

	@Unique
	private static final int VANILLA_MAX_FOOD = 20;

	@Unique
	private int jel$maxFood() {
		if (jel$player == null) {
			return VANILLA_MAX_FOOD;
		}
		AttributeInstance inst = jel$player.getAttribute(ModAttributes.maxFoodLevel());
		if (inst == null) {
			return VANILLA_MAX_FOOD;
		}
		return (int) inst.getValue();
	}

	@Unique
	private float jel$exhaustionScale() {
		int max = jel$maxFood();
		return max <= 0 ? 1f : (float) max / VANILLA_MAX_FOOD;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void jel$capturePlayer(Player player, CallbackInfo ci) {
		this.jel$player = player;
		int max = jel$maxFood();
		if (this.foodLevel > max) {
			this.foodLevel = max;
		}
	}

	@WrapOperation(
			method = "add",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/Mth;clamp(III)I"
			)
	)
	private int jel$clampFoodLevel(int value, int min, int max, Operation<Integer> original) {
		if (jel$player != null
				&& JelSkills.isBranch(jel$player, ModSkills.CONSTITUTION, Constitution.SUSTENANCE_BRANCH)) {
			float nourishing = JelTraits.value(jel$player, Constitution.NOURISHING_BONUS);
			if (nourishing > 0) {
				int rawFood = value - this.foodLevel;
				if (rawFood > 0) {
					value += (int) (rawFood * nourishing);
				}
			}
		}
		return original.call(value, min, jel$maxFood());
	}

	@WrapOperation(
			method = "add",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/Mth;clamp(FFF)F"
			)
	)
	private float jel$clampSaturation(float value, float min, float max, Operation<Float> original) {
		float result = original.call(value, min, (float) jel$maxFood());
		// Bottomless Appetite: saturation always fills to max food level
		if (jel$player != null
				&& JelSkills.isBranch(jel$player, ModSkills.CONSTITUTION, Constitution.SUSTENANCE_BRANCH)
				&& JelTraits.has(jel$player, Constitution.BOTTOMLESS_APPETITE)) {
			return (float) jel$maxFood();
		}
		return result;
	}

	@ModifyExpressionValue(
			method = "needsFood",
			at = @At(
					value = "CONSTANT",
					args = "intValue=20"
			)
	)
	private int jel$needsFoodMax(int original) {
		return jel$maxFood();
	}

	@ModifyExpressionValue(
			method = "tick",
			at = @At(
					value = "CONSTANT",
					args = "floatValue=4.0",
					ordinal = 0
			)
	)
	private float jel$exhaustionThreshold(float original) {
		return original * jel$exhaustionScale();
	}

	@ModifyExpressionValue(
			method = "tick",
			at = @At(
					value = "CONSTANT",
					args = "floatValue=4.0",
					ordinal = 1
			)
	)
	private float jel$exhaustionDecrement(float original) {
		return original * jel$exhaustionScale();
	}

	@ModifyExpressionValue(
			method = "tick",
			at = @At(
					value = "CONSTANT",
					args = "intValue=20"
			)
	)
	private int jel$saturatedRegenThreshold(int original) {
		return jel$maxFood();
	}

	@ModifyExpressionValue(
			method = "tick",
			at = @At(
					value = "CONSTANT",
					args = "intValue=18"
			)
	)
	private int jel$slowRegenThreshold(int original) {
		int max = jel$maxFood();
		return Math.max(0, max - Mth.ceil(2f * max / VANILLA_MAX_FOOD));
	}

	@ModifyExpressionValue(
			method = "tick",
			at = @At(
					value = "CONSTANT",
					args = "floatValue=6.0",
					ordinal = 0
			)
	)
	private float jel$saturatedHealMinSaturation(float original) {
		return original * jel$exhaustionScale();
	}

	@ModifyExpressionValue(
			method = "tick",
			at = @At(
					value = "CONSTANT",
					args = "floatValue=6.0",
					ordinal = 1
			)
	)
	private float jel$slowHealExhaustion(float original) {
		return original * jel$exhaustionScale();
	}

	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;heal(F)V",
					ordinal = 1,
					shift = At.Shift.AFTER
			)
	)
	private void jel$healMixin(Player player, CallbackInfo ci) {
		float bonus = JelTraits.value(player, Constitution.REGEN_BONUS);
		if (bonus > 0) {
			player.heal(bonus);
		}
	}

	// Feast: after eating FoodProperties, grant Strength I
	@Inject(method = "eat(Lnet/minecraft/world/food/FoodProperties;)V", at = @At("TAIL"))
	private void jel$feastOnEat(FoodProperties props, CallbackInfo ci) {
		if (jel$player == null || jel$player.level().isClientSide()) return;
		if (!JelSkills.isBranch(jel$player, ModSkills.CONSTITUTION, Constitution.SUSTENANCE_BRANCH)) return;

		int duration = (int) JelTraits.value(jel$player, Constitution.FEAST_DURATION);
		if (duration > 0) {
			jel$player.addEffect(new MobEffectInstance(
					MobEffects.DAMAGE_BOOST, duration, 0, true, false, true));
		}

		// Respite: bonus saturation from eating
		float respite = JelTraits.value(jel$player, Constitution.RESPITE_BONUS);
		if (respite > 0) {
			float bonusSat = this.saturationLevel * respite;
			this.saturationLevel = Math.min(this.saturationLevel + bonusSat, (float) jel$maxFood());
		}
	}

}
