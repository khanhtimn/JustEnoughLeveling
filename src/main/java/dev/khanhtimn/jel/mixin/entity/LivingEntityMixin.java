package dev.khanhtimn.jel.mixin.entity;

import java.util.HashMap;
import java.util.Map;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.content.skills.Farming;
import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.api.loot.JelLootContexts;
import dev.khanhtimn.jel.common.EffectTracker;
import dev.khanhtimn.jel.common.PlayerSkillData;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Agility;
import dev.khanhtimn.jel.content.skills.Constitution;
import dev.khanhtimn.jel.content.skills.Defense;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements EffectTracker {

	@Shadow
	public abstract float getHealth();

	@Shadow
	public abstract float getMaxHealth();

	@Unique
	private Map<Holder<MobEffect>, MobEffectInstance> jel$managedEffects;

	@Unique
	private static final ResourceLocation JEL_THORNS_NOVA_CD =
			Constants.rl("defense/thorns_nova_cd");

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
		if (!((Object) this instanceof Player self)) return;
		if (!(self.level() instanceof ServerLevel serverLevel)) return;

		PlayerSkillData data = JelSkills.getSkillData(self);
		if (data == null) return;

		LootContext ctx = JelLootContexts.damage(serverLevel, self, 0, damageSource);

		// Death Grace (Fortress) — cheat death once on cooldown
		if (data.isBranch(ModSkills.DEFENSE, Defense.FORTRESS_BRANCH)
				&& JelTraits.testChance(self, Defense.DEATH_GRACE_CHANCE, ctx)
				&& !JelTraits.isOnCooldown(self, Defense.DEATH_GRACE)) {
			self.setHealth(1.0F);
			self.removeAllEffects();
			self.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
			self.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
			self.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
			JelTraits.setCooldown(self, Defense.DEATH_GRACE, 6000);
			cir.setReturnValue(true);
			return;
		}

		// Second Wind (Sentinel) — heal burst when near death
		float secondWindHeal = JelTraits.branchValue(self, ModSkills.DEFENSE,
				Defense.SENTINEL_BRANCH, Defense.SECOND_WIND_HEAL);
		if (secondWindHeal > 0 && !JelTraits.isOnCooldown(self, Defense.SECOND_WIND)) {
			self.setHealth(1.0F);
			self.heal(self.getMaxHealth() * secondWindHeal);
			self.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
			JelTraits.setCooldown(self, Defense.SECOND_WIND, 6000);
			cir.setReturnValue(true);
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
		if (!((Object) this instanceof Player player)) return original;

		// Feather Landing fall damage reduction (Agility — Parkour branch)
		if (source.is(DamageTypes.FALL)) {
			float feather = JelTraits.branchValue(player, ModSkills.AGILITY,
					Agility.PARKOUR_BRANCH, Agility.FEATHER_LANDING_REDUCTION);
			if (feather > 0) {
				original += feather;
			}
		}

		// Fortitude (Constitution — Vitality): DR bonus when below 30% HP
		float fortDr = JelTraits.branchValue(player, ModSkills.CONSTITUTION,
				Constitution.VITALITY_BRANCH, Constitution.FORTITUDE_DR);
		if (fortDr > 0 && player.getHealth() / player.getMaxHealth() < 0.3f) {
			original += amount * fortDr;
		}

		// Thick Skin (Agility — Explorer): reduces environmental damage
		if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.FALL)
				|| source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
				|| source.is(DamageTypes.FREEZE)) {
			float thickSkin = JelTraits.branchValue(player, ModSkills.AGILITY,
					Agility.EXPLORER_BRANCH, Agility.THICK_SKIN_REDUCTION);
			if (thickSkin > 0) {
				original += amount * thickSkin;
			}
		}

		// Flat damage reduction (Fortress)
		if (JelSkills.isBranch(player, ModSkills.DEFENSE, Defense.FORTRESS_BRANCH)) {
			float reduction = JelTraits.value(player, Defense.DAMAGE_REDUCTION_PERCENT);
			if (reduction > 0) {
				original += amount * reduction;
			}

			// Elemental Ward (Fortress) — fire/explosion reduction
			float elemental = JelTraits.value(player, Defense.ELEMENTAL_WARD_PERCENT);
			if (elemental > 0 && (source.is(DamageTypeTags.IS_FIRE)
					|| source.is(DamageTypeTags.IS_EXPLOSION))) {
				original += amount * elemental;
			}

			// Iron Curtain (Fortress capstone) — crouch + blocking = massive reduction
			float ironCurtain = JelTraits.value(player, Defense.IRON_CURTAIN_REDUCTION);
			if (ironCurtain > 0 && player.isShiftKeyDown() && player.isBlocking()) {
				original += amount * ironCurtain;
			}
		}

		// Parry (Sentinel) — blocking negates damage and staggers attacker
		if (player.isBlocking()
				&& JelTraits.branchTestChance(player, ModSkills.DEFENSE,
				Defense.SENTINEL_BRANCH, Defense.PARRY_CHANCE)) {
			if (source.getEntity() instanceof LivingEntity attacker) {
				attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
			}
			return amount;
		}

		return original;
	}

	@ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float jel$packLeaderDamage(float amount, DamageSource source) {
		if (!(source.getEntity() instanceof TamableAnimal tamed)) return amount;
		if (!tamed.isTame()) return amount;
		if (!(tamed.getOwner() instanceof net.minecraft.server.level.ServerPlayer owner)) return amount;

		float bonus = JelTraits.branchValue(owner, ModSkills.FARMING,
				Farming.RANCHER_BRANCH, Farming.PACK_LEADER_DAMAGE);
		if (bonus <= 0) return amount;
		return amount * (1.0f + bonus);
	}

	@Inject(method = "hurt", at = @At("RETURN"))
	private void jel$defensePostHitMixin(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) return;
		if (!((Object) this instanceof Player self)) return;
		if (!(self.level() instanceof ServerLevel serverLevel)) return;

		// Fortress branch post-hit effects
		if (JelSkills.isBranch(self, ModSkills.DEFENSE, Defense.FORTRESS_BRANCH)) {
			// Thorns Nova — AOE damage pulse when hit (cooldown-gated to prevent spam)
			float thornsDamage = JelTraits.value(self, Defense.THORNS_NOVA_DAMAGE);
			if (thornsDamage > 0 && !JelTraits.isOnCooldown(self, JEL_THORNS_NOVA_CD)) {
				for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
						LivingEntity.class, self.getBoundingBox().inflate(4.0),
						e -> e != self && !self.isAlliedTo(e))) {
					nearby.hurt(self.damageSources().thorns(self), thornsDamage);
				}
				JelTraits.setCooldown(self, JEL_THORNS_NOVA_CD, 40);
			}
		}

		// Sentinel branch post-hit effects
		if (JelSkills.isBranch(self, ModSkills.DEFENSE, Defense.SENTINEL_BRANCH)) {
			LivingEntity attacker = source.getEntity() instanceof LivingEntity le ? le : null;

			// Thorns Reflect — reflect % of blocked damage to attacker
			if (self.isBlocking() && attacker != null) {
				float reflectPct = JelTraits.value(self, Defense.THORNS_REFLECT_PERCENT);
				if (reflectPct > 0) {
					attacker.hurt(self.damageSources().thorns(self), amount * reflectPct);
				}

				// Shield Bash — blocking counter-attack with knockback
				float bashDamage = JelTraits.value(self, Defense.SHIELD_BASH_DAMAGE);
				if (bashDamage > 0) {
					attacker.hurt(self.damageSources().playerAttack(self), bashDamage);
					attacker.knockback(0.5, self.getX() - attacker.getX(),
							self.getZ() - attacker.getZ());
				}
			}

			// Vengeful Spirit — damage buff when surrounded by hostiles
			float vengeful = JelTraits.value(self, Defense.VENGEFUL_SPIRIT_DAMAGE);
			if (vengeful > 0) {
				long nearbyHostiles = serverLevel.getEntitiesOfClass(
						LivingEntity.class, self.getBoundingBox().inflate(6.0),
						e -> e != self && !self.isAlliedTo(e)).size();
				if (nearbyHostiles >= 3) {
					self.addEffect(new MobEffectInstance(
							MobEffects.DAMAGE_BOOST, 100, 0, true, false, true));
					self.addEffect(new MobEffectInstance(
							MobEffects.DAMAGE_RESISTANCE, 100, 0, true, false, true));
				}
			}

			// Rallying Cry — near death, buff nearby allies (cooldown-gated)
			float radius = JelTraits.value(self, Defense.RALLYING_CRY_RADIUS);
			if (radius > 0 && self.getHealth() / self.getMaxHealth() < 0.2f
					&& !JelTraits.isOnCooldown(self, Defense.RALLYING_CRY)) {
				for (Player ally : serverLevel.getEntitiesOfClass(
						Player.class, self.getBoundingBox().inflate(radius),
						p -> p != self && self.isAlliedTo(p))) {
					ally.addEffect(new MobEffectInstance(
							MobEffects.DAMAGE_RESISTANCE, 200, 0, true, false, true));
					ally.addEffect(new MobEffectInstance(
							MobEffects.REGENERATION, 200, 0, true, false, true));
				}
				JelTraits.setCooldown(self, Defense.RALLYING_CRY, 2400);
			}
		}
	}
}
