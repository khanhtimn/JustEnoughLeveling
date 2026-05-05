package dev.khanhtimn.jel.platform.neoforge;

//? neoforge {

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.loot.JelLootItemConditions;
import dev.khanhtimn.jel.api.loot.JelLootItemFunctions;
import dev.khanhtimn.jel.core.ModAttributes;
import dev.khanhtimn.jel.platform.neoforge.loot.JelBlockLootModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber()
public class NeoforgeEventSubscriber {

	@SubscribeEvent
	public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
		event.dataPackRegistry(
				JelRegistries.SKILL_REGISTRY_KEY,
				SkillDefinition.CODEC,
				SkillDefinition.NETWORK_CODEC
		);
	}

	@SubscribeEvent
	public static void onRegister(RegisterEvent event) {
		event.register(
				BuiltInRegistries.LOOT_CONDITION_TYPE.key(),
				Constants.rl("skill_level_check"),
				() -> JelLootItemConditions.SKILL_LEVEL_CHECK
		);
		event.register(
				BuiltInRegistries.LOOT_CONDITION_TYPE.key(),
				Constants.rl("trait_chance"),
				() -> JelLootItemConditions.TRAIT_CHANCE
		);
		event.register(
				BuiltInRegistries.LOOT_FUNCTION_TYPE.key(),
				Constants.rl("auto_smelt"),
				() -> JelLootItemFunctions.AUTO_SMELT
		);
		event.register(
				BuiltInRegistries.ATTRIBUTE.key(),
				Constants.rl("max_food_level"),
				() -> ModAttributes.MAX_FOOD_LEVEL
		);
		event.register(
				NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.key(),
				Constants.rl("jel_block_drops"),
				() -> JelBlockLootModifier.CODEC
		);
	}
}
//?}
