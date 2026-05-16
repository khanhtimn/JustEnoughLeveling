package dev.khanhtimn.jel.event;

import dev.khanhtimn.jel.api.JelSkills;
import net.minecraft.server.level.ServerPlayer;

public final class SkillEvents {

	public static void onPlayerReady(ServerPlayer sp) {
		JelSkills.recomputeAll(sp);
	}

	private SkillEvents() {
	}
}
