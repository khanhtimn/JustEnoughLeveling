package dev.khanhtimn.jel.event;

import com.mrcrayfish.framework.api.event.TickEvents;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Mining;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class MiningEvents {

	private static final int HASTE_REFRESH_INTERVAL = 40;

	public static void register() {
		TickEvents.END_PLAYER.register(MiningEvents::onPlayerTick);
	}

	private static void onPlayerTick(Player player) {
		if (player.level().isClientSide()) return;
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
