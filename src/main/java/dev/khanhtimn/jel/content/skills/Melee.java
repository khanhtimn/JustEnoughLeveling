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

public final class Melee {
	public static final ResourceLocation CRITICAL_DAMAGE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "critical_damage");
	public static final ResourceLocation DOUBLE_DAMAGE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "double_damage");

	public static final TraitKey CRIT_DAMAGE_BONUS = TraitKey.of(CRITICAL_DAMAGE, "bonus");
	public static final TraitKey DOUBLE_DAMAGE_CHANCE = TraitKey.of(DOUBLE_DAMAGE, "chance");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.melee.name"))
				.description(Component.translatable("jel.skill.melee.description"))
				.icon(Items.DIAMOND_SWORD)
				.color(0xFF4444)
				.maxLevel(30)
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
								18
						),
						AttributeEffect.modifier(
								Attributes.SWEEPING_DAMAGE_RATIO,
								AttributeModifier.Operation.ADD_VALUE,
								LevelBasedValue.perLevel(0, 0.03f),
								15
						)
				))
				.perk(List.of(
								Perk.trait(CRITICAL_DAMAGE, TraitParam.of(CRIT_DAMAGE_BONUS, LevelBasedValue.perLevel(0, 0.005f), 21)),
								Perk.trait(DOUBLE_DAMAGE, TraitParam.of(DOUBLE_DAMAGE_CHANCE, LevelBasedValue.perLevel(0, 0.025f), 24))
						)
				)
				.build();
	}

	private Melee() {
	}
}
