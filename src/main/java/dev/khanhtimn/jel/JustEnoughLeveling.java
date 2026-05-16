package dev.khanhtimn.jel;

import com.lowdragmc.lowdraglib2.Platform;
import dev.khanhtimn.jel.client.ClientBootstrap;

public class JustEnoughLeveling {

	public static void onInitialize() {
		Constants.LOG.info("Initializing {} on {}", Constants.MOD_ID, Platform.platformName());
		Bootstrap.init();
	}

	public static void onInitializeClient() {
		Constants.LOG.info("Initializing {} Client on {}", Constants.MOD_ID, Platform.platformName());
		ClientBootstrap.init();
	}
}
