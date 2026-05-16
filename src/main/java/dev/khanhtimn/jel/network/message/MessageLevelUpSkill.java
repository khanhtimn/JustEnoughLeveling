package dev.khanhtimn.jel.network.message;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class MessageLevelUpSkill {

	private static final String PACKET_ID = "jel:level_up";

	@RPCPacket("jel:level_up")
	public static void handle(RPCSender sender, ResourceLocation skillId) {
		if (!sender.isServer()) return;
		ServerPlayer player = sender.asPlayer();
		if (player == null) return;

		ResourceKey<SkillDefinition> key = ResourceKey.create(
				JelRegistries.SKILL_REGISTRY_KEY, skillId);
		boolean success = JelSkills.tryLevelUpOnce(player, key);
		if (!success) {
			Constants.LOG.debug("Level-up denied for {} on skill {} (insufficient XP or max level)",
					player.getName().getString(), skillId);
		}
	}

	public static void sendToServer(ResourceLocation skillId) {
		RPCPacketDistributor.rpcToServer(PACKET_ID, skillId);
	}

	private MessageLevelUpSkill() {
	}
}
