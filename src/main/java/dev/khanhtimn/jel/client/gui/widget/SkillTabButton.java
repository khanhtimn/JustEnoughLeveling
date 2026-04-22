package dev.khanhtimn.jel.client.gui.widget;

import dev.khanhtimn.jel.client.gui.SkillScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

/**
 * Tab button injected into the survival inventory screen.
 * When clicked, opens the {@link SkillScreen}.
 * <p>
 * Also rendered on the SkillScreen itself as a "return to inventory" tab.
 */
public class SkillTabButton extends AbstractWidget {

	private static final int TAB_WIDTH = 28;
	private static final int TAB_HEIGHT = 26;

	private final boolean isOnInventory;

	/**
	 * Creates a tab button positioned on the right edge of the inventory screen.
	 *
	 * @param guiLeft       the left x of the inventory screen panel
	 * @param guiTop        the top y of the inventory screen panel
	 * @param isOnInventory true if this tab is on the InventoryScreen, false if on SkillScreen
	 */
	public SkillTabButton(int guiLeft, int guiTop, boolean isOnInventory) {
		super(guiLeft + 176, guiTop + 4, TAB_WIDTH, TAB_HEIGHT,
				isOnInventory
						? Component.translatable("gui.jel.skills_tab")
						: Component.translatable("gui.jel.inventory_tab"));
		this.isOnInventory = isOnInventory;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Tab background
		boolean hovered = isHovered();
		int bgColor = isOnInventory
				? (hovered ? 0xFF2C3E50 : 0xFF1E293B)
				: (hovered ? 0xFF3A506B : 0xFF2C3E50);

		graphics.fill(getX(), getY(), getX() + width, getY() + height, bgColor);

		// Border accent
		int borderColor = isOnInventory ? 0xFF3498DB : 0xFF7F8C8D;
		graphics.fill(getX(), getY(), getX() + 2, getY() + height, borderColor);

		// Icon
		ItemStack icon = isOnInventory
				? new ItemStack(Items.EXPERIENCE_BOTTLE)
				: new ItemStack(Items.CHEST);
		graphics.renderItem(icon, getX() + 6, getY() + 5);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		Minecraft mc = Minecraft.getInstance();
		if (isOnInventory) {
			mc.setScreen(new SkillScreen());
		} else {
			if (mc.player != null) {
				mc.setScreen(new InventoryScreen(mc.player));
			}
		}
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
