package dev.khanhtimn.jel.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Farming;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

	@Shadow
	@Nullable
	public abstract Player getPlayerOwner();

	@ModifyExpressionValue(
			method = "catchingFish",
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/FishingHook;lureSpeed:I", opcode = Opcodes.GETFIELD)
	)
	private int jel$modifyLureSpeed(int original) {
		Player player = this.getPlayerOwner();
		if (!(player instanceof ServerPlayer sp)) return original;

		float speedBonus = JelTraits.branchValue(sp, ModSkills.FARMING,
				Farming.RANCHER_BRANCH, Farming.ANGLERS_INSTINCT_SPEED);
		if (speedBonus <= 0) return original;

		return original + (int) (speedBonus * 100);
	}

	@ModifyExpressionValue(
			method = "retrieve",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;getLuck()F"
			)
	)
	private float jel$addTreasureHunterLuck(float original) {
		Player player = this.getPlayerOwner();
		if (!(player instanceof ServerPlayer sp)) return original;

		float bonus = JelTraits.branchValue(sp, ModSkills.FARMING,
				Farming.RANCHER_BRANCH, Farming.TREASURE_HUNTER_BONUS);
		return original + bonus;
	}

	@Inject(
			method = "retrieve",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/advancements/critereon/FishingRodHookedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/FishingHook;Ljava/util/Collection;)V",
					ordinal = 1
			),
			require = 1
	)
	private void jel$doubleFishLoot(ItemStack rod, CallbackInfoReturnable<Integer> cir,
	                                @Local List<ItemStack> list) {
		Player player = this.getPlayerOwner();
		if (!(player instanceof ServerPlayer sp)) return;
		if (!JelTraits.branchTestChance(sp, ModSkills.FARMING,
				Farming.RANCHER_BRANCH, Farming.FISHERMANS_FORTUNE_CHANCE)) return;

		List<ItemStack> extras = new ArrayList<>();
		for (ItemStack stack : list) {
			if (stack.is(ItemTags.FISHES)) {
				extras.add(stack.copy());
			}
		}
		list.addAll(extras);
	}
}
