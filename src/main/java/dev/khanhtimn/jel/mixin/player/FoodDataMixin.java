package dev.khanhtimn.jel.mixin.player;

import dev.khanhtimn.jel.content.skills.Constitution;
import dev.khanhtimn.jel.api.JelTraits;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {

	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;heal(F)V",
					ordinal = 1,
					shift = At.Shift.AFTER
			)
	)
	private void jel$bonusConstitutionRegen(Player player, CallbackInfo ci) {
		float bonus = JelTraits.value(player, Constitution.BONUS_REGEN_TRAIT);
		if (bonus > 0) {
			player.heal(bonus);
		}
	}
}
