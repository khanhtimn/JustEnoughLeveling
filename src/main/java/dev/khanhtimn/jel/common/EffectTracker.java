package dev.khanhtimn.jel.common;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

/**
 * Duck interface injected into {@link net.minecraft.world.entity.LivingEntity}
 * to track mob effects managed by JEL's perk system.
 * <p>
 * Backed by a single {@code Map<Holder<MobEffect>, MobEffectInstance>} where
 * key presence indicates JEL ownership and the value (nullable) is the
 * displaced effect snapshot to restore on release.
 */
public interface EffectTracker {

	/**
	 * Claim ownership of an effect type. Optionally stores a displaced
	 * effect that was overwritten by JEL's {@code addEffect()}.
	 *
	 * @param displaced snapshot of the pre-existing non-JEL effect, or null
	 *                  if there was nothing to displace
	 */
	void jel$claimEffect(Holder<MobEffect> type, @Nullable MobEffectInstance displaced);

	/**
	 * Release ownership of an effect type.
	 *
	 * @return the displaced effect to restore, or null if nothing was displaced
	 */
	@Nullable
	MobEffectInstance jel$releaseEffect(Holder<MobEffect> type);

	boolean jel$ownsEffect(Holder<MobEffect> type);
}
