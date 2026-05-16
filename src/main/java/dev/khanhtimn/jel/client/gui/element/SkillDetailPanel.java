package dev.khanhtimn.jel.client.gui.element;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.client.gui.sync.SkillSyncSetup;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class SkillDetailPanel extends UIElement {

	private final ScrollerView scrollContent;
	private final Label headerLabel;
	private final Label levelLabel;
	private final ProgressBar xpBar;
	private final Label xpLabel;
	private final Button levelUpButton;

	private ResourceLocation activeSkillId;
	private final SkillSyncSetup syncSetup;

	public SkillDetailPanel(SkillSyncSetup syncSetup, Runnable onBack) {
		this.syncSetup = syncSetup;

		setId("skill_detail");
		setVisible(false);
		layout(l -> {
			l.flexDirection(FlexDirection.COLUMN);
			l.widthPercent(100);
			l.heightPercent(100);
		});

		// Self-register the level-up RPC event
		addRPCEvent(syncSetup.getLevelUpRPC());

		// Back button
		var backButton = new Button();
		backButton.setText(Component.translatable("gui.jel.back"));
		backButton.setOnClick(e -> onBack.run());
		backButton.layout(l -> {
			l.height(20);
			l.widthAuto();
			l.marginBottom(4);
		});
		addChild(backButton);

		// Header row
		var headerRow = new UIElement();
		headerRow.layout(l -> {
			l.flexDirection(FlexDirection.ROW);
			l.alignItems(AlignItems.CENTER);
			l.gapColumn(8);
			l.marginBottom(8);
			l.widthPercent(100);
		});

		headerLabel = new Label();
		headerLabel.textStyle(ts -> ts.textColor(0xFFFFFFFF));
		headerLabel.layout(l -> {
			l.flexGrow(1);
			l.heightAuto();
		});
		headerRow.addChild(headerLabel);

		levelLabel = new Label();
		levelLabel.textStyle(ts -> ts.textColor(0xFFFFD700));
		levelLabel.layout(LayoutStyle::heightAuto);
		headerRow.addChild(levelLabel);

		addChild(headerRow);

		// XP progress section
		var xpSection = new UIElement();
		xpSection.layout(l -> {
			l.flexDirection(FlexDirection.COLUMN);
			l.gapRow(2);
			l.marginBottom(8);
			l.widthPercent(100);
		});

		xpBar = new ProgressBar();
		xpBar.setRange(0, 100);
		xpBar.layout(l -> {
			l.widthPercent(100);
			l.height(12);
		});
		xpSection.addChild(xpBar);

		xpLabel = new Label();
		xpLabel.textStyle(ts -> ts.textColor(0xFFAAAAAA));
		xpLabel.layout(LayoutStyle::heightAuto);
		xpSection.addChild(xpLabel);

		addChild(xpSection);

		// Level Up button
		levelUpButton = new Button();
		levelUpButton.setText(Component.translatable("gui.jel.level_up"));
		levelUpButton.layout(l -> {
			l.height(24);
			l.widthPercent(50);
			l.marginBottom(8);
		});
		addChild(levelUpButton);

		// Scrollable content for attributes and perks
		scrollContent = new ScrollerView();
		scrollContent.setId("detail_scroll");
		scrollContent.layout(l -> {
			l.flexDirection(FlexDirection.COLUMN);
			l.widthPercent(100);
			l.flexGrow(1);
		});
		scrollContent.viewContainer(vc -> vc.layout(l -> {
			l.flexDirection(FlexDirection.COLUMN);
			l.gapRow(4);
			l.widthPercent(100);
			l.paddingAll(4);
		}));
		addChild(scrollContent);
	}

	public void showSkill(ResourceLocation skillId) {
		this.activeSkillId = skillId;
		setVisible(true);
		rebuildContent();
	}

	public void hide() {
		setVisible(false);
		activeSkillId = null;
	}

	private void rebuildContent() {
		scrollContent.clearAllScrollViewChildren();
		if (activeSkillId == null) return;

		var registry = syncSetup.getPlayer().level().registryAccess()
				.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);
		var key = ResourceKey.create(JelRegistries.SKILL_REGISTRY_KEY, activeSkillId);
		SkillDefinition def = registry.get(key);
		if (def == null) return;

		// Update header
		headerLabel.setText(def.name());
		headerLabel.textStyle(ts -> ts.textColor(def.color()));

		SimpleBinding<Integer> levelBinding = syncSetup.getLevelBinding(activeSkillId);
		SimpleBinding<Integer> xpBinding = syncSetup.getXpBinding(activeSkillId);
		if (levelBinding == null || xpBinding == null) return;

		int level = levelBinding.getSyncValue().getValue() != null ? levelBinding.getSyncValue().getValue() : 0;
		int xp = xpBinding.getSyncValue().getValue() != null ? xpBinding.getSyncValue().getValue() : 0;
		int maxLevel = def.maxLevel();
		int xpRequired = def.xpCostForNextLevel(level);

		levelLabel.setText(Component.literal("Level " + level + " / " + maxLevel));
		xpBar.setRange(0, xpRequired > 0 ? xpRequired : 1);
		xpBar.setProgress(xp);
		xpLabel.setText(Component.literal(xp + " / " + xpRequired + " XP"));

		// Wire level-up button via RPC
		levelUpButton.setOnClick(e -> {
			var mui = getModularUI();
			if (mui != null && mui.syncManager != null) {
				mui.syncManager.sendEvent(syncSetup.getLevelUpRPC(), activeSkillId);
			}
		});

		// Reactive listeners via binding
		levelBinding.registerListener(newLevel -> {
			if (activeSkillId == null) return;
			int newXpReq = def.xpCostForNextLevel(newLevel);
			levelLabel.setText(Component.literal("Level " + newLevel + " / " + maxLevel));
			xpBar.setRange(0, newXpReq > 0 ? newXpReq : 1);
		});

		xpBinding.registerListener(newXp -> {
			if (activeSkillId == null) return;
			xpBar.setProgress(newXp);
			int currentLevel = levelBinding.getSyncValue().getValue() != null ? levelBinding.getSyncValue().getValue() : 0;
			int reqXp = def.xpCostForNextLevel(currentLevel);
			xpLabel.setText(Component.literal(newXp + " / " + reqXp + " XP"));
		});

		// Attributes section
		if (!def.attributes().isEmpty()) {
			var attrHeader = new Label();
			attrHeader.setText(Component.translatable("gui.jel.attributes"));
			attrHeader.textStyle(ts -> ts.textColor(0xFF88CCFF));
			attrHeader.layout(l -> {
				l.heightAuto();
				l.marginTop(4);
			});
			scrollContent.addScrollViewChild(attrHeader);

			for (var attr : def.attributes()) {
				var attrRow = new Label();
				attrRow.setText(Component.literal("  " + attr.toString()));
				attrRow.textStyle(ts -> ts.textColor(0xFFCCCCCC));
				attrRow.layout(LayoutStyle::heightAuto);
				scrollContent.addScrollViewChild(attrRow);
			}
		}

		// Perks section
		if (!def.perks().isEmpty()) {
			var perksHeader = new Label();
			perksHeader.setText(Component.translatable("gui.jel.perks"));
			perksHeader.textStyle(ts -> ts.textColor(0xFF88CCFF));
			perksHeader.layout(l -> {
				l.heightAuto();
				l.marginTop(4);
			});
			scrollContent.addScrollViewChild(perksHeader);

			for (var perk : def.perks()) {
				var perkRow = new Label();
				perkRow.setText(Component.literal("  " + perk.toString()));
				perkRow.textStyle(ts -> ts.textColor(0xFFCCCCCC));
				perkRow.layout(LayoutStyle::heightAuto);
				scrollContent.addScrollViewChild(perkRow);
			}
		}
	}
}
