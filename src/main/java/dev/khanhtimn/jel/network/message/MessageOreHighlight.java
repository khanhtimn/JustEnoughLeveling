package dev.khanhtimn.jel.network.message;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import dev.khanhtimn.jel.client.render.OreHighlightRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class MessageOreHighlight {

	private static final String PACKET_ID = "jel:ore_highlight";

	@RPCPacket("jel:ore_highlight")
	public static void handle(RPCSender sender, BlockPos position, int color, int durationTicks) {
		if (sender.isServer()) return;

		long gameTick = 0L;
		var player = sender.asPlayer();
		if (player != null) {
			gameTick = player.level().getGameTime();
		}
		long expireTick = gameTick + durationTicks;
		OreHighlightRenderer.addHighlight(position, color, expireTick);
	}

	public static void sendToPlayer(ServerPlayer player, List<BlockPos> positions, int color, int durationTicks) {
		for (BlockPos pos : positions) {
			RPCPacketDistributor.rpcToPlayer(player, PACKET_ID, pos, color, durationTicks);
		}
	}

	private MessageOreHighlight() {
	}
}
