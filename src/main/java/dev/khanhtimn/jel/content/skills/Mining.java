package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.perk.Perk;
import dev.khanhtimn.jel.api.perk.TraitParam;
import dev.khanhtimn.jel.api.skill.AttributeEffect;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpConversion;
import dev.khanhtimn.jel.api.skill.XpFormula;
import dev.khanhtimn.jel.api.trait.TraitKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public final class Mining {
	public static final ResourceLocation BONUS_MINING_DROP = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bonus_mining_drop");

	public static final TraitKey BONUS_DROP_CHANCE = TraitKey.of(BONUS_MINING_DROP, "chance");

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
				.perk(Perk.trait(BONUS_MINING_DROP, TraitParam.of(BONUS_DROP_CHANCE, LevelBasedValue.perLevel(0, 0.025f), 24)))
				.build();
	}

	private Mining() {
	}
}
