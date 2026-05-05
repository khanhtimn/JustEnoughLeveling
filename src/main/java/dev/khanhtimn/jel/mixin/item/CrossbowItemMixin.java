package dev.khanhtimn.jel.mixin.item;

import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Archery;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

	@Inject(method = "shootProjectile", at = @At("TAIL"))
	private void jel$crossbowShootMixin(
			LivingEntity shooter, Projectile projectile,
			int index, float speed,
			float divergence, float yaw,
			LivingEntity target, CallbackInfo ci
	) {
		if (!(shooter instanceof Player player) || !(projectile instanceof AbstractArrow arrow)) return;
		if (player.level().isClientSide()) return;

		// Base crossbow damage bonus (available to all archery users)
		float bonus = JelTraits.value(player, Archery.CROSSBOW_DAMAGE_BONUS);
		if (bonus > 0) {
			arrow.setBaseDamage(arrow.getBaseDamage() + bonus);
		}

		if (!JelSkills.isBranch(player, ModSkills.ARCHERY, Archery.VOLLEY_BRANCH)) return;

		// Crossbow Crit (Volley) — double damage chance
		if (JelTraits.testChance(player, Archery.CROSSBOW_CRIT_CHANCE)) {
			arrow.setBaseDamage(arrow.getBaseDamage() * 2D);
			arrow.setCritArrow(true);
		}

		// Chain Bolt (Volley) — tag for chain-hit processing in ArrowMixin
		float chainTargets = JelTraits.value(player, Archery.CHAIN_BOLT_TARGETS);
		if (chainTargets > 0) {
			arrow.addTag("jel:chain_bolt");
			arrow.addTag("jel:chain_targets:" + (int) chainTargets);
		}

		// Explosive Bolt (Volley) — tag for explosion on hit
		if (JelTraits.testChance(player, Archery.EXPLOSIVE_BOLT_CHANCE)) {
			arrow.addTag("jel:explosive_bolt");
		}

		// Auto Loader (Volley capstone) — chance to skip reload cooldown
		if (JelTraits.testChance(player, Archery.AUTO_LOADER_CHANCE)
				&& !JelTraits.isOnCooldown(player, Archery.AUTO_LOADER)) {
			player.addTag("jel:auto_reload");
			JelTraits.setCooldown(player, Archery.AUTO_LOADER, 100);
		}
	}
}
