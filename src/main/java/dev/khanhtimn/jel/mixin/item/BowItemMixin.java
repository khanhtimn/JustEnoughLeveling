package dev.khanhtimn.jel.mixin.item;

import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
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
	private void jel$bowShootMixin(
			LivingEntity shooter, Projectile projectile,
			int index, float speed,
			float divergence, float yaw,
			LivingEntity target, CallbackInfo ci
	) {
		if (!(shooter instanceof Player player) || !(projectile instanceof AbstractArrow arrow)) return;
		if (player.level().isClientSide()) return;

		// Base bow damage bonus (available to all archery users)
		float bonus = JelTraits.value(player, Archery.BOW_DAMAGE_BONUS);
		if (bonus > 0) {
			arrow.setBaseDamage(arrow.getBaseDamage() + bonus);
		}

		if (!JelSkills.isBranch(player, ModSkills.ARCHERY, Archery.SNIPER_BRANCH)) return;

		// Headshot (Sniper) — full-charge bonus damage
		float headshotBonus = JelTraits.value(player, Archery.HEADSHOT_BONUS);
		if (headshotBonus > 0 && speed >= 1.0f) {
			arrow.setBaseDamage(arrow.getBaseDamage() * (1.0 + headshotBonus));
		}

		// Bow Crit (Sniper) — double damage chance
		if (JelTraits.testChance(player, Archery.BOW_CRIT_CHANCE)) {
			arrow.setBaseDamage(arrow.getBaseDamage() * 2D);
			arrow.setCritArrow(true);
		}

		// Piercing Shot (Sniper) — arrow passes through entities
		if (JelTraits.testChance(player, Archery.PIERCING_SHOT_CHANCE)) {
			arrow.setPierceLevel((byte) 3);
		}

		// Frozen Arrow (Sniper) — chance to apply slowness on hit via tag
		// The actual effect application happens in AbstractArrowMixin on hit
		if (JelTraits.testChance(player, Archery.FROZEN_ARROW_CHANCE)) {
			arrow.addTag("jel:frozen_arrow");
		}
	}
}
