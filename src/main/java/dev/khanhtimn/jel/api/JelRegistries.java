package dev.khanhtimn.jel.api;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class JelRegistries {

	public static final ResourceKey<Registry<SkillDefinition>> SKILL_REGISTRY_KEY =
			ResourceKey.createRegistryKey(
					ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skill")
			);

	private JelRegistries() {
	}
}
