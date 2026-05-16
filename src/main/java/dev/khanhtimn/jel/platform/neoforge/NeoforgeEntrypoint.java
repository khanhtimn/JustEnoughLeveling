package dev.khanhtimn.jel.platform.neoforge;

//? neoforge {

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.JustEnoughLeveling;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class NeoforgeEntrypoint {

	public NeoforgeEntrypoint(IEventBus modBus) {
		NeoforgeEventSubscriber.registerModBusEvents(modBus);
		if (FMLEnvironment.dist.isClient()) {
			NeoforgeClientEventSubscriber.registerModBusEvents(modBus);
		}
		JustEnoughLeveling.onInitialize();
	}
}
//?}
