package dev.khanhtimn.jel.mixin.entity;

import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.skills.Batering;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {

	@Shadow
	@Nullable
	private Player lastTradedPlayer;

	public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(
			method = "rewardTradeXp",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
			),
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	protected void jel$rewardTradeXpMixin(MerchantOffer merchantOffer, CallbackInfo ci, int i) {
		if (this.lastTradedPlayer instanceof Player player) {
			int amount = (int) (i * JelTraits.value(player, Batering.TRADE_XP_MULTIPLIER));
			if (amount > 0) {
				ExperienceOrb.award((ServerLevel) player.level(), this.position().add(0.0D, 0.5D, 0.0D), amount);
			}
		}
	}

	@Inject(
			method = "updateSpecialPrices",
			at = @At(value = "TAIL")
	)
	private void jel$updateSpecialPricesMixin(Player player, CallbackInfo ci) {
		if (!player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) {
			for (MerchantOffer merchantOffer : this.getOffers()) {
				int originalPrice = merchantOffer.getBaseCostA().getCount();
//				 TODO: Tweak this to allign with
//				if (level >= skillBonus.getLevel()) {
//					return 1.0f - (level * ConfigInit.CONFIG.priceDiscountBonus);
//				}
//				return 1.0f;

//				And at call site:
//				merchantOffer.addToSpecialPriceDiff(-(int) (originalPrice - originalPrice * BonusHelper.priceDiscountBonus(player)));
//				but calculated using LevelBasedValue

				merchantOffer.addToSpecialPriceDiff(-(int) (originalPrice - originalPrice * JelTraits.value(player, Batering.TRADE_PRICE_DISCOUNT)));
			}
		}
	}

	@Inject(
			method = "setLastHurtByMob",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;onReputationEvent(Lnet/minecraft/world/entity/ai/village/ReputationEventType;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/ReputationEventHandler;)V"
			),
			cancellable = true
	)
	private void jel$setLastHurtByMobMixin(LivingEntity attacker, CallbackInfo ci) {
		if (attacker instanceof Player player && JelTraits.has(player, Batering.TRADE_IMMUNITY)) {
			super.setLastHurtByMob(attacker);
			ci.cancel();
		}
	}

}
