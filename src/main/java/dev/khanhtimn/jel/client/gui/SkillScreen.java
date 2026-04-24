package dev.khanhtimn.jel.client.gui;

import dev.khanhtimn.jel.client.gui.widget.SkillCard;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.common.VanillaXpHelper;
import dev.khanhtimn.jel.api.JelRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main skill overview screen, showing all registered skills in a grid.
 * Accessible via a tab button on the survival inventory.
 */
public class SkillScreen extends Screen {

	private static final int PANEL_WIDTH = 256;
	private static final int CARD_WIDTH = 120;
	private static final int CARD_HEIGHT = 40;
	private static final int CARD_GAP = 4;
	private static final int COLUMNS = 2;
	private static final int HEADER_HEIGHT = 36;

	private int panelLeft;
	private int panelTop;
	private int panelHeight;

	public SkillScreen() {
		super(Component.translatable("gui.jel.skills"));
	}

	@Override
	protected void init() {
		super.init();

		Player player = Minecraft.getInstance().player;
		if (player == null) return;

		Registry<SkillDefinition> registry = player.level().registryAccess()
				.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);

		// Collect skill entries
		List<Map.Entry<ResourceKey<SkillDefinition>, SkillDefinition>> entries = new ArrayList<>();
		registry.entrySet().forEach(entry ->
				entries.add(Map.entry(entry.getKey(), entry.getValue())));

		// Compute layout
		int rows = (entries.size() + COLUMNS - 1) / COLUMNS;
		this.panelHeight = HEADER_HEIGHT + rows * (CARD_HEIGHT + CARD_GAP) + CARD_GAP + 24;
		this.panelLeft = (this.width - PANEL_WIDTH) / 2;
		this.panelTop = (this.height - this.panelHeight) / 2;

		// Add skill cards — no tracker ref passed; cards read fresh each frame
		for (int i = 0; i < entries.size(); i++) {
			Map.Entry<ResourceKey<SkillDefinition>, SkillDefinition> entry = entries.get(i);
			int col = i % COLUMNS;
			int row = i / COLUMNS;
			int x = panelLeft + 4 + col * (CARD_WIDTH + CARD_GAP);
			int y = panelTop + HEADER_HEIGHT + row * (CARD_HEIGHT + CARD_GAP);

			addRenderableWidget(new SkillCard(
					x, y, CARD_WIDTH, CARD_HEIGHT,
					entry.getKey(),
					entry.getValue()
			));
		}

		// Back to inventory button
		addRenderableWidget(Button.builder(
				Component.translatable("gui.jel.back_to_inventory"),
				btn -> Minecraft.getInstance().setScreen(new InventoryScreen(player))
		).bounds(panelLeft + 4, panelTop + panelHeight - 22, PANEL_WIDTH - 8, 18).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Render a simple translucent overlay instead of the default blurred background.
		// This avoids the 1.21.1 blur shader which only applies to non-widget content.
		graphics.fill(0, 0, this.width, this.height, 0xC0101010);

		// Panel background
		graphics.fill(panelLeft - 2, panelTop - 2,
				panelLeft + PANEL_WIDTH + 2, panelTop + panelHeight + 2,
				0xFF1A1A2E);
		graphics.fill(panelLeft, panelTop,
				panelLeft + PANEL_WIDTH, panelTop + panelHeight,
				0xFF16213E);

		// Title
		graphics.drawCenteredString(
				this.font,
				this.title,
				this.width / 2,
				panelTop + 6,
				0xFFFFFF
		);

		// Vanilla XP info
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			int totalXp = VanillaXpHelper.getTotalXp(player);
			Component xpText = Component.translatable("gui.jel.vanilla_xp",
					player.experienceLevel, totalXp);
			graphics.drawCenteredString(this.font, xpText, this.width / 2,
					panelTop + 20, 0x7CFC00);
		}

		// Separator line
		graphics.fill(panelLeft + 4, panelTop + HEADER_HEIGHT - 2,
				panelLeft + PANEL_WIDTH - 4, panelTop + HEADER_HEIGHT - 1,
				0xFF3A506B);

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
