package dev.khanhtimn.jel;

import dev.khanhtimn.jel.core.ModEvents;
import dev.khanhtimn.jel.core.ModNetwork;
import dev.khanhtimn.jel.core.ModCommands;
import dev.khanhtimn.jel.common.ModSyncedDataKeys;

public final class Bootstrap {

	public static void init() {
		ModSyncedDataKeys.init();
		ModNetwork.init();
		ModCommands.init();
		ModEvents.init();
	}

	private Bootstrap() {
	}
}
