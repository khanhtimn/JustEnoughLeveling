package dev.khanhtimn.jel.core;

import com.mrcrayfish.framework.api.sync.SyncedClassKey;
import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.data.skill.SkillsTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Defines all {@link SyncedDataKey} instances for JustEnoughLeveling.
 * <p>
 * Keys must be registered during common initialization via
 * {@link com.mrcrayfish.framework.api.FrameworkAPI#registerSyncedDataKey}.
 */
public final class ModSyncedDataKeys {

	/**
	 * The player's skill progress and ability flags.
	 * Persisted to disk and synced to the owning player's client.
	 */
	public static final SyncedDataKey<Player, SkillsTracker> PLAYER_SKILLS =
			SyncedDataKey.builder(SyncedClassKey.PLAYER, SkillsTracker.SERIALIZER)
					.id(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skills"))
					.defaultValueSupplier(SkillsTracker::new)
					.saveToFile()
					.syncMode(SyncedDataKey.SyncMode.SELF_ONLY)
					.build();

	private ModSyncedDataKeys() {
	}
}
