package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.AttributeEffect;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpFormula;
import dev.khanhtimn.jel.api.perk.Perk;
import dev.khanhtimn.jel.api.perk.TraitParam;
import dev.khanhtimn.jel.api.trait.TraitKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public final class Constitution {
	public static final ResourceLocation NATURAL_REGEN = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "natural_regen");

	public static final TraitKey REGEN_BONUS = TraitKey.of(NATURAL_REGEN, "bonus");

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
				.perk(List.of(
								Perk.trait(NATURAL_REGEN, TraitParam.of(REGEN_BONUS, LevelBasedValue.perLevel(0, 0.025f), 10)),
								Perk.effect(MobEffects.REGENERATION, LevelBasedValue.constant(0), false, false, false, 34)
						)
				)
				.build();
	}

	private Constitution() {
	}
}
