package dev.khanhtimn.jel.platform.neoforge.loot;

//? neoforge {

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.khanhtimn.jel.common.LootDropModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * NeoForge Global Loot Modifier that applies all JEL multiplicative drop mechanics.
 * Replaces BlockMixin's runtime @WrapOperation on NeoForge — the mixin is only active on Fabric.
 */
public class JelBlockLootModifier extends LootModifier {

	public static final MapCodec<JelBlockLootModifier> CODEC =
			RecordCodecBuilder.mapCodec(instance ->
					codecStart(instance).apply(instance, JelBlockLootModifier::new));

	protected JelBlockLootModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@NotNull
	@Override
	protected ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		if (!(entity instanceof ServerPlayer player)) return generatedLoot;

		BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		if (state == null) return generatedLoot;

		LootDropModifiers.modifyMiningDrops(player, generatedLoot, state, context.getLevel());
		LootDropModifiers.modifyFarmingDrops(player, generatedLoot, state);

		return generatedLoot;
	}

	@NotNull
	@Override
	public MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
//?}
