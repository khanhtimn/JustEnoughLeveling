package dev.khanhtimn.jel.common.skill.impl.perk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.khanhtimn.jel.Constants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Grants items from a loot table when the perk is unlocked.
 * <p>
 * Uses the {@link LootContextParamSets#GIFT GIFT} param set with the
 * player's luck stat. Items go to inventory; overflow drops at feet.
 * <p>
 * Uses {@link ApplyMode#EVENT} — fires once on upward threshold crossing.
 * Revoke is a no-op (items are not taken back).
 *
 * <h2>JSON</h2>
 * <pre>{@code
 * { "type": "jel:loot_table", "loot_table": "jel:rewards/constitution_10",
 *   "unlock_level": 10 }
 * }</pre>
 */
public record LootTablePerk(
		int unlockLevel,
		ResourceLocation lootTable
) implements Perk {

	public static final MapCodec<LootTablePerk> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					Codec.INT.fieldOf("unlock_level")
							.forGetter(LootTablePerk::unlockLevel),
					ResourceLocation.CODEC.fieldOf("loot_table")
							.forGetter(LootTablePerk::lootTable)
			).apply(instance, LootTablePerk::new));

	public static LootTablePerk of(ResourceLocation lootTable, int unlockLevel) {
		return new LootTablePerk(unlockLevel, lootTable);
	}

	public static LootTablePerk of(String lootTable, int unlockLevel) {
		return new LootTablePerk(unlockLevel, ResourceLocation.parse(lootTable));
	}

	@Override
	public PerkType<?> type() {
		return PerkType.LOOT_TABLE;
	}

	@Override
	public ApplyMode applyMode() {
		return ApplyMode.EVENT;
	}

	@Override
	public void apply(PerkContext ctx, int currentLevel) {
		ServerPlayer player = ctx.player();
		ServerLevel level = ctx.serverLevel();

		ResourceKey<LootTable> key = ResourceKey.create(
				net.minecraft.core.registries.Registries.LOOT_TABLE, lootTable);
		LootTable table = level.getServer().reloadableRegistries()
				.getLootTable(key);
		if (table == LootTable.EMPTY) {
			Constants.LOG.warn("Perk loot table not found: {}", lootTable);
			return;
		}

		LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.withParameter(LootContextParams.ORIGIN, player.position())
				.withLuck(player.getLuck())
				.create(LootContextParamSets.GIFT);

		table.getRandomItems(params, player.getRandom().nextLong(), stack -> {
			if (!stack.isEmpty()) {
				giveOrDrop(player, stack);
			}
		});
	}

	@Override
	public void revoke(PerkContext ctx) {
		// One-shot reward — items are not taken back.
	}

	private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}
}
