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

public class Farming {

	public static final ResourceLocation BREED_TWIN = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "breed_twin");
	public static final ResourceLocation BREED_XP = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "breed_xp");

	public static final TraitKey BREED_TWIN_CHANCE = TraitKey.of(BREED_TWIN, "chance");
	public static final TraitKey BREED_XP_MULTIPLIER = TraitKey.of(BREED_XP, "multiplier");

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name(Component.translatable("jel.skill.farming.name"))
				.description(Component.translatable("jel.skill.farming.description"))
				.icon(Items.POTION)
				.color(0xFFAA00)
				.maxLevel(30)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				.perk(List.of(
								Perk.trait(BREED_TWIN, TraitParam.of(BREED_TWIN_CHANCE, LevelBasedValue.perLevel(0, 0.02f), 20)),
								Perk.trait(BREED_XP, TraitParam.of(BREED_XP_MULTIPLIER, LevelBasedValue.perLevel(0, 0.05f), 15))
						)
				)
				.build();
	}

	private Farming() {
	}
}
