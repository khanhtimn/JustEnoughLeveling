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

public final class Defense {
	public static final ResourceLocation DEATH_GRACE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "death_grace");

	public static final TraitKey DEATH_GRACE_CHANCE = TraitKey.of(DEATH_GRACE, "chance");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.defense.name"))
				.description(Component.translatable("jel.skill.defense.description"))
				.icon(Items.SHIELD)
				.color(0xFF4444)
				.maxLevel(30)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.attribute(List.of(
						AttributeEffect.base(
								Attributes.ARMOR,
								LevelBasedValue.perLevel(0.0f, 0.15f)
						),
						AttributeEffect.base(
								Attributes.KNOCKBACK_RESISTANCE,
								LevelBasedValue.perLevel(0.0f, 0.05f)
						)
				))
				.perk(List.of(
								Perk.trait(DEATH_GRACE, TraitParam.of(DEATH_GRACE_CHANCE, LevelBasedValue.perLevel(0, 0.025f), 24))
						)
				)
				.build();
	}

	private Defense() {
	}
}
