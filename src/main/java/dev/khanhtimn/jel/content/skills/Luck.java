package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.perk.Perk;
import dev.khanhtimn.jel.api.perk.TraitParam;
import dev.khanhtimn.jel.api.skill.AttributeEffect;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpFormula;
import dev.khanhtimn.jel.api.trait.TraitKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public class Luck {

	public static final ResourceLocation FREE_ANVIL_COST = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "free_anvil_cost");
	public static final TraitKey FREE_ANVIL_COST_CHANCE = TraitKey.of(FREE_ANVIL_COST, "chance");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.luck.name"))
				.description(Component.translatable("jel.skill.luck.description"))
				.icon(Items.EMERALD)
				.color(0xFFAA00)
				.maxLevel(30)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.attribute(
						AttributeEffect.base(
								Attributes.LUCK,
								LevelBasedValue.perLevel(-5f, 0.25f)
						)
				)
				.perk(List.of(
								Perk.trait(FREE_ANVIL_COST, TraitParam.of(FREE_ANVIL_COST_CHANCE, LevelBasedValue.perLevel(0.02f), 25))
						)
				)
				.build();
	}

	private Luck() {
	}
}
