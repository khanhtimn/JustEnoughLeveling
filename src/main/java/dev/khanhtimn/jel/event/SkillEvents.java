package dev.khanhtimn.jel.event;

import com.mrcrayfish.framework.api.event.PlayerEvents;
import dev.khanhtimn.jel.api.JelSkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class SkillEvents {

	public static void register() {
		PlayerEvents.LOGGED_IN.register(SkillEvents::onPlayerReady);
		PlayerEvents.RESPAWN.register((player, finishedGame) -> onPlayerReady(player));
	}

	private static void onPlayerReady(Player player) {
		if (player instanceof ServerPlayer sp) {
			JelSkills.recomputeAll(sp);
		}
	}

	private SkillEvents() {
	}
}
