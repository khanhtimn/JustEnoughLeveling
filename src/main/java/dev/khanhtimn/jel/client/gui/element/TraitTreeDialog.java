package dev.khanhtimn.jel.client.gui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Overlay dialog showing the trait tree for a skill branch.
 * Opened from the detail panel when a player clicks a trait branch header.
 */
public class TraitTreeDialog extends Dialog {

	public TraitTreeDialog(ResourceLocation skillId, SkillDefinition definition) {
		setClickOutsideClose(true);
		darkenBackground();
		setTitle(definition.name().getString() + " — Traits");

		layout(l -> {
			l.widthPercent(70);
			l.heightPercent(70);
		});

		var scrollContent = new ScrollerView();
		scrollContent.layout(l -> {
			l.flexDirection(FlexDirection.COLUMN);
			l.widthPercent(100);
			l.flexGrow(1);
		});
		scrollContent.viewContainer(vc -> vc.layout(l -> {
			l.flexDirection(FlexDirection.COLUMN);
			l.gapRow(6);
			l.widthPercent(100);
			l.paddingAll(6);
		}));

		if (!definition.perks().isEmpty()) {
			for (var perk : definition.perks()) {
				var row = new UIElement();
				row.layout(l -> {
					l.flexDirection(FlexDirection.ROW);
					l.gapColumn(6);
					l.widthPercent(100);
					l.paddingAll(4);
				});

				var nameLabel = new Label();
				nameLabel.setText(Component.literal(perk.toString()));
				nameLabel.textStyle(ts -> ts.textColor(0xFFCCCCCC));
				nameLabel.layout(l -> {
					l.flexGrow(1);
					l.heightAuto();
				});
				row.addChild(nameLabel);

				scrollContent.addScrollViewChild(row);
			}
		} else {
			var emptyLabel = new Label();
			emptyLabel.setText(Component.translatable("gui.jel.no_traits"));
			emptyLabel.textStyle(ts -> ts.textColor(0xFF888888));
			emptyLabel.layout(LayoutStyle::heightAuto);
			scrollContent.addScrollViewChild(emptyLabel);
		}

		addContent(scrollContent);
	}
}
