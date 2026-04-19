package dev.khanhtimn.jel.platform.fabric;

//? fabric {

import dev.khanhtimn.jel.JustEnoughLeveling;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		JustEnoughLeveling.onInitializeClient();
	}

}
//?}
