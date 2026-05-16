package dev.khanhtimn.jel.client.gui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Tab;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TabView;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.client.gui.sync.SkillSyncSetup;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Grid overview of all skills, organized by category tabs (All / Combat / Utility / Crafting).
 * Each tab contains a scrollable list of {@link SkillCardWidget} entries.
 */
public class SkillOverviewPanel extends UIElement {

	public SkillOverviewPanel(SkillSyncSetup syncSetup, Consumer<ResourceLocation> onClickSkill) {
		setId("skill_overview");
		layout(l -> {
			l.flexDirection(FlexDirection.COLUMN);
			l.widthPercent(100);
			l.heightPercent(100);
		});

		Registry<SkillDefinition> registry = syncSetup.getPlayer().level().registryAccess()
				.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);

		// Group skills by category derived from their skill ID namespace path
		Map<String, Map<ResourceLocation, SkillDefinition>> categories = new LinkedHashMap<>();
		categories.put("all", new LinkedHashMap<>());

		for (var entry : registry.entrySet()) {
			ResourceLocation id = entry.getKey().location();
			SkillDefinition def = entry.getValue();

			categories.get("all").put(id, def);

			String category = extractCategory(id);
			categories.computeIfAbsent(category, k -> new LinkedHashMap<>()).put(id, def);
		}

		var tabView = new TabView();
		tabView.setId("skill_tabs");
		tabView.layout(l -> {
			l.flexDirection(FlexDirection.COLUMN);
			l.widthPercent(100);
			l.flexGrow(1);
		});

		for (var catEntry : categories.entrySet()) {
			String category = catEntry.getKey();
			Map<ResourceLocation, SkillDefinition> skills = catEntry.getValue();

			var tab = new Tab();
			tab.setText(capitalizeFirst(category));

			var scrollContent = new ScrollerView();
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

			for (var skillEntry : skills.entrySet()) {
				ResourceLocation skillId = skillEntry.getKey();
				SkillDefinition def = skillEntry.getValue();
				var levelBinding = syncSetup.getLevelBinding(skillId);
				var xpBinding = syncSetup.getXpBinding(skillId);
				if (levelBinding == null || xpBinding == null) continue;

				var card = new SkillCardWidget(skillId, def, levelBinding, xpBinding, onClickSkill);
				scrollContent.addScrollViewChild(card);
			}

			tabView.addTab(tab, scrollContent);
		}

		addChild(tabView);
	}

	private static String extractCategory(ResourceLocation skillId) {
		String path = skillId.getPath();
		int slash = path.indexOf('/');
		if (slash > 0) {
			return path.substring(0, slash);
		}
		return "general";
	}

	private static String capitalizeFirst(String s) {
		if (s == null || s.isEmpty()) return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
