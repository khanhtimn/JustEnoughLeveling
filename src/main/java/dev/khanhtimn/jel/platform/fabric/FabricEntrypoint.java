package dev.khanhtimn.jel.platform.fabric;

//? fabric {

import dev.khanhtimn.jel.JustEnoughLeveling;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		JustEnoughLeveling.onInitialize();
		FabricEventSubscriber.registerEvents();
	}
}
//?}
