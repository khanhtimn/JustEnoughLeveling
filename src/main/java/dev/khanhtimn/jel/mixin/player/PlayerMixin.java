package dev.khanhtimn.jel.mixin.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.api.loot.JelLootContexts;
import dev.khanhtimn.jel.common.ComboTracker;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Agility;
import dev.khanhtimn.jel.content.skills.Constitution;
import dev.khanhtimn.jel.content.skills.Magic;
import dev.khanhtimn.jel.content.skills.Melee;
import dev.khanhtimn.jel.misc.CombatTraitAccessor;
import dev.khanhtimn.jel.misc.JelFoodDataAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin implements CombatTraitAccessor {
	@Unique
	private final Player jel$self = (Player) (Object) this;

	@Unique
	private boolean jel$reflecting = false;

	@Unique
	private final ComboTracker jel$comboTracker = new ComboTracker();

	@Unique
	private int jel$sprintTicks;

	@Override
	public ComboTracker jel$getComboTracker() {
		return jel$comboTracker;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void jel$initFoodDataOwner(CallbackInfo ci) {
		((JelFoodDataAccess) this.jel$self.getFoodData()).jel$setPlayer(this.jel$self);
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
		if (this.jel$self.level().isClientSide()) {
			return original.call(target, source, damage);
		}

		float modifiedDamage = damage;

		// Critical Fury (Berserker) — enhanced critical hit damage
		// bl3 in vanilla: falling + not on ground + not climbing + not in water + no blindness + not riding + not sprinting
		float critBonus = JelTraits.branchValue(this.jel$self, ModSkills.MELEE,
				Melee.BERSERKER_BRANCH, Melee.CRITICAL_FURY_BONUS);
		if (critBonus > 0 && jel$isVanillaCrit()) {
			modifiedDamage *= (1.0f + critBonus);
		}

		// Adrenaline (Berserker) — bonus damage when below 30% HP
		float adrenaline = JelTraits.branchValue(this.jel$self, ModSkills.MELEE,
				Melee.BERSERKER_BRANCH, Melee.ADRENALINE_DAMAGE);
		if (adrenaline > 0 && this.jel$self.getHealth() / this.jel$self.getMaxHealth() < 0.3f) {
			modifiedDamage *= (1.0f + adrenaline);
		}

		// Combo Finisher (Duelist) — every 3rd hit deals bonus damage
		if (target instanceof LivingEntity living) {
			float comboBonus = JelTraits.branchValue(this.jel$self, ModSkills.MELEE,
					Melee.DUELIST_BRANCH, Melee.COMBO_FINISHER_DAMAGE);
			if (comboBonus > 0) {
				int hits = jel$comboTracker.recordHit(living.getUUID(),
						this.jel$self.level().getGameTime());
				if (hits % 3 == 0) {
					modifiedDamage *= (1.0f + comboBonus);
				}
			}
		}

		damage = modifiedDamage;

		boolean hit = original.call(target, source, damage);

		if (!hit) return false;
		if (!(target instanceof LivingEntity living)) return true;

		if (JelSkills.isBranch(this.jel$self, ModSkills.MELEE, Melee.BERSERKER_BRANCH)) {
			// Bleeding Edge — apply Wither on hit
			if (JelTraits.testChance(this.jel$self, Melee.BLEEDING_EDGE_CHANCE)) {
				living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
			}

			// Ground Slam — AOE knockback + damage around target (cooldown-gated)
			if (JelTraits.testChance(this.jel$self, Melee.GROUND_SLAM_CHANCE)
					&& this.jel$self.level() instanceof ServerLevel serverLevel
					&& !JelTraits.isOnCooldown(this.jel$self, Melee.GROUND_SLAM)) {
				float slamDamage = damage * 0.4f;
				for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
						LivingEntity.class, target.getBoundingBox().inflate(3.0),
						e -> e != this.jel$self && e != target && !this.jel$self.isAlliedTo(e))) {
					nearby.hurt(this.jel$self.damageSources().playerAttack(this.jel$self), slamDamage);
					nearby.knockback(0.6, this.jel$self.getX() - nearby.getX(),
							this.jel$self.getZ() - nearby.getZ());
				}
				JelTraits.setCooldown(this.jel$self, Melee.GROUND_SLAM, 200);
			}
		}

		if (JelSkills.isBranch(this.jel$self, ModSkills.MELEE, Melee.DUELIST_BRANCH)) {
			// Life Steal — heal % of damage dealt
			float lifeSteal = JelTraits.value(this.jel$self, Melee.LIFE_STEAL_PERCENT);
			if (lifeSteal > 0) {
				this.jel$self.heal(damage * lifeSteal);
			}

			// Thousand Cuts — stacking Poison DOT (capped at amplifier 3)
			float dot = JelTraits.value(this.jel$self, Melee.THOUSAND_CUTS_DOT);
			if (dot > 0) {
				MobEffectInstance existing = living.getEffect(MobEffects.POISON);
				int amplifier = existing != null ? Math.min(existing.getAmplifier() + 1, 3) : 0;
				living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, amplifier));
			}

			// Disarm — disable target's shield (PvP)
			if (living instanceof Player targetPlayer
					&& JelTraits.testChance(this.jel$self, Melee.DISARM_CHANCE)) {
				jel$disarmPlayer(targetPlayer);
			}

			// Mark of Death — every 3rd consecutive hit marks target
			float markBonus = JelTraits.value(this.jel$self, Melee.MARK_OF_DEATH_BONUS);
			if (markBonus > 0) {
				int hits = jel$comboTracker.getComboCount(living.getUUID(),
						this.jel$self.level().getGameTime());
				if (hits >= 3 && hits % 3 == 0) {
					living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
					living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
				}
			}
		}

		return true;
	}


	@Inject(
			method = "attack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"
			)
	)
	private void jel$postKillCheck(Entity entity, CallbackInfo ci) {
		if (this.jel$self.level().isClientSide()) return;
		if (!(entity instanceof LivingEntity living) || living.isAlive()) return;

		// Bloodlust (Berserker) — kill grants Haste (attack speed proxy)
		float speedBonus = JelTraits.branchValue(this.jel$self, ModSkills.MELEE,
				Melee.BERSERKER_BRANCH, Melee.BLOODLUST_SPEED);
		if (speedBonus > 0) {
			int amplifier = Math.min((int) (speedBonus * 10), 2);
			this.jel$self.addEffect(new MobEffectInstance(
					MobEffects.DIG_SPEED, 100, amplifier, true, false, true));
		}

		jel$comboTracker.onTargetDeath(living.getUUID());
	}


	@Inject(method = "hurt", at = @At("HEAD"))
	private void jel$wrathCheck(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (this.jel$self.level().isClientSide()) return;
		if (!JelSkills.isBranch(this.jel$self, ModSkills.MELEE, Melee.BERSERKER_BRANCH)) return;

		float threshold = JelTraits.value(this.jel$self, Melee.WRATH_THRESHOLD);
		if (threshold <= 0) return;

		float hpAfterHit = (this.jel$self.getHealth() - amount) / this.jel$self.getMaxHealth();
		if (hpAfterHit < threshold && !this.jel$self.hasEffect(MobEffects.DAMAGE_BOOST)
				&& !JelTraits.isOnCooldown(this.jel$self, Melee.WRATH)) {
			this.jel$self.addEffect(new MobEffectInstance(
					MobEffects.DAMAGE_BOOST, 200, 1, true, false, true));
			this.jel$self.addEffect(new MobEffectInstance(
					MobEffects.MOVEMENT_SPEED, 200, 0, true, false, true));
			this.jel$self.addEffect(new MobEffectInstance(
					MobEffects.FIRE_RESISTANCE, 200, 0, true, false, true));
			JelTraits.setCooldown(this.jel$self, Melee.WRATH, 1200);
		}
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

		// Dodge check (Agility — non-branched)
		if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
				&& JelTraits.testChance(this.jel$self, Agility.MOB_COLLIDE_AVOID, damageCtx)) {
			cir.setReturnValue(false);
			return;
		}

		// Acrobat (Agility — Parkour): chance to dodge projectiles
		if (source.is(DamageTypeTags.IS_PROJECTILE)
				&& !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
				&& JelTraits.branchTestChance(this.jel$self, ModSkills.AGILITY,
						Agility.PARKOUR_BRANCH, Agility.ACROBAT_CHANCE)) {
			cir.setReturnValue(false);
			return;
		}

		// Riposte (Duelist) — counter-debuff attacker
		if (source.getEntity() instanceof LivingEntity attacker
				&& JelTraits.branchTestChance(this.jel$self, ModSkills.MELEE,
						Melee.DUELIST_BRANCH, Melee.RIPOSTE_CHANCE)) {
			attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
			attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
		}

		// Damage reflection (Magic)
		if (!jel$reflecting && source.getEntity() != null
				&& JelTraits.testChance(this.jel$self, Magic.REFLECT_CHANCE, damageCtx)) {
			jel$reflecting = true;
			try {
				float reflectMultiplier = JelTraits.value(this.jel$self, Magic.REFLECT_MULTIPLIER);
				source.getEntity().hurt(
						this.jel$self.damageSources().thorns(this.jel$self),
						amount * reflectMultiplier);
			} finally {
				jel$reflecting = false;
			}
		}
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
		// Efficient Digestion (Constitution — Sustenance): reduces all exhaustion
		float digestion = JelTraits.branchValue(this.jel$self, ModSkills.CONSTITUTION,
				Constitution.SUSTENANCE_BRANCH, Constitution.DIGESTION_REDUCTION);
		if (digestion > 0) {
			original *= (1.0f - digestion);
		}

		// Nomad (Agility — Explorer capstone): zeroes movement exhaustion
		if (JelTraits.branchHas(this.jel$self, ModSkills.AGILITY,
				Agility.EXPLORER_BRANCH, Agility.NOMAD)) {
			return 0;
		}

		// Sure-Footed (Explorer): reduces movement exhaustion
		float sureFoot = JelTraits.branchValue(this.jel$self, ModSkills.AGILITY,
				Agility.EXPLORER_BRANCH, Agility.SURE_FOOTED_REDUCTION);
		if (sureFoot > 0) {
			original *= (1.0f - sureFoot);
		}

		return original;
	}

	@Unique
	private boolean jel$isVanillaCrit() {
		Player self = this.jel$self;
		return self.fallDistance > 0.0f
				&& !self.onGround()
				&& !self.onClimbable()
				&& !self.isInWater()
				&& !self.hasEffect(MobEffects.BLINDNESS)
				&& !self.isPassenger()
				&& !self.isSprinting();
	}

	@Unique
	private void jel$disarmPlayer(Player target) {
		for (InteractionHand hand : InteractionHand.values()) {
			if (target.getItemInHand(hand).getItem() instanceof ShieldItem) {
				target.getCooldowns().addCooldown(target.getItemInHand(hand).getItem(), 100);
				target.stopUsingItem();
				return;
			}
		}
	}

	// Second Wind + Undying Will: post-damage death-defiance
	@Inject(
			method = "hurt",
			at = @At("RETURN")
	)
	private void jel$constitutionPostDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (this.jel$self.level().isClientSide()) return;
		if (!cir.getReturnValue()) return;
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
		if (!JelSkills.isBranch(this.jel$self, ModSkills.CONSTITUTION, Constitution.VITALITY_BRANCH)) return;

		// Second Wind: if HP dropped below threshold, heal back
		if (this.jel$self.getHealth() > 0) {
			float swThreshold = JelTraits.value(this.jel$self, Constitution.SECOND_WIND_THRESHOLD);
			if (swThreshold > 0
					&& this.jel$self.getHealth() / this.jel$self.getMaxHealth() < swThreshold
					&& !JelTraits.isOnCooldown(this.jel$self, Constitution.SECOND_WIND)) {
				float healTo = this.jel$self.getMaxHealth() * swThreshold;
				this.jel$self.setHealth(healTo);
				this.jel$self.addEffect(new MobEffectInstance(
						MobEffects.REGENERATION, 100, 1, true, false, true));
				int cd = (int) JelTraits.value(this.jel$self, Constitution.SECOND_WIND_COOLDOWN);
				JelTraits.setCooldown(this.jel$self, Constitution.SECOND_WIND, cd > 0 ? cd : 2400);
			}
		}

		// Undying Will: prevent fatal damage (death-defiance)
		if (this.jel$self.getHealth() <= 0) {
			float uwThreshold = JelTraits.value(this.jel$self, Constitution.UNDYING_WILL_THRESHOLD);
			if (uwThreshold > 0
					&& !JelTraits.isOnCooldown(this.jel$self, Constitution.UNDYING_WILL)) {
				this.jel$self.setHealth(this.jel$self.getMaxHealth() * uwThreshold);
				this.jel$self.addEffect(new MobEffectInstance(
						MobEffects.ABSORPTION, 200, 4, true, false, true));
				this.jel$self.addEffect(new MobEffectInstance(
						MobEffects.REGENERATION, 200, 2, true, false, true));
				this.jel$self.addEffect(new MobEffectInstance(
						MobEffects.DAMAGE_RESISTANCE, 60, 4, true, false, true));
				int cd = (int) JelTraits.value(this.jel$self, Constitution.UNDYING_WILL_COOLDOWN);
				JelTraits.setCooldown(this.jel$self, Constitution.UNDYING_WILL, cd > 0 ? cd : 6000);
			}
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void jel$traitTickHandler(CallbackInfo ci) {
		if (this.jel$self.level().isClientSide()) return;

		// Avatar of Life (Constitution — Vitality capstone)
		if (JelTraits.branchHas(this.jel$self, ModSkills.CONSTITUTION,
				Constitution.VITALITY_BRANCH, Constitution.AVATAR_OF_LIFE)
				&& !this.jel$self.hasEffect(MobEffects.REGENERATION)) {
			this.jel$self.addEffect(new MobEffectInstance(
					MobEffects.REGENERATION, 80, 0, true, false, true));
		}

		// Agility tick-based traits
		jel$agilityTick();
	}

	@Unique
	private void jel$agilityTick() {
		// Momentum (Parkour): speed bonus after sustained sprinting
		if (JelSkills.isBranch(this.jel$self, ModSkills.AGILITY, Agility.PARKOUR_BRANCH)) {
			if (this.jel$self.isSprinting()) {
				jel$sprintTicks++;
				int threshold = (int) JelTraits.value(this.jel$self, Agility.MOMENTUM_THRESHOLD);
				if (threshold > 0 && jel$sprintTicks >= threshold) {
					float bonus = JelTraits.value(this.jel$self, Agility.MOMENTUM_BONUS);
					if (bonus > 0 && !this.jel$self.hasEffect(MobEffects.MOVEMENT_SPEED)) {
						int amplifier = Math.min((int) (bonus * 10), 2);
						this.jel$self.addEffect(new MobEffectInstance(
								MobEffects.MOVEMENT_SPEED, 40, amplifier, true, false, true));
					}
				}
			} else {
				jel$sprintTicks = 0;
			}

			// Sprint Surge: activated sprint burst (cooldown-gated)
			if (this.jel$self.isSprinting()) {
				int surgeDuration = (int) JelTraits.value(this.jel$self, Agility.SPRINT_SURGE_DURATION);
				if (surgeDuration > 0 && !JelTraits.isOnCooldown(this.jel$self, Agility.SPRINT_SURGE)) {
					this.jel$self.addEffect(new MobEffectInstance(
							MobEffects.MOVEMENT_SPEED, surgeDuration, 1, true, false, true));
					int cd = (int) JelTraits.value(this.jel$self, Agility.SPRINT_SURGE_COOLDOWN);
					JelTraits.setCooldown(this.jel$self, Agility.SPRINT_SURGE, cd > 0 ? cd : 300);
				}
			}

			// Windrunner (capstone): permanent Speed I
			if (JelTraits.has(this.jel$self, Agility.WINDRUNNER)
					&& !this.jel$self.hasEffect(MobEffects.MOVEMENT_SPEED)) {
				this.jel$self.addEffect(new MobEffectInstance(
						MobEffects.MOVEMENT_SPEED, 80, 0, true, false, true));
			}
		}

		// Explorer branch tick-based traits
		if (JelSkills.isBranch(this.jel$self, ModSkills.AGILITY, Agility.EXPLORER_BRANCH)) {
			// Night Eyes: Night Vision when light level is below threshold
			int nightThreshold = (int) JelTraits.value(this.jel$self, Agility.NIGHT_EYES_THRESHOLD);
			if (nightThreshold > 0) {
				int light = this.jel$self.level().getMaxLocalRawBrightness(this.jel$self.blockPosition());
				if (light <= nightThreshold && !this.jel$self.hasEffect(MobEffects.NIGHT_VISION)) {
					this.jel$self.addEffect(new MobEffectInstance(
							MobEffects.NIGHT_VISION, 300, 0, true, false, true));
				}
			}

			// Glider: slow falling when airborne and sneaking
			float gliderBonus = JelTraits.value(this.jel$self, Agility.GLIDER_BONUS);
			if (gliderBonus > 0 && !this.jel$self.onGround()
					&& this.jel$self.isShiftKeyDown()
					&& this.jel$self.getDeltaMovement().y < 0
					&& !this.jel$self.hasEffect(MobEffects.SLOW_FALLING)) {
				this.jel$self.addEffect(new MobEffectInstance(
						MobEffects.SLOW_FALLING, 20, 0, true, false, true));
			}

			// Nomad (capstone): permanent Speed I
			if (JelTraits.has(this.jel$self, Agility.NOMAD)
					&& !this.jel$self.hasEffect(MobEffects.MOVEMENT_SPEED)) {
				this.jel$self.addEffect(new MobEffectInstance(
						MobEffects.MOVEMENT_SPEED, 80, 0, true, false, true));
			}
		}
	}
}
