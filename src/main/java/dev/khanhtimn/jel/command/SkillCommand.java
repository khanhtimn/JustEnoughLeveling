package dev.khanhtimn.jel.command;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.khanhtimn.jel.common.skill.impl.SkillDefinition;
import dev.khanhtimn.jel.common.skill.impl.SkillLogic;
import dev.khanhtimn.jel.common.skill.impl.SkillProgress;
import dev.khanhtimn.jel.core.ModRegistries;
import dev.khanhtimn.jel.common.skill.impl.PlayerSkillData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class SkillCommand {

	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SKILLS = (ctx, builder) -> {
		RegistryAccess access = ctx.getSource().registryAccess();
		Registry<SkillDefinition> registry = access.registryOrThrow(ModRegistries.SKILL_REGISTRY_KEY);
		return SharedSuggestionProvider.suggestResource(registry.keySet(), builder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("jel")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("skills")
						.executes(SkillCommand::listSkills))
				.then(Commands.literal("addxp")
						.then(Commands.argument("skill", ResourceLocationArgument.id())
								.suggests(SUGGEST_SKILLS)
								.then(Commands.argument("amount", IntegerArgumentType.integer(1))
										.executes(SkillCommand::addXp))))
				.then(Commands.literal("levelup")
						.then(Commands.argument("skill", ResourceLocationArgument.id())
								.suggests(SUGGEST_SKILLS)
								.executes(ctx -> levelUp(ctx, 1))
								.then(Commands.argument("levels", IntegerArgumentType.integer(1))
										.executes(ctx -> levelUp(ctx, IntegerArgumentType.getInteger(ctx, "levels"))))))
				.then(Commands.literal("refund")
						.then(Commands.argument("skill", ResourceLocationArgument.id())
								.suggests(SUGGEST_SKILLS)
								.executes(SkillCommand::refund)))
				.then(Commands.literal("reset")
						.executes(SkillCommand::resetAll))
		);
	}

	private static int listSkills(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		PlayerSkillData tracker = SkillLogic.getSkills(player);
		RegistryAccess access = player.level().registryAccess();
		Registry<SkillDefinition> registry = access.registryOrThrow(ModRegistries.SKILL_REGISTRY_KEY);

		ctx.getSource().sendSuccess(()
				-> Component.literal("=== Skill Data ===").withStyle(ChatFormatting.GOLD), false);

		// Show registered skills with progress
		Map<ResourceLocation, SkillProgress> allSkills = tracker.getAllSkills();

		if (allSkills.isEmpty() && registry.keySet().isEmpty()) {
			ctx.getSource().sendSuccess(()
					-> Component.literal("  No skills registered.").withStyle(ChatFormatting.GRAY), false);
			return 0;
		}

		// Iterate registered definitions to show all skills (even those with no progress)
		for (var entry : registry.entrySet()) {
			ResourceLocation skillId = entry.getKey().location();
			SkillDefinition def = entry.getValue();
			SkillProgress prog = tracker.getProgress(entry.getKey());

			String displayName = def.name().getString();
			int maxLevel = def.maxLevel();
			int xpNext = def.xpCostForNextLevel(prog.level());

			ctx.getSource().sendSuccess(()
							-> Component.literal("  " + displayName)
							.withStyle(ChatFormatting.AQUA)
							.append(Component.literal(" [" + skillId + "]")
									.withStyle(ChatFormatting.DARK_GRAY))
							.append(Component.literal("\n    Level: ")
									.withStyle(ChatFormatting.WHITE))
							.append(Component.literal(prog.level() + "/" + maxLevel)
									.withStyle(prog.level() >= maxLevel ? ChatFormatting.GREEN : ChatFormatting.YELLOW))
							.append(Component.literal("  XP: ")
									.withStyle(ChatFormatting.WHITE))
							.append(Component.literal(prog.xp() + (xpNext > 0 ? "/" + xpNext : " (MAX)"))
									.withStyle(ChatFormatting.YELLOW)),
					false);
		}

		// Show ability flags
		var flags = tracker.getAbilityFlags();
		if (!flags.isEmpty()) {
			ctx.getSource().sendSuccess(()
							-> Component.literal("  Ability Flags: ").withStyle(ChatFormatting.LIGHT_PURPLE)
							.append(Component.literal(flags.toString()).withStyle(ChatFormatting.WHITE)),
					false);
		}

		return 1;
	}

	private static int addXp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		ResourceLocation skillId = ResourceLocationArgument.getId(ctx, "skill");
		int amount = IntegerArgumentType.getInteger(ctx, "amount");

		ResourceKey<SkillDefinition> skillKey = ResourceKey.create(ModRegistries.SKILL_REGISTRY_KEY, skillId);
		int levelsGained = SkillLogic.addSkillXp(player, skillKey, amount);

		ctx.getSource().sendSuccess(()
				-> Component.literal("Added " + amount + " skill XP to " + skillId
				+ " (" + levelsGained + " levels gained)").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int levelUp(CommandContext<CommandSourceStack> ctx, int levels) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		ResourceLocation skillId = ResourceLocationArgument.getId(ctx, "skill");

		ResourceKey<SkillDefinition> skillKey = ResourceKey.create(ModRegistries.SKILL_REGISTRY_KEY, skillId);
		int gained = SkillLogic.tryLevelUp(player, skillKey, levels);

		ctx.getSource().sendSuccess(()
				-> Component.literal("Leveled up " + skillId + " x" + gained
				+ " (requested " + levels + ")").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int refund(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		ResourceLocation skillId = ResourceLocationArgument.getId(ctx, "skill");

		ResourceKey<SkillDefinition> skillKey = ResourceKey.create(ModRegistries.SKILL_REGISTRY_KEY, skillId);
		int refunded = SkillLogic.refundSkill(player, skillKey);

		ctx.getSource().sendSuccess(()
				-> Component.literal("Refunded " + skillId + " → " + refunded
				+ " vanilla XP returned").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int resetAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();

		SkillLogic.resetAll(player);

		ctx.getSource().sendSuccess(()
				-> Component.literal("Reset all skills.").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private SkillCommand() {
	}
}
