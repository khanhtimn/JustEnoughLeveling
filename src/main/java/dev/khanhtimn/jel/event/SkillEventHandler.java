package dev.khanhtimn.jel.event;

import com.mrcrayfish.framework.api.event.PlayerEvents;
import dev.khanhtimn.jel.component.SkillsComponent;
import dev.khanhtimn.jel.registry.skill.SkillLogic;
import net.minecraft.server.level.ServerPlayer;

/**
 * Cross-platform event handler for skill-related player lifecycle events.
 * Uses Framework's own event API so no per-loader wiring is needed.
 */
public final class SkillEventHandler {

	/**
	 * Registers event listeners. Must be called during common initialization.
	 */
	public static void init() {
		// Recompute attributes when a player logs in
		PlayerEvents.LOGGED_IN.register(player -> {
			if (player instanceof ServerPlayer sp) {
				SkillsComponent skills = SkillLogic.getSkills(sp);
				SkillLogic.recomputeAll(sp, skills, sp.level().registryAccess());
			}
		});

		// Recompute attributes when a player respawns (after death or end-portal)
		PlayerEvents.RESPAWN.register((player, finishedGame) -> {
			if (player instanceof ServerPlayer sp) {
				SkillsComponent skills = SkillLogic.getSkills(sp);
				SkillLogic.recomputeAll(sp, skills, sp.level().registryAccess());
			}
		});
	}

	private SkillEventHandler() {}
}
