package dev.khanhtimn.jel.core;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.network.message.MessageLevelUpSkill;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;

public final class ModNetwork {

	private static final FrameworkNetwork PLAY = FrameworkAPI
			.createNetworkBuilder(
					ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "play"),
					1
			)
			.registerPlayMessage(
					"level_up",
					MessageLevelUpSkill.class,
					MessageLevelUpSkill.STREAM_CODEC,
					MessageLevelUpSkill::handle,
					PacketFlow.SERVERBOUND
			)
			.build();

	public static FrameworkNetwork getPlay() {
		return PLAY;
	}

	public static void init() {
	}

	private ModNetwork() {
	}
}
