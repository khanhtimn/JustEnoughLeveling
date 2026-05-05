package dev.khanhtimn.jel.mixin.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

	@Inject(method = "onHitEntity", at = @At("TAIL"))
	private void jel$arrowHitEffects(EntityHitResult hitResult, CallbackInfo ci) {
		AbstractArrow self = (AbstractArrow) (Object) this;
		Level level = self.level();
		if (level.isClientSide()) return;

		Entity hit = hitResult.getEntity();
		if (!(hit instanceof LivingEntity living)) return;

		// Frozen Arrow (Sniper) — apply Slowness + Weakness
		if (self.getTags().contains("jel:frozen_arrow")) {
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
			living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
		}

		// Explosive Bolt (Volley) — create explosion at impact
		if (self.getTags().contains("jel:explosive_bolt")
				&& level instanceof ServerLevel serverLevel) {
			serverLevel.explode(
					self.getOwner(), self.getX(), self.getY(), self.getZ(),
					2.0f, Level.ExplosionInteraction.NONE);
			self.removeTag("jel:explosive_bolt");
		}

		// Chain Bolt (Volley) — damage nearby entities (single-pass tag scan)
		if (level instanceof ServerLevel serverLevel) {
			int chainTargets = jel$parseChainTargets(self);
			if (chainTargets > 0) {
				float chainDamage = (float) (self.getBaseDamage() * 0.5);
				int chained = 0;
				for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
						LivingEntity.class, hit.getBoundingBox().inflate(5.0),
						e -> e != hit && e != self.getOwner())) {
					if (chained >= chainTargets) break;
					nearby.hurt(self.damageSources().arrow(self, self.getOwner()), chainDamage);
					chained++;
				}
			}
		}
	}

	@Unique
	private static int jel$parseChainTargets(AbstractArrow self) {
		for (String tag : self.getTags()) {
			if (tag.startsWith("jel:chain_targets:")) {
				try {
					return Integer.parseInt(tag.substring("jel:chain_targets:".length()));
				} catch (NumberFormatException e) {
					return 2;
				}
			}
		}
		return 0;
	}
}
