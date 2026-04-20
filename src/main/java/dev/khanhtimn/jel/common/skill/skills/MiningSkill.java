package dev.khanhtimn.jel.common.skill.skills;

import dev.khanhtimn.jel.common.skill.SkillDefinition;
import dev.khanhtimn.jel.common.skill.SkillPassive;
import dev.khanhtimn.jel.common.skill.XpConversion;
import dev.khanhtimn.jel.common.skill.XpFormula;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Mining skill: improves block break speed, uses ratio XP conversion.
 */
public final class MiningSkill {

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name("Mining", 0x55FFFF)
				.description("Improves mining efficiency")
				.icon("minecraft:diamond_pickaxe")
				.color(0x55FFFF)
				.maxLevel(15)
				.xpFormula(XpFormula.constant(200))
				.xpConversion(XpConversion.ratio(1.5))
				.passive(SkillPassive.attributeBonus(
						1,
						"minecraft:player.block_break_speed",
						0.1,
						AttributeModifier.Operation.ADD_MULTIPLIED_BASE
				))
				.build();
	}

	private MiningSkill() {}
}
