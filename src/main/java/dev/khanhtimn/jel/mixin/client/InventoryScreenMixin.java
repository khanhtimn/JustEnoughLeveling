package dev.khanhtimn.jel.mixin.client;

import dev.khanhtimn.jel.client.gui.widget.SkillTabButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends Screen {

	private InventoryScreenMixin() {
		super(null);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void jel$addSkillTab(CallbackInfo ci) {
		InventoryScreen screen = (InventoryScreen) (Object) this;
		addRenderableWidget(new SkillTabButton(screen.leftPos, screen.topPos, true));
	}
}
