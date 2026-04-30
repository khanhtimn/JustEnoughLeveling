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

public class Archery {
	public static final ResourceLocation BOW_DAMAGE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bow_damage");
	public static final ResourceLocation CROSSBOW_DAMAGE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "crossbow_damage");
	public static final ResourceLocation BOW_DOUBLE_DAMAGE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bow_double_damage");
	public static final ResourceLocation CROSSBOW_DOUBLE_DAMAGE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "crossbow_double_damage");

	public static final TraitKey BOW_DAMAGE_BONUS = TraitKey.of(BOW_DAMAGE, "bonus");
	public static final TraitKey CROSSBOW_DAMAGE_BONUS = TraitKey.of(CROSSBOW_DAMAGE, "bonus");
	public static final TraitKey BOW_DOUBLE_CHANCE = TraitKey.of(BOW_DOUBLE_DAMAGE, "chance");
	public static final TraitKey CROSSBOW_DOUBLE_CHANCE = TraitKey.of(CROSSBOW_DOUBLE_DAMAGE, "chance");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.archery.name"))
				.description(Component.translatable("jel.skill.archery.description"))
				.icon(Items.BOW)
				.color(0xFF4444)
				.maxLevel(30)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.attribute(List.of(
						AttributeEffect.base(
								Attributes.BLOCK_INTERACTION_RANGE,
								LevelBasedValue.perLevel(2.5f, 0.1f)
						),
						AttributeEffect.base(
								Attributes.FOLLOW_RANGE,
								LevelBasedValue.perLevel(32.0f, 1.0f)
						)
				))
				.perk(List.of(
								Perk.trait(BOW_DAMAGE, TraitParam.of(BOW_DAMAGE_BONUS, LevelBasedValue.perLevel(0, 0.025f), 12)),
								Perk.trait(CROSSBOW_DAMAGE, TraitParam.of(CROSSBOW_DAMAGE_BONUS, LevelBasedValue.perLevel(0, 0.025f), 16)),
								Perk.trait(BOW_DOUBLE_DAMAGE, TraitParam.of(BOW_DOUBLE_CHANCE, LevelBasedValue.perLevel(0, 0.025f), 20)),
								Perk.trait(CROSSBOW_DOUBLE_DAMAGE, TraitParam.of(CROSSBOW_DOUBLE_CHANCE, LevelBasedValue.perLevel(0, 0.025f), 24))
						)
				)
				.build();
	}

	private Archery() {
	}
}
