package dev.khanhtimn.jel.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Mining;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMiningMixin {

	@WrapOperation(
			method = "mineBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
			)
	)
	private void jel$reduceMiningDurability(
			ItemStack stack, int damage, LivingEntity entity, EquipmentSlot slot,
			Operation<Void> original) {
		if (entity instanceof Player player) {
			if (JelTraits.branchTestChance(player, ModSkills.MINING,
					Mining.EXCAVATOR_BRANCH, Mining.UNBREAKING_GRIP_REDUCTION)) {
				return;
			}

			float earthshaper = JelTraits.branchValue(player, ModSkills.MINING,
					Mining.EXCAVATOR_BRANCH, Mining.EARTHSHAPER_ACTIVE);
			if (earthshaper > 0) {
				damage = Math.max(0, damage / 2);
				if (damage == 0) return;
			}
		}
		original.call(stack, damage, entity, slot);
	}
}
