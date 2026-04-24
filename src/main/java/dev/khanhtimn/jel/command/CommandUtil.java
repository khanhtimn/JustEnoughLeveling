package dev.khanhtimn.jel.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.JelRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class CommandUtil {

	static final DynamicCommandExceptionType UNKNOWN_SKILL =
			new DynamicCommandExceptionType(id ->
					Component.translatable("commands.jel.error.unknown_skill", String.valueOf(id)));

	static final SuggestionProvider<CommandSourceStack> SUGGEST_SKILLS = (ctx, builder) -> {
		RegistryAccess access = ctx.getSource().registryAccess();
		Registry<SkillDefinition> registry = access.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);
		return SharedSuggestionProvider.suggestResource(registry.keySet(), builder);
	};

	static ResourceKey<SkillDefinition> resolveSkillKey(
			CommandContext<CommandSourceStack> ctx
	) throws CommandSyntaxException {
		ResourceLocation id = ResourceLocationArgument.getId(ctx, "skill");
		RegistryAccess access = ctx.getSource().registryAccess();
		Registry<SkillDefinition> registry = access.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);
		ResourceKey<SkillDefinition> key = ResourceKey.create(JelRegistries.SKILL_REGISTRY_KEY, id);
		if (!registry.containsKey(key)) {
			throw UNKNOWN_SKILL.create(id);
		}
		return key;
	}

	static SkillDefinition resolveDefinition(
			CommandContext<CommandSourceStack> ctx
	) throws CommandSyntaxException {
		ResourceKey<SkillDefinition> key = resolveSkillKey(ctx);
		RegistryAccess access = ctx.getSource().registryAccess();
		Registry<SkillDefinition> registry = access.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);
		return registry.get(key);
	}

	static MutableComponent skillDisplayName(SkillDefinition def) {
		return def.name().copy().withStyle(
				Style.EMPTY.withColor(TextColor.fromRgb(def.color()))
		);
	}

	private CommandUtil() {
	}
}
