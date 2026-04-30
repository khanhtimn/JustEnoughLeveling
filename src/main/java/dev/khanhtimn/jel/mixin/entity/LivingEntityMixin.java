package dev.khanhtimn.jel.mixin.entity;

import java.util.HashMap;
import java.util.Map;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.api.loot.JelLootContexts;
import dev.khanhtimn.jel.common.EffectTracker;
import dev.khanhtimn.jel.content.skills.Agility;
import dev.khanhtimn.jel.content.skills.Defense;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements EffectTracker {

	@Unique
	private Map<Holder<MobEffect>, MobEffectInstance> jel$managedEffects;

	@Override
	public void jel$claimEffect(Holder<MobEffect> type, @Nullable MobEffectInstance displaced) {
		if (jel$managedEffects == null) {
			jel$managedEffects = new HashMap<>();
		}
		jel$managedEffects.put(type, displaced);
	}

	@Override
	@Nullable
	public MobEffectInstance jel$releaseEffect(Holder<MobEffect> type) {
		if (jel$managedEffects == null) return null;
		return jel$managedEffects.remove(type);
	}

	@Override
	public boolean jel$ownsEffect(Holder<MobEffect> type) {
		return jel$managedEffects != null && jel$managedEffects.containsKey(type);
	}

	@Definition(id = "values", method = "Lnet/minecraft/world/InteractionHand;values()[Lnet/minecraft/world/InteractionHand;")
	@Expression("? = values()")
	@Inject(
			method = "checkTotemDeathProtection",
			at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER),
			cancellable = true
	)
	private void jel$deathEventMixin(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Player self
				&& self.level() instanceof ServerLevel serverLevel) {
			LootContext ctx = JelLootContexts.damage(serverLevel, self, 0, damageSource);
			if (JelTraits.testChance(self, Defense.DEATH_GRACE_CHANCE, ctx)) {
				self.setHealth(1.0F);
				self.removeAllEffects();
				self.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
				self.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
				cir.setReturnValue(true);
			}
		}
	}

	@Definition(id = "getDamageProtection", method = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageProtection(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)F")
	@Expression("? = getDamageProtection(?, this, ?)")
	@ModifyVariable(
			method = "getDamageAfterMagicAbsorb",
			at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER),
			ordinal = 1
	)
	private float jel$damageProtectionMixin(float original, DamageSource source, float amount) {
		if ((Object) this instanceof Player player) {
			if (source.is(DamageTypes.FALL)) {
				return original + JelTraits.value(player, Agility.FALL_DAMAGE_REDUCTION);
			}
		}
		return original;
	}
}
