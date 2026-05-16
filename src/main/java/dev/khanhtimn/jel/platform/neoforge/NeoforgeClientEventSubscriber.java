package dev.khanhtimn.jel.platform.neoforge;

//? neoforge {

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.JustEnoughLeveling;
import dev.khanhtimn.jel.client.render.OreHighlightRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class NeoforgeClientEventSubscriber {

	public static void registerModBusEvents(IEventBus modBus) {
		modBus.addListener(NeoforgeClientEventSubscriber::onClientSetup);
	}

	private static void onClientSetup(final FMLClientSetupEvent event) {
		JustEnoughLeveling.onInitializeClient();
	}

	@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
	public static class GameBusEvents {
		@SubscribeEvent
		public static void onRenderLevelStage(final RenderLevelStageEvent event) {
			if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
			Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) return;

			OreHighlightRenderer.render(
					event.getPoseStack(),
					mc.renderBuffers().bufferSource(),
					event.getCamera(),
					mc.level.getGameTime()
			);
		}
	}
}
//?}
