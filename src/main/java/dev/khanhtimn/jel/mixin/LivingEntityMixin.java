package dev.khanhtimn.jel.mixin;

import dev.khanhtimn.jel.common.effect.JelDisplacedEffects;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements JelDisplacedEffects {

    @Unique
    private Map<Holder<MobEffect>, MobEffectInstance> jel$displacedEffects;

    @Override
    public void jel$saveDisplacedEffect(Holder<MobEffect> type, MobEffectInstance instance) {
        if (jel$displacedEffects == null) {
            jel$displacedEffects = new HashMap<>();
        }
        jel$displacedEffects.put(type, instance);
    }

    @Override
    @Nullable
    public MobEffectInstance jel$popDisplacedEffect(Holder<MobEffect> type) {
        if (jel$displacedEffects == null) return null;
        return jel$displacedEffects.remove(type);
    }
}
