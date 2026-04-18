package dev.khanhtimn.punchblank;

import dev.khanhtimn.punchblank.platform.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? fabric {
/*import dev.khanhtimn.punchblank.platform.fabric.FabricPlatform;
*///?} neoforge {
import dev.khanhtimn.punchblank.platform.neoforge.NeoforgePlatform;
 //?} forge {
/*import com.example.modtemplate.platform.forge.ForgePlatform;
*///?}

@SuppressWarnings("LoggingSimilarMessage")
public class PunchBlank {

	public static final String MOD_ID = /*$ mod_id*/ "punchblank";
	public static final String MOD_VERSION = /*$ mod_version*/ "0.1.0";
	public static final String MOD_FRIENDLY_NAME = /*$ mod_name*/ "PunchBlank";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Platform PLATFORM = createPlatformInstance();

	public static void onInitialize() {
		LOGGER.info("Initializing {} on {}", MOD_ID, PunchBlank.xplat().loader());
		LOGGER.debug("{}: { version: {}; friendly_name: {} }", MOD_ID, MOD_VERSION, MOD_FRIENDLY_NAME);
	}

	public static void onInitializeClient() {
		LOGGER.info("Initializing {} Client on {}", MOD_ID, PunchBlank.xplat().loader());
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
