package dev.khanhtimn.jel.common.skill.skills;

import dev.khanhtimn.jel.common.skill.SkillDefinition;
import dev.khanhtimn.jel.common.skill.SkillPassive;
import dev.khanhtimn.jel.common.skill.XpFormula;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Combat skill: increases attack damage, unlocks combat mastery at level 5.
 */
public final class CombatSkill {

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name("Combat", 0xFF4444)
				.description("Increases attack damage and attack speed")
				.icon("minecraft:diamond_sword")
				.color(0xFF4444)
				.maxLevel(10)
				.xpFormula(XpFormula.linear(100, 50))
				.passive(SkillPassive.attributeBonus(
						1,
						"minecraft:generic.attack_damage",
						0.5,
						AttributeModifier.Operation.ADD_VALUE
				))
				.passive(SkillPassive.abilityFlag(5, "jel:combat_mastery"))
				.build();
	}

	private CombatSkill() {}
}
