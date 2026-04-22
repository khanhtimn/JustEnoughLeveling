package dev.khanhtimn.jel.client;

import com.mrcrayfish.framework.api.event.ScreenEvents;

import dev.khanhtimn.jel.client.gui.widget.SkillTabButton;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public final class ClientBootstrap {

	public static void init() {
		ScreenEvents.MODIFY_WIDGETS.register((screen, widgets, add, remove) -> {
			if (screen instanceof InventoryScreen inventoryScreen) {
				add.accept(new SkillTabButton(inventoryScreen.leftPos, inventoryScreen.topPos, true));
			}
		});
	}

	private ClientBootstrap() {
	}
}
