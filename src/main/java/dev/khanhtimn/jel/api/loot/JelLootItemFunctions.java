package dev.khanhtimn.jel.api.loot;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public final class JelLootItemFunctions {

	public static final LootItemFunctionType<JelAutoSmeltFunction> AUTO_SMELT =
			new LootItemFunctionType<>(JelAutoSmeltFunction.CODEC);

	private JelLootItemFunctions() {
	}
}
