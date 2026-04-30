package dev.khanhtimn.jel.platform.fabric;

//? fabric {

/*import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.JustEnoughLeveling;
import dev.khanhtimn.jel.api.loot.JelLootItemConditions;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		Registry.register(
				BuiltInRegistries.LOOT_CONDITION_TYPE,
				ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skill_level_check"),
				JelLootItemConditions.SKILL_LEVEL_CHECK
		);
		JustEnoughLeveling.onInitialize();
		FabricEventSubscriber.registerEvents();
	}
}
*///?}
