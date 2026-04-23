package dev.khanhtimn.jel.common.skill.skills;

import dev.khanhtimn.jel.common.skill.impl.AttributeEffect;
import dev.khanhtimn.jel.common.skill.impl.SkillDefinition;
import dev.khanhtimn.jel.common.skill.impl.XpFormula;
import dev.khanhtimn.jel.common.skill.impl.perk.Perk;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * Constitution skill: controls max health base value and grants regeneration.
 * <p>
 * At level 0, the player starts with only 3 hearts (6.0 health).
 * Each level increases the base, reaching 40.0 at level 34.
 * At level 24, the player gains passive Regeneration I via a native
 * {@link dev.khanhtimn.jel.common.skill.impl.perk.EffectPerk}.
 */
public final class Constitution {

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.constitution.name"))
				.description(Component.translatable("jel.skill.constitution.description"))
				.icon(Items.GOLDEN_APPLE)
				.color(0xFFAA00)
				.maxLevel(34)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.attribute(
						AttributeEffect.base(
								Attributes.MAX_HEALTH,
								LevelBasedValue.perLevel(6f, 1f)
						)
				)
				.perk(Perk.effect(MobEffects.REGENERATION, 1, 24))
				.build();
	}

	private Constitution() {
	}
}
