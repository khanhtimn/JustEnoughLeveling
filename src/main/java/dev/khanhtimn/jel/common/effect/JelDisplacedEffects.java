package dev.khanhtimn.jel.common.effect;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public interface JelDisplacedEffects {

    void jel$saveDisplacedEffect(Holder<MobEffect> type, MobEffectInstance instance);

    @Nullable
    MobEffectInstance jel$popDisplacedEffect(Holder<MobEffect> type);
}
