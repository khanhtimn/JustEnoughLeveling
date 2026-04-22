package dev.khanhtimn.jel.common.skill.skills;

import dev.khanhtimn.jel.common.skill.impl.SkillDefinition;
import dev.khanhtimn.jel.common.skill.impl.SkillPassive;
import dev.khanhtimn.jel.common.skill.impl.XpConversion;
import dev.khanhtimn.jel.common.skill.impl.XpFormula;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * Mining skill: improves block break speed, uses ratio XP conversion.
 */
public final class Mining {

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name("Mining")
				.description("Improves mining efficiency")
				.icon("minecraft:diamond_pickaxe")
				.color(0x55FFFF)
				.maxLevel(15)
				.xpFormula(XpFormula.of(LevelBasedValue.constant(200)))
				.xpConversion(XpConversion.ratio(1.5f))
				.passive(SkillPassive.attributeBonus(
						1,
						"minecraft:player.block_break_speed",
						LevelBasedValue.perLevel(0.1f, 0.1f),
						AttributeModifier.Operation.ADD_MULTIPLIED_BASE
				))
				.build();
	}

	private Mining() {
	}
}
