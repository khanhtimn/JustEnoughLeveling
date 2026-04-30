package dev.khanhtimn.jel.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.skills.Smithing;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PrimedTnt.class)
public abstract class TntEntityMixin extends Entity {

	@Shadow
	@Nullable
	private LivingEntity owner;

	public TntEntityMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@WrapOperation(
			method = "explode",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"
			)
	)
	private Explosion jel$explodeMixin(Level level, Entity entity, DamageSource damageSource, ExplosionDamageCalculator behavior, double x, double y, double z, float power, boolean createFire, Level.ExplosionInteraction explosionInteraction, Operation<Explosion> original) {
		if (owner != null && owner instanceof Player player) {
			power += JelTraits.value(player, Smithing.TNT_STRENGTH);
		}
		return original.call(level, entity, damageSource, behavior, x, y, z, power, createFire, explosionInteraction);
	}
}
