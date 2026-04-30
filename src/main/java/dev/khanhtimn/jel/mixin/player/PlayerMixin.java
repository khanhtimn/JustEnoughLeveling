package dev.khanhtimn.jel.mixin.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.api.loot.JelLootContexts;
import dev.khanhtimn.jel.content.skills.Agility;
import dev.khanhtimn.jel.content.skills.Magic;
import dev.khanhtimn.jel.content.skills.Melee;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
	@Unique
	private final Player jel$self = (Player) (Object) this;

	@Unique
	private boolean jel$reflecting = false;

	@Inject(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
			)
	)
	private void jel$critAttackMixin(Entity entity, CallbackInfo ci,
	                                 @Local(ordinal = 0) LocalFloatRef damageAmount,
	                                 @Local(ordinal = 2) boolean isCrit) {
		if (isCrit) {
			float bonus = JelTraits.value(this.jel$self, Melee.CRIT_DAMAGE_BONUS);
			if (bonus > 0) {
				damageAmount.set(damageAmount.get() * bonus);
			}
		}
	}

	@WrapOperation(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
			)
	)
	private boolean jel$attackMixin(
			Entity target, DamageSource source,
			float damage, Operation<Boolean> original
	) {
		boolean hit = original.call(target, source, damage);
		if (!hit) return false;

		if (!this.jel$self.level().isClientSide()
				&& JelTraits.testChance(this.jel$self, Melee.DOUBLE_DAMAGE_CHANCE)) {
			original.call(target, source, damage);
		}

		return true;
	}

	@ModifyVariable(
			method = "causeFoodExhaustion",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"
			),
			ordinal = 0,
			argsOnly = true
	)
	private float jel$exhaustionMixin(float original) {
		float bonus = JelTraits.value(this.jel$self, Agility.EXHAUSTION_REDUCTION);
		if (bonus > 0) {
			original *= bonus;
		}
		return original;
	}

	@Inject(
			method = "hurt",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;removeEntitiesOnShoulder()V"
			),
			cancellable = true
	)
	private void jel$damageMixin(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!(this.jel$self.level() instanceof ServerLevel serverLevel)) return;

		LootContext damageCtx = JelLootContexts.damage(serverLevel, this.jel$self, 0, source);

		// Dodge check first — if dodged, skip reflection entirely
		if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
				&& JelTraits.testChance(this.jel$self, Agility.MOB_COLLIDE_AVOID, damageCtx)) {
			cir.setReturnValue(false);
			return;
		}

		// Damage reflection — guarded against infinite recursion
		if (!jel$reflecting && source.getEntity() != null
				&& JelTraits.testChance(this.jel$self, Magic.REFLECT_CHANCE, damageCtx)) {
			jel$reflecting = true;
			try {
				float reflectMultiplier = JelTraits.value(this.jel$self, Magic.REFLECT_MULTIPLIER);
				source.getEntity().hurt(this.jel$self.damageSources().thorns(this.jel$self), amount * reflectMultiplier);
			} finally {
				jel$reflecting = false;
			}
		}
	}
}
