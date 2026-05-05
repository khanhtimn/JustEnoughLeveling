package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpFormula;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

// TODO: Redesign Cooking skill — MAX_FOOD_LEVEL now managed by Constitution
public final class Cooking {

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.cooking.name"))
				.description(Component.translatable("jel.skill.cooking.description"))
				.icon(Items.COOKED_BEEF)
				.color(0xFFAA00)
				.maxLevel(30)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.build();
	}

	private Cooking() {
	}
}
