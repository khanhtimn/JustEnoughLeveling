package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.perk.Perk;
import dev.khanhtimn.jel.api.perk.TraitParam;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpFormula;
import dev.khanhtimn.jel.api.trait.TraitKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public class Magic {
	public static final ResourceLocation DAMAGE_REFLECTION = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "damage_reflection");
	public static final ResourceLocation UPGRADED_EFFECT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "upgraded_effect");

	public static final TraitKey UPGRADED_EFFECT_CHANCE = TraitKey.of(UPGRADED_EFFECT, "chance");
	public static final TraitKey REFLECT_CHANCE = TraitKey.of(DAMAGE_REFLECTION, "chance");
	public static final TraitKey REFLECT_MULTIPLIER = TraitKey.of(DAMAGE_REFLECTION, "multiplier");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.magic.name"))
				.description(Component.translatable("jel.skill.magic.description"))
				.icon(Items.POTION)
				.color(0xFFAA00)
				.maxLevel(30)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.perk(List.of(
								Perk.trait(DAMAGE_REFLECTION,
										TraitParam.of(REFLECT_CHANCE, LevelBasedValue.perLevel(0, 0.008f), 15),
										TraitParam.of(REFLECT_MULTIPLIER, LevelBasedValue.perLevel(0, 0.025f), 15)
								),
								Perk.trait(UPGRADED_EFFECT, TraitParam.of(UPGRADED_EFFECT_CHANCE, LevelBasedValue.perLevel(0, 0.012f), 20))
						)
				)
				.build();
	}

	private Magic() {
	}
}
