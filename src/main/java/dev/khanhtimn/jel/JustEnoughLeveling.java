package dev.khanhtimn.jel;

import dev.khanhtimn.jel.platform.Platform;

//? fabric {
/*import dev.khanhtimn.jel.platform.fabric.FabricPlatform;
 *///?} neoforge {
import dev.khanhtimn.jel.platform.neoforge.NeoforgePlatform;
 //?} forge {
/*import dev.khanhtimn.jel.platform.forge.ForgePlatform;
 *///?}

public class JustEnoughLeveling {

	private static final Platform PLATFORM = createPlatformInstance();

	public static void onInitialize() {
		Constants.LOG.info("Initializing {} on {}", Constants.MOD_ID, xplat().loader());
		Bootstrap.init();
	}

	public static void onInitializeClient() {
		Constants.LOG.info("Initializing {} Client on {}", Constants.MOD_ID, xplat().loader());
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
