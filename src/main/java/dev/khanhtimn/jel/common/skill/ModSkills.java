package dev.khanhtimn.jel.common.skill;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.common.skill.impl.SkillDefinition;
import dev.khanhtimn.jel.common.skill.skills.Constitution;
import dev.khanhtimn.jel.common.skill.skills.Melee;
import dev.khanhtimn.jel.common.skill.skills.Mining;
import dev.khanhtimn.jel.core.ModRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Registry of all mod-provided skill definition keys and bootstrap logic.
 * <p>
 * Each skill's actual configuration lives in its own class under this package
 * (e.g. {@link Melee}, {@link Mining}).
 */
public final class ModSkills {

	public static final ResourceKey<SkillDefinition> COMBAT = key("combat");
	public static final ResourceKey<SkillDefinition> MINING = key("mining");
	public static final ResourceKey<SkillDefinition> CONSTITUTION = key("constitution");

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
				ModRegistries.SKILL_REGISTRY_KEY,
				ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name)
		);
	}

	private ModSkills() {
	}
}
