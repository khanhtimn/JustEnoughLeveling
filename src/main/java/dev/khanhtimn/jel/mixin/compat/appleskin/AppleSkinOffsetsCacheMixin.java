package dev.khanhtimn.jel.mixin.compat.appleskin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.khanhtimn.jel.misc.JelFoodDataAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import squeek.appleskin.util.IntPoint;

import java.util.Vector;

@IfModLoaded("appleskin")
@Mixin(targets = "squeek.appleskin.client.HUDOverlayHandler$OffsetsCache")
public abstract class AppleSkinOffsetsCacheMixin {

	@Shadow
	@Final
	protected Vector<IntPoint> foodBarOffsets;

	@Unique
	private static final int VANILLA_MAX_FOOD = 20;

	@Unique
	private static int jel$maxFood() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return VANILLA_MAX_FOOD;
		}
		return ((JelFoodDataAccess) mc.player.getFoodData()).jel$getMaxFoodLevel();
	}

	@Inject(method = "generate", at = @At("TAIL"), require = 0)
	private void jel$fixFoodBarOffsets(int guiTicks, Player player, CallbackInfo ci) {
		int maxFood = jel$maxFood();
		if (maxFood == VANILLA_MAX_FOOD) {
			return;
		}

		int totalSlots = Mth.ceil((float) maxFood / 2);
		foodBarOffsets.setSize(totalSlots);

		for (int i = 0; i < totalSlots; i++) {
			int col = i % 10;
			int row = i / 10;

			IntPoint point = foodBarOffsets.get(i);
			if (point == null) {
				point = new IntPoint();
				foodBarOffsets.set(i, point);
			}
			point.x = -(col * 8) - 9;
			point.y = -(row * 10);
		}
	}
}
