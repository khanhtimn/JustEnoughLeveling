package dev.khanhtimn.jel.platform.neoforge;

//? neoforge {

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.loot.JelLootItemConditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
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
				ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skill_level_check"),
				() -> JelLootItemConditions.SKILL_LEVEL_CHECK
		);
	}
}
//?}
