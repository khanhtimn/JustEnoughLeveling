package dev.khanhtimn.jel.event;

import com.mrcrayfish.framework.api.event.PlayerEvents;
import dev.khanhtimn.jel.common.skill.impl.PlayerSkillData;
import dev.khanhtimn.jel.common.skill.impl.SkillLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class SkillEvents {

	public static void register() {
		PlayerEvents.LOGGED_IN.register(SkillEvents::onPlayerReady);
		PlayerEvents.RESPAWN.register((player, finishedGame) -> onPlayerReady(player));
	}

	/**
	 * Recompute all passive effects for a player that just
	 * entered the game (login) or returned from death/end-portal (respawn).
	 */
	private static void onPlayerReady(Player player) {
		if (player instanceof ServerPlayer sp) {
			PlayerSkillData skills = SkillLogic.getSkills(sp);
			SkillLogic.recomputeAll(sp, skills, sp.level().registryAccess());
		}
	}

	private SkillEvents() {
	}
}
