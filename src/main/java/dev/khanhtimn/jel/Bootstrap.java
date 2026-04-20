package dev.khanhtimn.jel;

import com.mrcrayfish.framework.api.FrameworkAPI;
import dev.khanhtimn.jel.common.SkillEvents;
import dev.khanhtimn.jel.core.ModCommands;
import dev.khanhtimn.jel.core.ModSyncedDataKeys;

/**
 * Common initialization entry point.
 * Registers synced data keys, events, commands, and other shared setup.
 */
public final class Bootstrap {

	public static void init() {
		// Register Framework synced data keys
		FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.PLAYER_SKILLS);

		// Register skill event listeners (login/respawn attribute recompute)
		SkillEvents.init();

		// Register debug/admin commands
		ModCommands.init();
	}

	private Bootstrap() {}
}
