package dev.khanhtimn.jel.core;

import com.mrcrayfish.framework.api.sync.SyncedClassKey;
import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import dev.khanhtimn.jel.JustEnoughLeveling;
import dev.khanhtimn.jel.component.SkillsComponent;
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
	public static final SyncedDataKey<Player, SkillsComponent> PLAYER_SKILLS =
			SyncedDataKey.builder(SyncedClassKey.PLAYER, SkillsComponent.SERIALIZER)
					.id(ResourceLocation.fromNamespaceAndPath(JustEnoughLeveling.MOD_ID, "skills"))
					.defaultValueSupplier(SkillsComponent::new)
					.saveToFile()
					.syncMode(SyncedDataKey.SyncMode.SELF_ONLY)
					.build();

	private ModSyncedDataKeys() {}
}
