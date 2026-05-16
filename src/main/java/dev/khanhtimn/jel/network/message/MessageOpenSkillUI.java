package dev.khanhtimn.jel.network.message;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import dev.khanhtimn.jel.client.gui.JelMenuTypes;
import net.minecraft.server.level.ServerPlayer;

public final class MessageOpenSkillUI {

	@RPCPacket("jel:open_skill_ui")
	public static void handle(RPCSender sender) {
		ServerPlayer player = sender.asPlayer();
		if (player == null) {
			return;
		}
		PlayerUIMenuType.openUI(player, JelMenuTypes.SKILLS_UI);
	}

	public static void sendToServer() {
		RPCPacketDistributor.rpcToServer("jel:open_skill_ui");
	}

	private MessageOpenSkillUI() {
	}
}
