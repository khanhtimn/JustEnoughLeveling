package dev.khanhtimn.jel.event;

import java.util.WeakHashMap;

import com.mrcrayfish.framework.api.event.PlayerEvents;
import com.mrcrayfish.framework.api.event.TickEvents;
import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.JelTraits;
import dev.khanhtimn.jel.content.ModSkills;
import dev.khanhtimn.jel.content.skills.Defense;
import dev.khanhtimn.jel.misc.CombatTraitAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class CombatEvents {

	private static final ResourceLocation JEL_FORTIFY_MOD_ID =
			Constants.rl("defense/fortify");

	private static final int FORTIFY_TICKS_REQUIRED = 20;

	private static final double MOVE_THRESHOLD_SQ = 0.0001;

	private static final WeakHashMap<Player, FortifyState> FORTIFY_STATES = new WeakHashMap<>();

	public static void register() {
		TickEvents.END_PLAYER.register(CombatEvents::onPlayerTick);
		PlayerEvents.DEATH.register(CombatEvents::onPlayerDeath);
	}

	private static void onPlayerTick(Player player) {
		if (player.level().isClientSide()) return;

		FortifyState state = FORTIFY_STATES.computeIfAbsent(player, p -> new FortifyState());

		double dx = player.getX() - state.prevX;
		double dz = player.getZ() - state.prevZ;
		state.prevX = player.getX();
		state.prevZ = player.getZ();

		boolean moved = (dx * dx + dz * dz) > MOVE_THRESHOLD_SQ;

		if (moved) {
			state.stillTicks = 0;
			if (state.active) {
				state.active = false;
				AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
				if (armor != null) {
					armor.removeModifier(JEL_FORTIFY_MOD_ID);
				}
			}
			return;
		}

		state.stillTicks++;
		if (state.stillTicks < FORTIFY_TICKS_REQUIRED || state.active) return;

		float armorBonus = JelTraits.branchValue(player, ModSkills.DEFENSE,
				Defense.FORTRESS_BRANCH, Defense.FORTIFY_ARMOR);
		if (armorBonus <= 0) return;

		AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
		if (armor != null) {
			armor.addTransientModifier(new AttributeModifier(
					JEL_FORTIFY_MOD_ID, armorBonus,
					AttributeModifier.Operation.ADD_VALUE));
			state.active = true;
		}
	}

	private static boolean onPlayerDeath(Player player, DamageSource source) {
		if (player instanceof CombatTraitAccessor accessor) {
			accessor.jel$getComboTracker().onPlayerDeath();
		}
		FORTIFY_STATES.remove(player);
		return false;
	}

	private static class FortifyState {
		double prevX;
		double prevZ;
		int stillTicks;
		boolean active;
	}

	private CombatEvents() {
	}
}
