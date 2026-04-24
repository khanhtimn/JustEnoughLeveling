package dev.khanhtimn.jel.mixin.entity;

import java.util.HashMap;
import java.util.Map;

import dev.khanhtimn.jel.common.EffectTracker;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

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
}
