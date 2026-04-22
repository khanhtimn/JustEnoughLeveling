package dev.khanhtimn.jel.core;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.common.skill.impl.SkillDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Defines custom registry keys for JustEnoughLeveling.
 * <p>
 * Registration of these keys as datapack registries is handled per-loader:
 * <ul>
 *   <li>NeoForge: via {@code DataPackRegistryEvent.NewRegistry}</li>
 *   <li>Fabric: via {@code DynamicRegistries.register}</li>
 * </ul>
 */
public final class ModRegistries {

	public static final ResourceKey<Registry<SkillDefinition>> SKILL_REGISTRY_KEY =
			ResourceKey.createRegistryKey(
					ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skill")
			);

	private ModRegistries() {
	}
}
