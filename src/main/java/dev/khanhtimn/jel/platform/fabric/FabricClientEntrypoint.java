package dev.khanhtimn.jel.platform.fabric;

//? fabric {

import dev.khanhtimn.jel.JustEnoughLeveling;
import dev.khanhtimn.jel.client.render.OreHighlightRenderer;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		JustEnoughLeveling.onInitializeClient();

		WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) return;

			OreHighlightRenderer.render(
					ctx.matrixStack(),
					ctx.consumers(),
					ctx.camera(),
					mc.level.getGameTime()
			);
		});
	}

}
//?}
