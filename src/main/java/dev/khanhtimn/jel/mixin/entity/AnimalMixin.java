package dev.khanhtimn.jel.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Farming;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalMixin extends AgeableMob {

	@Shadow
	@Nullable
	public abstract ServerPlayer getLoveCause();

	protected AnimalMixin(EntityType<? extends AgeableMob> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(
			method = "spawnChildFromBreeding",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"
			)
	)
	private void jel$onBreed(ServerLevel serverLevel, Animal partner, CallbackInfo ci,
							 @Local AgeableMob baby) {
		ServerPlayer player = jel$findBreeder(partner);
		if (player == null) return;

		jel$tryBreedTwin(serverLevel, player, baby, partner);
		jel$applyShepherdsCare(player, baby);
	}

	@Inject(
			method = "finalizeSpawnChildFromBreeding",
			at = @At("TAIL")
	)
	private void jel$modifyBreedingCooldown(ServerLevel serverLevel, Animal partner,
											@Nullable AgeableMob baby, CallbackInfo ci) {
		ServerPlayer player = jel$findBreeder(partner);
		if (player == null) return;

		float penalty = JelTraits.value(player, Farming.BREEDING_PENALTY_MULTIPLIER);
		if (penalty <= 0) penalty = 1.0f;

		float reduction = JelTraits.branchValue(player, ModSkills.FARMING,
				Farming.RANCHER_BRANCH, Farming.BEASTMASTER_REDUCTION);

		float finalMultiplier = Math.max(0.3f, penalty - reduction);
		if (Math.abs(finalMultiplier - 1.0f) < 0.001f) return;

		int modifiedCooldown = Math.round(6000 * finalMultiplier);
		this.setAge(modifiedCooldown);
		partner.setAge(modifiedCooldown);
	}

	@Unique
	private void jel$tryBreedTwin(ServerLevel serverLevel, ServerPlayer player,
								  AgeableMob baby, AgeableMob otherParent) {
		if (!JelTraits.branchTestChance(player, ModSkills.FARMING,
				Farming.RANCHER_BRANCH, Farming.BREED_TWIN_CHANCE)) return;

		AgeableMob twin = baby.getBreedOffspring(serverLevel, otherParent);
		if (twin == null) return;
		twin.setBaby(true);
		twin.moveTo(baby.getX(), baby.getY(), baby.getZ(),
				player.getRandom().nextFloat() * 360F, 0.0F);
		serverLevel.addFreshEntityWithPassengers(twin);
	}

	@Unique
	private void jel$applyShepherdsCare(ServerPlayer player, AgeableMob baby) {
		float bonus = JelTraits.branchValue(player, ModSkills.FARMING,
				Farming.RANCHER_BRANCH, Farming.SHEPHERDS_CARE_BONUS);
		if (bonus <= 0) return;

		int currentAge = baby.getAge();
		if (currentAge >= 0) return;
		baby.setAge((int) (currentAge * (1.0f - bonus)));
	}

	@Unique
	@Nullable
	private ServerPlayer jel$findBreeder(@Nullable Animal partner) {
		ServerPlayer cause = this.getLoveCause();
		if (cause != null) return cause;
		if (partner != null) return partner.getLoveCause();
		return null;
	}
}
