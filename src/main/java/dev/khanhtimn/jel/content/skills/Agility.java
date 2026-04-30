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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public class Agility {
	public static final ResourceLocation EXHAUSTION = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "exhaustion");
	public static final ResourceLocation FALL_DAMAGE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "fall_damage");
	public static final ResourceLocation MOB_COLLIDE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mob_collide");

	public static final TraitKey EXHAUSTION_REDUCTION = TraitKey.of(EXHAUSTION, "reduction");
	public static final TraitKey FALL_DAMAGE_REDUCTION = TraitKey.of(FALL_DAMAGE, "reduction");
	public static final TraitKey MOB_COLLIDE_AVOID = TraitKey.of(MOB_COLLIDE, "avoid_chance");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.agility.name"))
				.description(Component.translatable("jel.skill.agility.description"))
				.icon(Items.LEATHER_BOOTS)
				.color(0xFFAA00)
				.maxLevel(30)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.attribute(List.of(
								AttributeEffect.base(
										Attributes.MOVEMENT_SPEED,
										LevelBasedValue.perLevel(0.07f, 0.001f)
								),
								AttributeEffect.base(
										Attributes.SNEAKING_SPEED,
										LevelBasedValue.perLevel(0.2f, 0.01f)
								),
								AttributeEffect.base(
										Attributes.SUBMERGED_MINING_SPEED,
										LevelBasedValue.perLevel(0.1f, 0.01f)
								),
								AttributeEffect.modifier(
										Attributes.WATER_MOVEMENT_EFFICIENCY,
										AttributeModifier.Operation.ADD_VALUE,
										LevelBasedValue.perLevel(0.1f, 0.01f),
										15
								),
								AttributeEffect.modifier(
										Attributes.OXYGEN_BONUS,
										AttributeModifier.Operation.ADD_VALUE,
										LevelBasedValue.perLevel(0.2f),
										10
								)
						)
				)
				.perk(List.of(
								Perk.trait(MOB_COLLIDE, TraitParam.of(MOB_COLLIDE_AVOID, LevelBasedValue.perLevel(0, 0.025f), 10)),
								Perk.trait(EXHAUSTION, TraitParam.of(EXHAUSTION_REDUCTION, LevelBasedValue.perLevel(0, 0.025f), 15)),
								Perk.trait(FALL_DAMAGE, TraitParam.of(FALL_DAMAGE_REDUCTION, LevelBasedValue.perLevel(0, 0.025f), 20)),
								Perk.effect(MobEffects.SATURATION, LevelBasedValue.constant(0), false, false, false, 30)
						)
				)
				.build();
	}

	private Agility() {
	}
}
