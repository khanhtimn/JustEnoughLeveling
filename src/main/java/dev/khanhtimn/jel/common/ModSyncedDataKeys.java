package dev.khanhtimn.jel.common;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.sync.SyncedClassKey;
import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import dev.khanhtimn.jel.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class ModSyncedDataKeys {

	public static void init() {
		FrameworkAPI.registerSyncedDataKey(PLAYER_SKILLS);
	}

	/**
	 * The player's skill progress and ability flags.
	 * Persisted to disk and synced to the owning player's client.
	 */
	public static final SyncedDataKey<Player, PlayerSkillData> PLAYER_SKILLS =
			SyncedDataKey.builder(SyncedClassKey.PLAYER, PlayerSkillData.SERIALIZER)
					.id(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skills"))
					.defaultValueSupplier(PlayerSkillData::new)
					.saveToFile()
					.syncMode(SyncedDataKey.SyncMode.SELF_ONLY)
					.build();

	private ModSyncedDataKeys() {
	}
}
