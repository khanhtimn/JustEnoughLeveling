package dev.khanhtimn.jel.mixin.misc;

import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Farming;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class CropBlockMixin {

	@Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
	private void jel$fertileTouchGrowth(BlockState state, Level level, BlockPos pos,
										Player player, BlockHitResult hitResult,
										CallbackInfoReturnable<InteractionResult> cir) {
		if (level.isClientSide()) return;
		if (!(player instanceof ServerPlayer serverPlayer)) return;
		if (!((Object) this instanceof CropBlock crop)) return;
		if (crop.isMaxAge(state)) return;

		float bonus = JelTraits.branchValue(serverPlayer, ModSkills.FARMING,
				Farming.CULTIVATOR_BRANCH, Farming.FERTILE_TOUCH_BONUS);
		if (bonus <= 0) return;

		if (serverPlayer.getRandom().nextFloat() >= bonus) return;

		int newAge = Math.min(crop.getAge(state) + 1, crop.getMaxAge());
		level.setBlock(pos, crop.getStateForAge(newAge), CropBlock.UPDATE_ALL);
		cir.setReturnValue(InteractionResult.SUCCESS);
	}
}
