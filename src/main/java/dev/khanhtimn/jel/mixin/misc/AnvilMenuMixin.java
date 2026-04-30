package dev.khanhtimn.jel.mixin.misc;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.skills.Luck;
import dev.khanhtimn.jel.content.skills.Smithing;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

	@Shadow
	@Mutable
	@Final
	private DataSlot cost;

	public AnvilMenuMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(menuType, i, inventory, containerLevelAccess);
	}

	@Inject(
			method = "mayPickup",
			at = @At("HEAD"),
			cancellable = true
	)
	protected void jel$mayPickupMixin(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
		if (JelTraits.has(player, Smithing.XP_CAP)) {
			cir.setReturnValue(true);
		}
	}


	@Inject(
			method = "createResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V",
					ordinal = 4)
	)
	private void jel$createResultMixin(CallbackInfo ci) {
		if (this.cost.get() > 1) {
//			if (JelTraits.has(this.player, Smithing.ANVIL_XP_COST)) {
//				this.cost.set((int) JelTraits.value(this.player, Smithing.XP_CAP));
//
//			}
			Constants.LOG.info("Original cost: {}", this.cost.get());
			Constants.LOG.info("Discount: {}", JelTraits.value(this.player, Smithing.XP_DISCOUNT));
			int levelCost = this.cost.get() + (int) JelTraits.value(this.player, Smithing.XP_DISCOUNT);
			Constants.LOG.info("Discounted cost: {}", levelCost);
//			TODO: Tweak this to allign with
//			if (level >= skillBonus.getLevel()) {
//				return (int) (levelCost * (1.0f - level * ConfigInit.CONFIG.anvilXpDiscountBonus));
//			}
//			but using LevelBasedValue declared in Smithing
			this.cost.set(levelCost);
		}
	}

	@Inject(
			method = "onTake",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/DataSlot;get()I"
			),
			require = 0
	)
	private void onTakeOutputMixin(Player player, ItemStack itemStack, CallbackInfo ci) {
		if (JelTraits.testChance(player, Luck.FREE_ANVIL_COST_CHANCE)) {
			this.cost.set(0);
		}
	}

	@Inject(
			method = "getCost",
			at = @At(
					value = "HEAD"
			),
			cancellable = true
	)
	public void jel$getAnvilCostMixin(CallbackInfoReturnable<Integer> info) {
		int levelCost = this.cost.get() + (int) JelTraits.value(this.player, Smithing.XP_DISCOUNT);
		if (levelCost != this.cost.get()) {
			info.setReturnValue(levelCost);
		}
	}

}
