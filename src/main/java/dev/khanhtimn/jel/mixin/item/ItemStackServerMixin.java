package dev.khanhtimn.jel.mixin.item;

import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.skills.Smithing;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackServerMixin {

	//? if fabric {
	@Inject(
			method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I"
			),
			cancellable = true
	)
	private void jel$breakItem(int i, ServerLevel serverLevel, ServerPlayer serverPlayer, Consumer<Item> consumer, CallbackInfo ci) {
		if (JelTraits.testChance(serverPlayer, Smithing.ITEM_BREAK_CHANCE)) {
			ci.cancel();
		}
	}

	//?} else {
	/*@Inject(
			method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I"
			),
			cancellable = true
	)
	private void jel$breakItem(int i, ServerLevel serverLevel, LivingEntity entity, Consumer<Item> consumer, CallbackInfo ci) {
		if (entity instanceof ServerPlayer serverPlayer && JelTraits.testChance(serverPlayer, Smithing.ITEM_BREAK_CHANCE)) {
			ci.cancel();
		}
	}
	*///?}
}
