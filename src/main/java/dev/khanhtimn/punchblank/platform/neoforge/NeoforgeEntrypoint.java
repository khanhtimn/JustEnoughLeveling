package dev.khanhtimn.punchblank.platform.neoforge;

//? neoforge {

import dev.khanhtimn.punchblank.PunchBlank;
import net.neoforged.fml.common.Mod;

@Mod(PunchBlank.MOD_ID)
public class NeoforgeEntrypoint {

	public NeoforgeEntrypoint() {
		PunchBlank.onInitialize();
	}
}
//?}
