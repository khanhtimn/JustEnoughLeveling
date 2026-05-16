package dev.khanhtimn.jel.common;

import net.minecraft.world.entity.player.Player;

//? fabric {
/*import dev.khanhtimn.jel.platform.fabric.FabricEventSubscriber;
*///?} neoforge {
import dev.khanhtimn.jel.platform.neoforge.NeoforgeEventSubscriber;
//?}

public final class PlayerDataHelper {

	public static PlayerSkillData get(Player player) {
		//? fabric {
		/*return player.getAttachedOrCreate(FabricEventSubscriber.SKILL_DATA);
		*///?} neoforge {
		return player.getData(NeoforgeEventSubscriber.SKILL_DATA.get());
		//?}
	}

	public static TraitCooldownTracker getCooldowns(Player player) {
		//? fabric {
		/*return player.getAttachedOrCreate(FabricEventSubscriber.COOLDOWNS);
		*///?} neoforge {
		return player.getData(NeoforgeEventSubscriber.COOLDOWNS.get());
		//?}
	}

	public static void syncSkillData(Player player) {
		//? fabric {
		/*PlayerSkillData data = player.getAttachedOrCreate(FabricEventSubscriber.SKILL_DATA);
		player.setAttached(FabricEventSubscriber.SKILL_DATA, data);
		*///?} neoforge {
		player.setData(NeoforgeEventSubscriber.SKILL_DATA.get(), get(player));
		//?}
	}

	public static void syncCooldowns(Player player) {
		//? fabric {
		/*TraitCooldownTracker data = player.getAttachedOrCreate(FabricEventSubscriber.COOLDOWNS);
		player.setAttached(FabricEventSubscriber.COOLDOWNS, data);
		*///?} neoforge {
		player.setData(NeoforgeEventSubscriber.COOLDOWNS.get(), getCooldowns(player));
		//?}
	}

	private PlayerDataHelper() {
	}
}
