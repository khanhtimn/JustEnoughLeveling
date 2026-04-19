package dev.khanhtimn.jel;

import com.mrcrayfish.framework.api.FrameworkAPI;
import dev.khanhtimn.jel.core.ModSyncedDataKeys;
import dev.khanhtimn.jel.event.SkillEventHandler;
import dev.khanhtimn.jel.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? fabric {
/*import dev.khanhtimn.jel.platform.fabric.FabricPlatform;
 *///?} neoforge {
import dev.khanhtimn.jel.platform.neoforge.NeoforgePlatform;
 //?} forge {
/*import dev.khanhtimn.jel.platform.forge.ForgePlatform;
 *///?}

@SuppressWarnings("LoggingSimilarMessage")
public class JustEnoughLeveling {

	public static final String MOD_ID = /*$ mod_id*/ "jel";
	public static final String MOD_VERSION = /*$ mod_version*/ "0.1.0";
	public static final String MOD_FRIENDLY_NAME = /*$ mod_name*/ "JustEnoughLeveling";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Platform PLATFORM = createPlatformInstance();

	public static void onInitialize() {
		LOGGER.info("Initializing {} on {}", MOD_ID, JustEnoughLeveling.xplat().loader());
		LOGGER.debug("{}: { version: {}; friendly_name: {} }", MOD_ID, MOD_VERSION, MOD_FRIENDLY_NAME);

		// Register Framework synced data keys
		FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.PLAYER_SKILLS);

		// Register skill event listeners (login/respawn attribute recompute)
		SkillEventHandler.init();
	}

	public static void onInitializeClient() {
		LOGGER.info("Initializing {} Client on {}", MOD_ID, JustEnoughLeveling.xplat().loader());
		LOGGER.debug("{}: { version: {}; friendly_name: {} }", MOD_ID, MOD_VERSION, MOD_FRIENDLY_NAME);
	}

	static Platform xplat() {
		return PLATFORM;
	}

	private static Platform createPlatformInstance() {
		//? fabric {
		/*return new FabricPlatform();
		 *///?} neoforge {
		return new NeoforgePlatform();
		//?} forge {
		/*return new ForgePlatform();
		 *///?}
	}
}
