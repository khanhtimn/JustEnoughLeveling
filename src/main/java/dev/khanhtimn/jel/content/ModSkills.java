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
	public static final ResourceKey<SkillDefinition> DEFENSE = key("defense");
	public static final ResourceKey<SkillDefinition> ARCHERY = key("archery");
	public static final ResourceKey<SkillDefinition> AGILITY = key("agility");
	public static final ResourceKey<SkillDefinition> MAGIC = key("magic");
	public static final ResourceKey<SkillDefinition> SMITHING = key("smithing");
	public static final ResourceKey<SkillDefinition> MINING = key("mining");
	public static final ResourceKey<SkillDefinition> FARMING = key("farming");
	public static final ResourceKey<SkillDefinition> COOKING = key("cooking");
	public static final ResourceKey<SkillDefinition> BATERING = key("batering");

	/**
	 * Registers all built-in skill definitions into the given context.
	 * Used by datagen's {@code RegistrySetBuilder}.
	 */
	public static void bootstrap(BootstrapContext<SkillDefinition> ctx) {
		ctx.register(CONSTITUTION, Constitution.create());
		ctx.register(COMBAT, Melee.create());
		ctx.register(DEFENSE, Defense.create());
		ctx.register(ARCHERY, Archery.create());
		ctx.register(AGILITY, Agility.create());
		ctx.register(MAGIC, Magic.create());
		ctx.register(SMITHING, Smithing.create());
		ctx.register(MINING, Mining.create());
		ctx.register(FARMING, Farming.create());
		ctx.register(BATERING, Batering.create());
		ctx.register(COOKING, Cooking.create());
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
