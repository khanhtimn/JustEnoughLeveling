package dev.khanhtimn.jel.event;

import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Mining;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class MiningEvents {

	private static final int HASTE_REFRESH_INTERVAL = 40;

	public static void onPlayerTick(ServerPlayer player) {
		if (player.tickCount % HASTE_REFRESH_INTERVAL != 0) return;

		float active = JelTraits.branchValue(player, ModSkills.MINING,
				Mining.EXCAVATOR_BRANCH, Mining.EARTHSHAPER_ACTIVE);
		if (active <= 0) return;

		player.addEffect(new MobEffectInstance(
				MobEffects.DIG_SPEED, HASTE_REFRESH_INTERVAL + 10,
				0, true, false, true));
	}

	private MiningEvents() {
	}
}
