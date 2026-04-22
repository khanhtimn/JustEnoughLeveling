package dev.khanhtimn.jel.platform.fabric;

//? fabric {

/*import dev.khanhtimn.jel.common.skill.impl.SkillDefinition;
import dev.khanhtimn.jel.core.ModRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

// Handles Fabric-specific registrations including the SkillDefinition datapack registry.
public class FabricEventSubscriber {

	public static void registerEvents() {
		// Register the SkillDefinition datapack registry via Fabric API (synced to clients)
		DynamicRegistries.registerSynced(
				ModRegistries.SKILL_REGISTRY_KEY,
				SkillDefinition.CODEC,
				SkillDefinition.NETWORK_CODEC
		);
	}
}
*///?}
