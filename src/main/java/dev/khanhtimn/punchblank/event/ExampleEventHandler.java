package dev.khanhtimn.punchblank.event;

import dev.khanhtimn.punchblank.PunchBlank;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class ExampleEventHandler {

	public static void onPlayerHurt(ServerPlayer player) {
		//? if > 1.19.2 {
		// MinecraftServer.pvp is private... only here to test ATs/AWs
		if (Objects.requireNonNull(player.getServer()).pvp) {
			PunchBlank.LOGGER.info("{} took damage. PVP is allowed.", player.getDisplayName());
		} else {
			PunchBlank.LOGGER.info("{} took damage. PVP is disallowed.", player.getDisplayName());
		}
		//?}
	}
}
