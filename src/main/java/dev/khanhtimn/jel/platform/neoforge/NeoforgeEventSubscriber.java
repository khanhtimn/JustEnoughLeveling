package dev.khanhtimn.jel.platform.neoforge;

//? neoforge {

import dev.khanhtimn.jel.registry.skill.SkillDefinition;
import dev.khanhtimn.jel.registry.skill.SkillRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/**
 * Handles NeoForge-specific event subscriptions on the MOD event bus.
 */
@EventBusSubscriber()
public class NeoforgeEventSubscriber {

	@SubscribeEvent
	public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
		event.dataPackRegistry(
				SkillRegistry.SKILL_REGISTRY_KEY,
				SkillDefinition.CODEC
		);
	}
}
//?}
