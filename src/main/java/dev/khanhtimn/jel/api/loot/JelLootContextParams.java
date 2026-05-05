package dev.khanhtimn.jel.api.loot;

import dev.khanhtimn.jel.Constants;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public final class JelLootContextParams {
	public static final LootContextParam<Integer> SKILL_LEVEL
			= new LootContextParam<>(Constants.rl("skill_level"));

	private JelLootContextParams() {
	}
}
