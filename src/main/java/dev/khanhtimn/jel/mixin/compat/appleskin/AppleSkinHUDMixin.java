package dev.khanhtimn.jel.mixin.compat.appleskin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.khanhtimn.jel.misc.JelFoodDataAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.helpers.TextureHelper;
import com.mojang.blaze3d.systems.RenderSystem;
//? neoforge {
import squeek.appleskin.helpers.HungerHelper;
//?} else {
/*import squeek.appleskin.helpers.FoodHelper;
 *///?}

@IfModLoaded("appleskin")
@Mixin(targets = "squeek.appleskin.client.HUDOverlayHandler")
public abstract class AppleSkinHUDMixin {

	@Unique
	private static final int VANILLA_MAX_FOOD = 20;

	@Unique
	private static int jel$maxFood() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return VANILLA_MAX_FOOD;
		return ((JelFoodDataAccess) mc.player.getFoodData()).jel$getMaxFoodLevel();
	}

	@Unique
	private static float jel$foodBarWidth() {
		int maxFood = jel$maxFood();
		if (maxFood == VANILLA_MAX_FOOD) return 81.0F;
		return Math.min(81.0F, (float) (Math.ceil(maxFood / 2.0) * 8 + 1));
	}

	@Unique
	private static void jel$drawMultiRowExhaustion(
			float exhaustion, float maxExhaustion,
			GuiGraphics guiGraphics, int right, int top
	) {
		int maxFood = jel$maxFood();
		float ratio = Math.clamp(exhaustion / maxExhaustion, 0.0F, 1.0F);

		int totalSlots = (int) Math.ceil(maxFood / 2.0);
		int numRows = (int) Math.ceil(totalSlots / 10.0);

		int totalWidth = 0;
		int[] rowWidths = new int[numRows];
		for (int row = 0; row < numRows; row++) {
			int slotsInRow = Math.min(10, totalSlots - row * 10);
			rowWidths[row] = slotsInRow * 8 + 1;
			totalWidth += rowWidths[row];
		}

		int filledWidth = (int) (ratio * totalWidth);

		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.75F);
		RenderSystem.blendFunc(770, 771);
		for (int row = 0; row < numRows && filledWidth > 0; row++) {
			int rowFill = Math.min(filledWidth, rowWidths[row]);
			guiGraphics.blit(TextureHelper.MOD_ICONS,
					right - rowFill, top - row * 10,
					81 - rowFill, 18,
					rowFill, 9);
			filledWidth -= rowFill;
		}
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	//? neoforge {
	@ModifyExpressionValue(
			method = "drawSaturationOverlay(FFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/gui/GuiGraphics;IIFI)V",
			at = @At(value = "CONSTANT", args = "floatValue=20.0"),
			require = 0
	)
	private static float jel$saturationCap(float original) {
		return (float) jel$maxFood();
	}

	@ModifyExpressionValue(
			method = "drawHungerOverlay(IILnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/gui/GuiGraphics;IIFZI)V",
			at = @At(value = "CONSTANT", args = "intValue=20"),
			require = 0
	)
	private static int jel$hungerCap(int original) {
		return jel$maxFood();
	}

	@ModifyExpressionValue(
			method = "shouldShowEstimatedHealth(Lnet/minecraft/world/entity/player/Player;)Z",
			at = @At(value = "CONSTANT", args = "intValue=18"),
			require = 0
	)
	private static int jel$regenThreshold(int original) {
		int max = jel$maxFood();
		if (max == VANILLA_MAX_FOOD) return original;
		return Math.max(0, max - (int) Math.ceil(2.0 * max / VANILLA_MAX_FOOD));
	}

	@WrapMethod(
			method = "drawExhaustionOverlay(FLnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
			require = 0
	)
	private static void jel$multiRowExhaustion(
			float exhaustion, Player player, GuiGraphics guiGraphics,
			int right, int top, float alpha,
			Operation<Void> original
	) {
		int maxFood = jel$maxFood();
		if (maxFood == VANILLA_MAX_FOOD) {
			original.call(exhaustion, player, guiGraphics, right, top, alpha);
			return;
		}
		float maxExhaustion = HungerHelper.getMaxExhaustion(player);
		jel$drawMultiRowExhaustion(exhaustion, maxExhaustion, guiGraphics, right, top);
	}
	//?} else {
	/*@ModifyExpressionValue(
			method = "drawSaturationOverlay(Lnet/minecraft/client/gui/GuiGraphics;FFLnet/minecraft/client/Minecraft;IIFI)V",
			at = @At(value = "CONSTANT", args = "floatValue=20.0"),
			require = 0
	)
	private float jel$saturationCap(float original) {
		return (float) jel$maxFood();
	}

	@ModifyExpressionValue(
			method = "drawHungerOverlay(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/Minecraft;IIFZI)V",
			at = @At(value = "CONSTANT", args = "intValue=20"),
			require = 0
	)
	private int jel$hungerCap(int original) {
		return jel$maxFood();
	}

	@ModifyExpressionValue(
			method = "shouldShowEstimatedHealth(Lnet/minecraft/world/entity/player/Player;I)Z",
			at = @At(value = "CONSTANT", args = "intValue=18"),
			require = 0
	)
	private int jel$regenThreshold(int original) {
		int max = jel$maxFood();
		if (max == VANILLA_MAX_FOOD) return original;
		return Math.max(0, max - (int) Math.ceil(2.0 * max / VANILLA_MAX_FOOD));
	}

	@WrapMethod(
			method = "drawExhaustionOverlay(Lnet/minecraft/client/gui/GuiGraphics;FIIF)V",
			require = 0,
			remap = false
	)
	private void jel$multiRowExhaustion(
			GuiGraphics guiGraphics, float exhaustion,
			int right, int top, float alpha,
			Operation<Void> original
	) {
		int maxFood = jel$maxFood();
		if (maxFood == VANILLA_MAX_FOOD) {
			original.call(guiGraphics, exhaustion, right, top, alpha);
			return;
		}
		float maxExhaustion = FoodHelper.MAX_EXHAUSTION * maxFood / (float) VANILLA_MAX_FOOD;
		jel$drawMultiRowExhaustion(exhaustion, maxExhaustion, guiGraphics, right, top);
	}
	*///?}
}
