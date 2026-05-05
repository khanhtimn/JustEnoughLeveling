package dev.khanhtimn.jel.core;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.network.message.MessageLevelUpSkill;
import dev.khanhtimn.jel.network.message.MessageOreHighlight;
import net.minecraft.network.protocol.PacketFlow;

public final class ModNetwork {

	private static final FrameworkNetwork PLAY = FrameworkAPI
			.createNetworkBuilder(
					Constants.rl("play"),
					1
			)
			.registerPlayMessage(
					"level_up",
					MessageLevelUpSkill.class,
					MessageLevelUpSkill.STREAM_CODEC,
					MessageLevelUpSkill::handle,
					PacketFlow.SERVERBOUND
			)
			.registerPlayMessage(
					"ore_highlight",
					MessageOreHighlight.class,
					MessageOreHighlight.STREAM_CODEC,
					MessageOreHighlight::handle,
					PacketFlow.CLIENTBOUND
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
