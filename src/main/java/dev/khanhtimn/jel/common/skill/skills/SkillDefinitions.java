package dev.khanhtimn.jel.common.skill.skills;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.common.skill.SkillDefinition;
import dev.khanhtimn.jel.core.ModRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Registry of all mod-provided skill definition keys and bootstrap logic.
 * <p>
 * Each skill's actual configuration lives in its own class under this package
 * (e.g. {@link CombatSkill}, {@link MiningSkill}).
 */
public final class SkillDefinitions {

	public static final ResourceKey<SkillDefinition> COMBAT = key("combat");
	public static final ResourceKey<SkillDefinition> MINING = key("mining");

	/**
	 * Registers all built-in skill definitions into the given context.
	 * Used by datagen's {@code RegistrySetBuilder}.
	 */
	public static void bootstrap(BootstrapContext<SkillDefinition> ctx) {
		ctx.register(COMBAT, CombatSkill.create());
		ctx.register(MINING, MiningSkill.create());
	}

	private static ResourceKey<SkillDefinition> key(String name) {
		return ResourceKey.create(
				ModRegistries.SKILL_REGISTRY_KEY,
				ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name)
		);
	}

	private SkillDefinitions() {}
}
