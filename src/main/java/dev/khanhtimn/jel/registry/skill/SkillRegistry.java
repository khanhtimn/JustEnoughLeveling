package dev.khanhtimn.jel.registry.skill;

import dev.khanhtimn.jel.JustEnoughLeveling;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Defines the custom datapack registry key for {@link SkillDefinition}.
 * <p>
 * Registration of this key as a datapack registry is handled per-loader:
 * <ul>
 *   <li>NeoForge: via {@code DataPackRegistryEvent.NewRegistry}</li>
 *   <li>Fabric: via {@code DataPackRegistryProvider}</li>
 * </ul>
 */
public final class SkillRegistry {

	public static final ResourceKey<Registry<SkillDefinition>> SKILL_REGISTRY_KEY =
			ResourceKey.createRegistryKey(
					ResourceLocation.fromNamespaceAndPath(JustEnoughLeveling.MOD_ID, "skill")
			);

	private SkillRegistry() {}
}
