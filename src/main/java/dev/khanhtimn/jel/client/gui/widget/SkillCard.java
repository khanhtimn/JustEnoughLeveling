package dev.khanhtimn.jel.client.gui.widget;

import dev.khanhtimn.jel.core.ModNetwork;
import dev.khanhtimn.jel.network.message.MessageLevelUpSkill;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.skill.SkillProgress;
import dev.khanhtimn.jel.common.VanillaXpHelper;
import dev.khanhtimn.jel.common.PlayerSkillData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A single skill card widget displayed in the skill screen grid.
 * Shows icon, name, level, XP bar, and a level-up button.
 * <p>
 * Reads the live {@link PlayerSkillData} fresh each frame via
 * {@link JelSkills#getSkillData} so that synced updates from the
 * server are reflected immediately.
 */
public class SkillCard extends AbstractWidget {

	private static final int ICON_SIZE = 16;
	private static final int BAR_HEIGHT = 6;
	private static final int LEVEL_UP_BTN_SIZE = 16;

	private final ResourceKey<SkillDefinition> skillKey;
	private final SkillDefinition definition;
	private final ItemStack iconStack;

	public SkillCard(int x, int y, int width, int height,
	                 ResourceKey<SkillDefinition> skillKey,
	                 SkillDefinition definition) {
		super(x, y, width, height, definition.name());
		this.skillKey = skillKey;
		this.definition = definition;
		this.iconStack = new ItemStack(
				BuiltInRegistries.ITEM.get(definition.icon())
		);
	}

	/**
	 * Get the live tracker from the local player (re-read every frame).
	 */
	private PlayerSkillData liveTracker() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return new PlayerSkillData();
		return JelSkills.getSkillData(mc.player);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		PlayerSkillData tracker = liveTracker();
		SkillProgress progress = tracker.getProgress(skillKey);
		int level = progress.level();
		int xp = progress.xp();
		int maxLevel = definition.maxLevel();
		int xpForNext = definition.xpCostForNextLevel(level);
		boolean isMaxed = definition.isMaxLevel(level);

		// Card background
		int bgColor = isHovered() ? 0xFF2C3E50 : 0xFF1E293B;
		graphics.fill(getX(), getY(), getX() + width, getY() + height, bgColor);

		// Border with skill color
		int borderColor = definition.color() | 0xFF000000;
		graphics.fill(getX(), getY(), getX() + 2, getY() + height, borderColor);

		// Icon (cached in constructor)
		graphics.renderItem(this.iconStack, getX() + 4, getY() + 4);

		// Skill name
		graphics.drawString(
				Minecraft.getInstance().font,
				definition.name(),
				getX() + 4 + ICON_SIZE + 4,
				getY() + 4,
				definition.color() | 0xFF000000,
				true
		);

		// Level text
		String levelText = "Lv. " + level + "/" + maxLevel;
		graphics.drawString(
				Minecraft.getInstance().font,
				levelText,
				getX() + 4 + ICON_SIZE + 4,
				getY() + 16,
				isMaxed ? 0xFFD700 : 0xAAAAAA,
				true
		);

		// XP progress bar
		int barX = getX() + 4;
		int barY = getY() + height - BAR_HEIGHT - 4;
		int barWidth = width - 8 - (isMaxed ? 0 : LEVEL_UP_BTN_SIZE + 4);

		// Bar background
		graphics.fill(barX, barY, barX + barWidth, barY + BAR_HEIGHT, 0xFF0A0A0A);

		if (!isMaxed && xpForNext > 0) {
			// Bar fill
			float ratio = Math.min(1.0f, (float) xp / xpForNext);
			int fillWidth = (int) (barWidth * ratio);
			if (fillWidth > 0) {
				graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, borderColor);
			}

			// XP text on bar
			String xpText = xp + "/" + xpForNext;
			int textWidth = Minecraft.getInstance().font.width(xpText);
			if (textWidth < barWidth - 2) {
				graphics.drawString(
						Minecraft.getInstance().font,
						xpText,
						barX + (barWidth - textWidth) / 2,
						barY - 1,
						0xCCCCCC,
						true
				);
			}
		} else if (isMaxed) {
			// Full gold bar for maxed skills
			graphics.fill(barX, barY, barX + barWidth, barY + BAR_HEIGHT, 0xFFFFD700);
			String maxText = "MAX";
			int textWidth = Minecraft.getInstance().font.width(maxText);
			graphics.drawString(
					Minecraft.getInstance().font,
					maxText,
					barX + (barWidth - textWidth) / 2,
					barY - 1,
					0xFFD700,
					true
			);
		}

		// Level-up button
		if (!isMaxed) {
			int btnX = getX() + width - LEVEL_UP_BTN_SIZE - 4;
			int btnY = getY() + height - LEVEL_UP_BTN_SIZE - 4;
			boolean canAfford = canAffordLevelUp(tracker, level);
			int btnColor = canAfford ? 0xFF2ECC71 : 0xFF555555;
			boolean btnHovered = mouseX >= btnX && mouseX < btnX + LEVEL_UP_BTN_SIZE
					&& mouseY >= btnY && mouseY < btnY + LEVEL_UP_BTN_SIZE;
			if (btnHovered && canAfford) {
				btnColor = 0xFF27AE60;
			}
			graphics.fill(btnX, btnY, btnX + LEVEL_UP_BTN_SIZE, btnY + LEVEL_UP_BTN_SIZE, btnColor);

			// "+" symbol
			int plusColor = canAfford ? 0xFFFFFFFF : 0xFF888888;
			graphics.drawCenteredString(
					Minecraft.getInstance().font,
					"+",
					btnX + LEVEL_UP_BTN_SIZE / 2,
					btnY + (LEVEL_UP_BTN_SIZE - 8) / 2,
					plusColor
			);
		}
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		PlayerSkillData tracker = liveTracker();
		if (definition.isMaxLevel(tracker.getProgress(skillKey).level())) return;

		int btnX = getX() + width - LEVEL_UP_BTN_SIZE - 4;
		int btnY = getY() + height - LEVEL_UP_BTN_SIZE - 4;
		if (mouseX >= btnX && mouseX < btnX + LEVEL_UP_BTN_SIZE
				&& mouseY >= btnY && mouseY < btnY + LEVEL_UP_BTN_SIZE) {
			if (canAffordLevelUp(tracker, tracker.getProgress(skillKey).level())) {
				ModNetwork.getPlay().sendToServer(
						new MessageLevelUpSkill(skillKey.location())
				);
			}
		}
	}

	private boolean canAffordLevelUp(PlayerSkillData tracker, int currentLevel) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return false;
		int neededSkillXp = definition.xpCostForNextLevel(currentLevel)
				- tracker.getProgress(skillKey).xp();
		if (neededSkillXp <= 0) return true;
		int vanillaCost = definition.skillToVanillaXp(neededSkillXp, currentLevel);
		return VanillaXpHelper.getTotalXp(mc.player) >= vanillaCost;
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
