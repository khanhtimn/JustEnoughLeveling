package dev.khanhtimn.jel.common;

import com.mrcrayfish.framework.api.event.PlayerEvents;
import dev.khanhtimn.jel.common.skill.SkillLogic;
import dev.khanhtimn.jel.data.skill.SkillsTracker;
import net.minecraft.server.level.ServerPlayer;

/**
 * Cross-platform event handler for skill-related player lifecycle events.
 * Uses Framework's own event API so no per-loader wiring is needed.
 */
public final class SkillEvents {

	/**
	 * Registers event listeners. Must be called during common initialization.
	 */
	public static void init() {
		// Recompute attributes when a player logs in
		PlayerEvents.LOGGED_IN.register(player -> {
			if (player instanceof ServerPlayer sp) {
				SkillsTracker skills = SkillLogic.getSkills(sp);
				SkillLogic.recomputeAll(sp, skills, sp.level().registryAccess());
			}
		});

		// Recompute attributes when a player respawns (after death or end-portal)
		PlayerEvents.RESPAWN.register((player, finishedGame) -> {
			if (player instanceof ServerPlayer sp) {
				SkillsTracker skills = SkillLogic.getSkills(sp);
				SkillLogic.recomputeAll(sp, skills, sp.level().registryAccess());
			}
		});
	}

	private SkillEvents() {}
}
