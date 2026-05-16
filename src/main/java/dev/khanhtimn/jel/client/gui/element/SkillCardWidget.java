package dev.khanhtimn.jel.client.gui.element;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class SkillCardWidget extends UIElement {

	private final ResourceLocation skillId;
	private final SkillDefinition definition;

	public SkillCardWidget(ResourceLocation skillId, SkillDefinition definition,
						   SimpleBinding<Integer> levelBinding, SimpleBinding<Integer> xpBinding,
						   Consumer<ResourceLocation> onClickSkill) {
		this.skillId = skillId;
		this.definition = definition;

		setId("skill_card_" + skillId);
		layout(l -> {
			l.flexDirection(FlexDirection.ROW);
			l.alignItems(AlignItems.CENTER);
			l.paddingAll(4);
			l.gapColumn(6);
			l.height(28);
			l.widthPercent(100);
		});
		style(s -> s.background(new ColorRectTexture(0x80000000)));

		// Self-register sync values so they auto-register with UISyncManager
		addSyncValue(levelBinding.getSyncValue());
		addSyncValue(xpBinding.getSyncValue());

		// Skill icon
		var iconElement = new UIElement();
		iconElement.layout(l -> {
			l.width(16);
			l.height(16);
		});
		iconElement.style(s -> s.background(definition.icon().toGuiTexture()));
		addChild(iconElement);

		// Skill name
		var nameLabel = new Label();
		nameLabel.setText(definition.name());
		nameLabel.textStyle(ts -> ts.textColor(definition.color()));
		nameLabel.layout(l -> {
			l.flexGrow(1);
			l.heightAuto();
		});
		addChild(nameLabel);

		// Level badge
		var levelLabel = new Label();
		int level = levelBinding.getSyncValue().getValue() != null ? levelBinding.getSyncValue().getValue() : 0;
		levelLabel.setText(Component.literal("Lv." + level));
		levelLabel.textStyle(ts -> ts.textColor(0xFFFFD700));
		levelLabel.layout(LayoutStyle::heightAuto);
		addChild(levelLabel);

		// XP progress bar
		int maxLevel = definition.maxLevel();
		var progressBar = new ProgressBar();
		progressBar.setRange(0, maxLevel > 0 ? maxLevel : 1);
		progressBar.setProgress(level);
		progressBar.layout(l -> {
			l.width(40);
			l.height(6);
		});
		addChild(progressBar);

		// Reactive listeners via binding
		levelBinding.registerListener(newLevel -> {
			levelLabel.setText(Component.literal("Lv." + newLevel));
			progressBar.setProgress(newLevel);
		});

		// Click to open detail view
		addEventListener("mouseDown", e -> onClickSkill.accept(this.skillId));
	}

	public ResourceLocation getSkillId() {
		return skillId;
	}

	public SkillDefinition getDefinition() {
		return definition;
	}
}
