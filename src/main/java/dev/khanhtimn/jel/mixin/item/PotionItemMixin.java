package dev.khanhtimn.jel.mixin.item;

import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.skills.Magic;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {

	@ModifyVariable(
			//? if fabric {
			method = "method_57389",
			//?} else {
			/*method = "lambda$finishUsingItem$0",
			*///?}
			at = @At("HEAD"),
			argsOnly = true
	)
	private static MobEffectInstance jel$applyMobEffectMixin(MobEffectInstance original, Player player, LivingEntity livingEntity) {
		if (player != null) {
			if (JelTraits.testChance(player, Magic.UPGRADED_EFFECT_CHANCE)) {
				return new MobEffectInstance(original.getEffect(), original.getDuration(),
						original.getAmplifier() + 1, original.isAmbient(),
						original.isVisible(), original.showIcon());
			}
		}
		return original;
	}
}
