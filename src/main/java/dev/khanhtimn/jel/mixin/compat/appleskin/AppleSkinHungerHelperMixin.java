package dev.khanhtimn.jel.mixin.compat.appleskin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.khanhtimn.jel.misc.JelFoodDataAccess;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@IfModLoaded("appleskin")
@Pseudo
@Mixin(targets = "squeek.appleskin.helpers.HungerHelper")
public abstract class AppleSkinHungerHelperMixin {

	@WrapMethod(method = "getMaxExhaustion", require = 0)
	private static float jel$scaleMaxExhaustion(Player player, Operation<Float> original) {
		float base = original.call(player);
		int maxFood = ((JelFoodDataAccess) player.getFoodData()).jel$getMaxFoodLevel();
		if (maxFood == 20) return base;
		return base * maxFood / 20f;
	}
}
