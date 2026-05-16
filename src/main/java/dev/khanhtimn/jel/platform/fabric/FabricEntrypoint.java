package dev.khanhtimn.jel.platform.fabric;

//? fabric {

/*import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.JustEnoughLeveling;
import dev.khanhtimn.jel.api.loot.JelLootItemConditions;
import dev.khanhtimn.jel.api.loot.JelLootItemFunctions;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		Registry.register(
				BuiltInRegistries.LOOT_CONDITION_TYPE,
				Constants.rl("skill_level_check"),
				JelLootItemConditions.SKILL_LEVEL_CHECK
		);
		Registry.register(
				BuiltInRegistries.LOOT_CONDITION_TYPE,
				Constants.rl("trait_chance"),
				JelLootItemConditions.TRAIT_CHANCE
		);
		Registry.register(
				BuiltInRegistries.LOOT_FUNCTION_TYPE,
				Constants.rl("auto_smelt"),
				JelLootItemFunctions.AUTO_SMELT
		);
		JustEnoughLeveling.onInitialize();
		FabricEventSubscriber.registerEvents();
	}
}
*///?}
