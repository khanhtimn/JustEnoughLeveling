package dev.khanhtimn.jel.mixin.entity;

import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Farming;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin extends Animal {

	@Shadow
	public abstract boolean isTame();

	protected TamableAnimalMixin(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(
			method = "wantsToAttack",
			at = @At("HEAD"),
			cancellable = true
	)
	private void jel$alphaOfPack(LivingEntity target, LivingEntity owner, CallbackInfoReturnable<Boolean> cir) {
		if (!isTame()) return;
		if (!(owner instanceof ServerPlayer player)) return;

		float active = JelTraits.branchValue(player, ModSkills.FARMING,
				Farming.RANCHER_BRANCH, Farming.ALPHA_OF_PACK_ACTIVE);
		if (active <= 0) return;

		if (jel$isRetaliation(player, target)) {
			cir.setReturnValue(true);
		}
	}

	@Unique
	private static boolean jel$isRetaliation(ServerPlayer player, LivingEntity target) {
		Entity lastAttacker = player.getLastDamageSource() != null
				? player.getLastDamageSource().getEntity() : null;
		return target == lastAttacker;
	}
}
