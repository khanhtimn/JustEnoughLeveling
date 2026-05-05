package dev.khanhtimn.jel.content.skills;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.XpFormula;
import dev.khanhtimn.jel.api.trait.TraitKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public class Batering {

	public static final ResourceLocation TRADE_PRICE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "trade_price");
	public static final ResourceLocation TRADE_XP = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "trade_xp");
	public static final ResourceLocation TRADE_IMMUNITY = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "trade_immunity");
	public static final TraitKey TRADE_PRICE_DISCOUNT = TraitKey.of(TRADE_PRICE, "discount");
	public static final TraitKey TRADE_XP_MULTIPLIER = TraitKey.of(TRADE_XP, "multiplier");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.batering.name"))
				.description(Component.translatable("jel.skill.batering.description"))
				.icon(Items.EMERALD)
				.color(0xFFAA00)
				.maxLevel(30)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.trait(TRADE_XP, TRADE_XP_MULTIPLIER, LevelBasedValue.perLevel(0.005f), 10)
				.trait(TRADE_PRICE, TRADE_PRICE_DISCOUNT, LevelBasedValue.perLevel(0.01f), 15)
				.trait(TRADE_IMMUNITY, 30)
				.build();
	}

	private Batering() {
	}
}
