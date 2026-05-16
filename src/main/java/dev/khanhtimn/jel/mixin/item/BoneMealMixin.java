package dev.khanhtimn.jel.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.skills.Farming;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoneMealItem.class)
public abstract class BoneMealMixin {

	//? if neoforge {
	@WrapOperation(
			method = "applyBonemeal",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/BonemealableBlock;performBonemeal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
			)
	)
	private static void jel$wrapPerformBonemeal(
			BonemealableBlock instance, ServerLevel level, RandomSource random,
			BlockPos pos, BlockState state, Operation<Void> original,
			@SuppressWarnings("unused") ItemStack stack, Level levelArg,
			BlockPos posArg, Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			float failChance = JelTraits.value(serverPlayer, Farming.BONEMEAL_FAILURE_CHANCE);
			if (failChance > 0 && random.nextFloat() < failChance) {
				jel$spawnFailParticles(level, pos);
				return;
			}
		}
		original.call(instance, level, random, pos, state);
	}
	//?} elif fabric {
	/*@WrapOperation(
			method = "growCrop",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/BonemealableBlock;performBonemeal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
			)
	)
	private static void jel$wrapPerformBonemeal(
			BonemealableBlock instance, ServerLevel level, RandomSource random,
			BlockPos pos, BlockState state, Operation<Void> original,
			@SuppressWarnings("unused") ItemStack stack,
			@SuppressWarnings("unused") Level levelArg,
			@SuppressWarnings("unused") BlockPos posArg) {
		ServerPlayer player = jel$findNearestPlayer(level, pos);
		if (player != null) {
			float failChance = JelTraits.value(player, Farming.BONEMEAL_FAILURE_CHANCE);
			if (failChance > 0 && random.nextFloat() < failChance) {
				jel$spawnFailParticles(level, pos);
				return;
			}
		}
		original.call(instance, level, random, pos, state);
	}

	@Unique
	private static ServerPlayer jel$findNearestPlayer(ServerLevel level, BlockPos pos) {
		Player nearest = level.getNearestPlayer(
				pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6.0, false);
		return nearest instanceof ServerPlayer sp ? sp : null;
	}
	*///?}

	@Unique
	private static void jel$spawnFailParticles(ServerLevel level, BlockPos pos) {
		for (int i = 0; i < 8; i++) {
			double dx = level.random.nextGaussian() * 0.15;
			double dy = level.random.nextDouble() * 0.3;
			double dz = level.random.nextGaussian() * 0.15;
			level.sendParticles(ParticleTypes.SMOKE,
					pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					1, dx, dy, dz, 0.01);
		}
	}
}
