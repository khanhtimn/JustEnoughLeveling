package dev.khanhtimn.jel.content;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.JelRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import dev.khanhtimn.jel.content.skills.*;

/**
 * Registry of all mod-provided skill definition keys and bootstrap logic.
 * <p>
 * Each skill's actual configuration lives in its own class under this package
 * (e.g. {@link Melee}, {@link Mining}).
 */
public final class ModSkills {
	public static final ResourceKey<SkillDefinition> CONSTITUTION = key("constitution");
	public static final ResourceKey<SkillDefinition> COMBAT = key("combat");
	public static final ResourceKey<SkillDefinition> MINING = key("mining");

	/**
	 * Registers all built-in skill definitions into the given context.
	 * Used by datagen's {@code RegistrySetBuilder}.
	 */
	public static void bootstrap(BootstrapContext<SkillDefinition> ctx) {
		ctx.register(COMBAT, Melee.create());
		ctx.register(MINING, Mining.create());
		ctx.register(CONSTITUTION, Constitution.create());
	}

	private static ResourceKey<SkillDefinition> key(String name) {
		return ResourceKey.create(
				JelRegistries.SKILL_REGISTRY_KEY,
				ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name)
		);
	}

	private ModSkills() {
	}
}
