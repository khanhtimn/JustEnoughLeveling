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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public class Smithing {
	public static final ResourceLocation UPGRADED_TNT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "upgraded_tnt");
	public static final ResourceLocation AVOID_ITEM_BREAK = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "avoid_item_break");
	public static final ResourceLocation ANVIL_XP_COST = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "anvil_xp_cost");

	public static final TraitKey TNT_STRENGTH = TraitKey.of(UPGRADED_TNT, "multiplier");
	public static final TraitKey ITEM_BREAK_CHANCE = TraitKey.of(AVOID_ITEM_BREAK, "chance");
	//TODO: Make this percentage
	public static final TraitKey XP_DISCOUNT = TraitKey.of(ANVIL_XP_COST, "xp_discount");
	public static final TraitKey XP_CAP = TraitKey.of(ANVIL_XP_COST, "xp_cap");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.smithing.name"))
				.description(Component.translatable("jel.skill.smithing.description"))
				.icon(Items.FURNACE)
				.color(0xFFAA00)
				.maxLevel(30)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.attribute(List.of(
								AttributeEffect.base(
										Attributes.BURNING_TIME,
										LevelBasedValue.perLevel(1.8f, -0.05f)
								),
								AttributeEffect.modifier(
										Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
										AttributeModifier.Operation.ADD_VALUE,
										LevelBasedValue.perLevel(0.05f),
										15
								)
						)
				)
				.perk(List.of(
								Perk.trait(AVOID_ITEM_BREAK, TraitParam.of(ITEM_BREAK_CHANCE, LevelBasedValue.perLevel(0.005f), 10)),
								Perk.trait(ANVIL_XP_COST,
										TraitParam.of(XP_DISCOUNT, LevelBasedValue.perLevel(0, -1f), 12),
										TraitParam.of(XP_CAP, LevelBasedValue.constant(30f), 30)
								),
								Perk.trait(UPGRADED_TNT, TraitParam.of(TNT_STRENGTH, LevelBasedValue.perLevel(0.2f), 15))
						)
				)
				.build();
	}

	private Smithing() {
	}
}
