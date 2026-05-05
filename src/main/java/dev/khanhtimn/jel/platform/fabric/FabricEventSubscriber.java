package dev.khanhtimn.jel.platform.fabric;

//? fabric {

/*import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.common.JelLootPoolInjector;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.LootPool;

public class FabricEventSubscriber {

	public static void registerEvents() {
		DynamicRegistries.registerSynced(
				JelRegistries.SKILL_REGISTRY_KEY,
				SkillDefinition.CODEC,
				SkillDefinition.NETWORK_CODEC
		);

		registerLootTableModifications();
	}

	private static void registerLootTableModifications() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!source.isBuiltin()) return;

			for (LootPool.Builder pool : JelLootPoolInjector.buildMiningPools(key)) {
				tableBuilder.withPool(pool);
			}
		});
	}
}
*///?}
