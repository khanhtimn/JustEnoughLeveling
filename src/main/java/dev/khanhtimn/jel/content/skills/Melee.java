package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.api.skill.AttributeEffect;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpFormula;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

/**
 * Melee skill: increases attack aptitudes.
 */
public final class Melee {

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.melee.name"))
				.description(Component.translatable("jel.skill.melee.description"))
				.icon(Items.DIAMOND_SWORD)
				.color(0xFF4444)
				.maxLevel(20)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.attribute(List.of(
						AttributeEffect.base(
								Attributes.ATTACK_DAMAGE,
								LevelBasedValue.perLevel(0.4f, 0.2f)
						),
						AttributeEffect.base(
								Attributes.ATTACK_SPEED,
								LevelBasedValue.perLevel(2.4f, 0.2f)
						),
						AttributeEffect.modifier(
								Attributes.ATTACK_KNOCKBACK,
								AttributeModifier.Operation.ADD_VALUE,
								LevelBasedValue.perLevel(0.05f),
								6
						),
						AttributeEffect.modifier(
								Attributes.SWEEPING_DAMAGE_RATIO,
								AttributeModifier.Operation.ADD_VALUE,
								LevelBasedValue.perLevel(0, 0.03f),
								12
						)
				))
				.build();
	}

	private Melee() {
	}
}
