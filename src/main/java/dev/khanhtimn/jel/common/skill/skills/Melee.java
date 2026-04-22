package dev.khanhtimn.jel.common.skill.skills;

import dev.khanhtimn.jel.common.skill.impl.SkillDefinition;
import dev.khanhtimn.jel.common.skill.impl.SkillPassive;
import dev.khanhtimn.jel.common.skill.impl.XpFormula;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * Melee skill: increases attack damage, unlocks combat mastery at level 5.
 */
public final class Melee {

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name("Melee")
				.description("Increases attack damage and attack speed")
				.icon("minecraft:diamond_sword")
				.color(0xFF4444)
				.maxLevel(10)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.passive(SkillPassive.attributeBonus(
						1,
						"minecraft:generic.attack_damage",
						LevelBasedValue.perLevel(0.5f, 0.5f),
						AttributeModifier.Operation.ADD_VALUE
				))
				.passive(SkillPassive.abilityFlag(5, "jel:combat_mastery"))
				.build();
	}

	private Melee() {
	}
}
