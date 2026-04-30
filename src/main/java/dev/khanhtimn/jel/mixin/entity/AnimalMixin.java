package dev.khanhtimn.jel.mixin.entity;

import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.skills.Farming;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Animal.class)
public abstract class AnimalMixin extends AgeableMob {

	@Shadow
	@Nullable
	public ServerPlayer getLoveCause() {
		return null;
	}

	protected AnimalMixin(EntityType<? extends AgeableMob> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(
			method = "spawnChildFromBreeding",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"
			),
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void jel$breedMixin(ServerLevel serverLevel, Animal animal, CallbackInfo ci, AgeableMob ageableMob) {
		ServerPlayer player = this.getLoveCause() != null ? this.getLoveCause() : animal.getLoveCause();
		if (player != null) {
			jel$breedTwin(serverLevel, player, ageableMob, animal);
		}
	}

	@Unique
	private static void jel$breedTwin(ServerLevel serverLevel, ServerPlayer player, AgeableMob animal, AgeableMob otherAnimal) {
		if (JelTraits.testChance(player, Farming.BREED_TWIN_CHANCE)) {
			AgeableMob extraAgeableMob = animal.getBreedOffspring(serverLevel, otherAnimal);
			extraAgeableMob.setBaby(true);
			extraAgeableMob.moveTo(animal.getX(), animal.getY(), animal.getZ(), player.getRandom().nextFloat() * 360F, 0.0F);
			serverLevel.addFreshEntityWithPassengers(extraAgeableMob);
		}
	}

	@Inject(
			method = "finalizeSpawnChildFromBreeding",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
			)
	)
	private void jel$breedExperienceMixin(ServerLevel serverLevel, Animal animal, AgeableMob ageableMob, CallbackInfo ci) {
		ServerPlayer player = this.getLoveCause() != null ? this.getLoveCause() : animal.getLoveCause();
		float xp_multiplier = JelTraits.value(player, Farming.BREED_XP_MULTIPLIER);
		if (xp_multiplier > 0.0F) {
			ExperienceOrb.award(serverLevel, this.position().add(0.0D, 0.1D, 0.0D),
					(int) ((this.getRandom().nextInt(7) + 1) * xp_multiplier));
		}
	}
}
