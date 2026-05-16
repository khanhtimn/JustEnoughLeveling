package dev.khanhtimn.jel.client.gui;

import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.khanhtimn.jel.client.gui.element.SkillDetailPanel;
import dev.khanhtimn.jel.client.gui.element.SkillOverviewPanel;
import dev.khanhtimn.jel.client.gui.sync.SkillSyncSetup;
import dev.vfyjxf.taffy.style.FlexDirection;

public final class SkillUIBuilder {

	public static UI buildUI(SkillSyncSetup syncSetup) {
		var root = new UIElement();
		root.setId("jel_root");
		root.layout(l -> {
			l.flexDirection(FlexDirection.COLUMN);
			l.widthPercent(80);
			l.heightPercent(80);
			l.paddingAll(8);
		});

		// Mutable holder to break the circular reference between overview and detail
		var viewState = new ViewState();

		var detail = new SkillDetailPanel(syncSetup, () -> {
			viewState.detail.hide();
			viewState.overview.setVisible(true);
		});

		var overview = new SkillOverviewPanel(syncSetup, skillId -> {
			viewState.overview.setVisible(false);
			viewState.detail.showSkill(skillId);
		});

		viewState.overview = overview;
		viewState.detail = detail;

		root.addChild(overview);
		root.addChild(detail);

		return UI.of(root);
	}

	private static final class ViewState {
		SkillOverviewPanel overview;
		SkillDetailPanel detail;
	}

	private SkillUIBuilder() {
	}
}
