package dev.khanhtimn.jel.platform.neoforge;

//? neoforge {

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.JustEnoughLeveling;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class NeoforgeEntrypoint {

	public NeoforgeEntrypoint() {
		JustEnoughLeveling.onInitialize();
	}
}
//?}
