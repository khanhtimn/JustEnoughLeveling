package dev.khanhtimn.jel.mixin.item;

import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.skills.Archery;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

	@Inject(method = "shootProjectile", at = @At("TAIL"))
	private void jel$crossbowShootMixin(
			LivingEntity shooter, Projectile projectile,
			int index, float speed,
			float divergence, float yaw,
			LivingEntity target, CallbackInfo ci
	) {
		if (shooter instanceof Player player && projectile instanceof AbstractArrow abstractArrow) {
			float bonus = JelTraits.value(player, Archery.BOW_DAMAGE_BONUS);
			if (bonus > 0) {
				abstractArrow.setBaseDamage(abstractArrow.getBaseDamage() + bonus);
			}
			if (JelTraits.testChance(player, Archery.BOW_DOUBLE_CHANCE)) {
				abstractArrow.setBaseDamage(abstractArrow.getBaseDamage() * 2D);
			}
		}
	}
}
