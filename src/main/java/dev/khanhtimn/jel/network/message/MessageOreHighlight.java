package dev.khanhtimn.jel.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import dev.khanhtimn.jel.client.render.OreHighlightRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record MessageOreHighlight(List<BlockPos> positions, int color, int durationTicks) {

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageOreHighlight> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()).cast(),
					MessageOreHighlight::positions,
					ByteBufCodecs.INT.cast(),
					MessageOreHighlight::color,
					ByteBufCodecs.INT.cast(),
					MessageOreHighlight::durationTicks,
					MessageOreHighlight::new
			);

	public static void handle(MessageOreHighlight message, MessageContext context) {
		context.execute(() -> {
			long gameTick = context.getPlayer()
					.map(player -> player.level().getGameTime())
					.orElse(0L);
			long expireTick = gameTick + message.durationTicks();
			for (BlockPos pos : message.positions()) {
				OreHighlightRenderer.addHighlight(pos, message.color(), expireTick);
			}
		});
		context.setHandled(true);
	}
}
