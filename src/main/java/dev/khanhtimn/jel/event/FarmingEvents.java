package dev.khanhtimn.jel.event;

import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Farming;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class FarmingEvents {

	private static final int PHOTOSYNTHESIS_INTERVAL = 100;
	private static final int HERDER_CALL_INTERVAL = 20;
	private static final int PHOTOSYNTHESIS_RADIUS = 4;

	public static void onPlayerTick(ServerPlayer player) {
		if (JelSkills.getLevel(player, ModSkills.FARMING) <= 0) return;

		if (player.tickCount % PHOTOSYNTHESIS_INTERVAL == 0) {
			tickPhotosynthesis(player);
		}

		if (player.tickCount % HERDER_CALL_INTERVAL == 0) {
			tickHerderCall(player);
		}
	}

	private static void tickPhotosynthesis(ServerPlayer player) {
		float speedBonus = JelTraits.branchValue(player, ModSkills.FARMING,
				Farming.CULTIVATOR_BRANCH, Farming.PHOTOSYNTHESIS_SPEED);
		if (speedBonus <= 0) return;

		ServerLevel level = player.serverLevel();
		BlockPos center = player.blockPosition();
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for (int dx = -PHOTOSYNTHESIS_RADIUS; dx <= PHOTOSYNTHESIS_RADIUS; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -PHOTOSYNTHESIS_RADIUS; dz <= PHOTOSYNTHESIS_RADIUS; dz++) {
					mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					BlockState state = level.getBlockState(mutable);
					if (!(state.getBlock() instanceof CropBlock crop)) continue;
					if (crop.isMaxAge(state)) continue;
					if (player.getRandom().nextFloat() >= speedBonus) continue;

					int currentAge = crop.getAge(state);
					level.setBlock(mutable.immutable(),
							crop.getStateForAge(Math.min(currentAge + 1, crop.getMaxAge())),
							CropBlock.UPDATE_CLIENTS);
				}
			}
		}
	}

	private static void tickHerderCall(ServerPlayer player) {
		if (!player.isShiftKeyDown()) return;

		float radius = JelTraits.branchValue(player, ModSkills.FARMING,
				Farming.RANCHER_BRANCH, Farming.HERDERS_CALL_RADIUS);
		if (radius <= 0) return;

		AABB area = player.getBoundingBox().inflate(radius);
		List<Animal> animals = player.level().getEntitiesOfClass(Animal.class, area);

		Vec3 playerPos = player.position();
		for (Animal animal : animals) {
			Vec3 dir = playerPos.subtract(animal.position()).normalize().scale(0.15);
			animal.setDeltaMovement(animal.getDeltaMovement().add(dir.x, 0, dir.z));
		}
	}

	private FarmingEvents() {
	}
}
