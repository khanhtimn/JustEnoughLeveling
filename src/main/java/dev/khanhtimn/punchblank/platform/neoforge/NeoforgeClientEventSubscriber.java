package dev.khanhtimn.punchblank.platform.neoforge;

//? neoforge {

import dev.khanhtimn.punchblank.PunchBlank;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = PunchBlank.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEventSubscriber {
	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event) {
		PunchBlank.onInitializeClient();
	}
}
//?}
