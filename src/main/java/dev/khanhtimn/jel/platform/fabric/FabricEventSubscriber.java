package dev.khanhtimn.jel.platform.fabric;

//? fabric {

import dev.khanhtimn.jel.registry.skill.SkillDefinition;
import dev.khanhtimn.jel.registry.skill.SkillRegistry;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

// Handles Fabric-specific registrations including the SkillDefinition datapack registry.
public class FabricEventSubscriber {

	public static void registerEvents() {
		// Register the SkillDefinition datapack registry via Fabric API
		DynamicRegistries.register(
				SkillRegistry.SKILL_REGISTRY_KEY,
				SkillDefinition.CODEC
		);
	}
}
//?}
