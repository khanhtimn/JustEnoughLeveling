package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.api.skill.AttributeEffect;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpConversion;
import dev.khanhtimn.jel.api.skill.XpFormula;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

/**
 * Mining skill: improves block break speed.
 */
public final class Mining {

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.mining.name"))
				.description(Component.translatable("jel.skill.mining.description"))
				.icon(Items.GOLDEN_PICKAXE)
				.color(0x55FFFF)
				.maxLevel(30)
				.xpFormula(XpFormula.of(LevelBasedValue.constant(200)))
				.xpConversion(XpConversion.ratio(1.5f))
				.attribute(
						List.of(
								AttributeEffect.base(
										Attributes.BLOCK_BREAK_SPEED,
										LevelBasedValue.perLevel(0.5f, 0.05f)
								),
								AttributeEffect.modifier(
										Attributes.MINING_EFFICIENCY,
										AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
										LevelBasedValue.perLevel(0f, 0.1f),
										18
								)
						)
				)
				.build();
	}

	private Mining() {
	}
}
