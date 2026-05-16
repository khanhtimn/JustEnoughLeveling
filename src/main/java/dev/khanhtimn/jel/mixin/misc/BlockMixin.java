package dev.khanhtimn.jel.mixin.misc;

import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Farming;
import dev.khanhtimn.jel.content.skills.Mining;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? fabric {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.khanhtimn.jel.common.LootDropModifiers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import java.util.ArrayList;
import java.util.List;
*///?}

@Mixin(Block.class)
public abstract class BlockMixin {

	@Unique
	private static final ThreadLocal<Boolean> jel$inAoeBreak = ThreadLocal.withInitial(() -> false);

	//? fabric {

	/*@WrapOperation(
			method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
			)
	)
	private static List<ItemStack> jel$modifyDrops(
			BlockState state, ServerLevel level, BlockPos pos,
			@Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack tool,
			Operation<List<ItemStack>> original) {
		List<ItemStack> drops = original.call(state, level, pos, blockEntity, entity, tool);
		if (!(entity instanceof ServerPlayer player)) return drops;

		List<ItemStack> modified = new ArrayList<>(drops);
		LootDropModifiers.modifyMiningDrops(player, modified, state, level);
		if (state.getBlock() instanceof CropBlock) {
			LootDropModifiers.modifyFarmingDrops(player, modified, state);
		}
		return modified;
	}
	*///?}

	@Inject(
			method = "playerDestroy",
			at = @At("TAIL")
	)
	private void jel$afterPlayerDestroy(
			Level level, Player player, BlockPos pos, BlockState state,
			@Nullable BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
		if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) return;
		if (jel$inAoeBreak.get()) return;

		if (state.getBlock() instanceof CropBlock) {
			jel$applyGreenThumb(serverPlayer, level, pos, state);
			jel$applyHarvestFestival(serverPlayer, level, pos, state);
		}

		if (JelSkills.getLevel(serverPlayer, ModSkills.MINING) > 0) {
			jel$applyQuarryStrike(serverPlayer, level, pos, state);
			jel$applyTremor(serverPlayer, level, pos, state);
		}
	}

	@Unique
	private static void jel$applyQuarryStrike(ServerPlayer player, Level level, BlockPos pos, BlockState state) {
		float chance = JelTraits.branchValue(player, ModSkills.MINING,
				Mining.EXCAVATOR_BRANCH, Mining.QUARRY_STRIKE_CHANCE);
		if (chance <= 0 || player.getRandom().nextFloat() >= chance) return;

		int extra = (int) JelTraits.branchValue(player, ModSkills.MINING,
				Mining.EXCAVATOR_BRANCH, Mining.QUARRY_STRIKE_EXTRA);
		if (extra <= 0) return;

		jel$inAoeBreak.set(true);
		try {
			int broken = 0;
			for (Direction dir : Direction.values()) {
				if (broken >= extra) break;
				BlockPos adjacent = pos.relative(dir);
				BlockState adjState = level.getBlockState(adjacent);
				if (adjState.is(state.getBlock())) {
					level.destroyBlock(adjacent, true, player);
					broken++;
				}
			}
		} finally {
			jel$inAoeBreak.set(false);
		}
	}

	@Unique
	private static void jel$applyTremor(ServerPlayer player, Level level, BlockPos pos, BlockState state) {
		if (JelTraits.isOnCooldown(player, Mining.TREMOR)) return;
		float radius = JelTraits.branchValue(player, ModSkills.MINING,
				Mining.EXCAVATOR_BRANCH, Mining.TREMOR_RADIUS);
		if (radius <= 0) return;

		int r = (int) radius;
		int cooldown = (int) JelTraits.branchValue(player, ModSkills.MINING,
				Mining.EXCAVATOR_BRANCH, Mining.TREMOR_COOLDOWN);
		JelTraits.setCooldown(player, Mining.TREMOR, cooldown);

		jel$inAoeBreak.set(true);
		try {
			BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
			for (int dx = -r; dx <= r; dx++) {
				for (int dy = -r; dy <= r; dy++) {
					for (int dz = -r; dz <= r; dz++) {
						if (dx == 0 && dy == 0 && dz == 0) continue;
						mutable.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
						BlockState target = level.getBlockState(mutable);
						if (target.is(state.getBlock())) {
							level.destroyBlock(mutable.immutable(), true, player);
						}
					}
				}
			}
		} finally {
			jel$inAoeBreak.set(false);
		}
	}

	@Unique
	private static void jel$applyGreenThumb(ServerPlayer player, Level level, BlockPos pos, BlockState harvestedState) {
		if (!(harvestedState.getBlock() instanceof CropBlock crop)) return;
		if (!crop.isMaxAge(harvestedState)) return;
		if (!JelTraits.branchTestChance(player, ModSkills.FARMING,
				Farming.CULTIVATOR_BRANCH, Farming.GREEN_THUMB_CHANCE)) return;

		level.setBlock(pos, crop.getStateForAge(0), Block.UPDATE_ALL);
	}

	@Unique
	private static void jel$applyHarvestFestival(ServerPlayer player, Level level, BlockPos pos, BlockState harvestedState) {
		if (!(harvestedState.getBlock() instanceof CropBlock crop)) return;
		if (!crop.isMaxAge(harvestedState)) return;
		if (JelTraits.isOnCooldown(player, Farming.HARVEST_FESTIVAL)) return;

		float radius = JelTraits.branchValue(player, ModSkills.FARMING,
				Farming.CULTIVATOR_BRANCH, Farming.HARVEST_FESTIVAL_RADIUS);
		if (radius <= 0) return;

		int r = (int) radius;
		int cooldown = (int) JelTraits.branchValue(player, ModSkills.FARMING,
				Farming.CULTIVATOR_BRANCH, Farming.HARVEST_FESTIVAL_COOLDOWN);
		JelTraits.setCooldown(player, Farming.HARVEST_FESTIVAL, cooldown);

		jel$inAoeBreak.set(true);
		try {
			BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (dx == 0 && dz == 0) continue;
					mutable.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
					BlockState adjState = level.getBlockState(mutable);
					if (adjState.getBlock() == harvestedState.getBlock()
							&& crop.isMaxAge(adjState)) {
						level.destroyBlock(mutable.immutable(), true, player);
					}
				}
			}
		} finally {
			jel$inAoeBreak.set(false);
		}
	}
}
