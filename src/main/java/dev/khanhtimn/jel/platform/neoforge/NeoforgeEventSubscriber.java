package dev.khanhtimn.jel.platform.neoforge;

//? neoforge {

import dev.khanhtimn.jel.common.skill.impl.SkillDefinition;
import dev.khanhtimn.jel.core.ModRegistries;
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
				ModRegistries.SKILL_REGISTRY_KEY,
				SkillDefinition.CODEC,
				SkillDefinition.NETWORK_CODEC
		);
	}
}
//?}
