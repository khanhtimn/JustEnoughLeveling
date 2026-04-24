package dev.khanhtimn.jel.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.skill.SkillProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

import static dev.khanhtimn.jel.command.CommandUtil.*;

public final class SkillSubcommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("skill")
				.requires(source -> source.hasPermission(2))
				.then(xpBranch())
				.then(levelBranch())
				.then(refundBranch())
				.then(resetBranch());
	}

	// ── /jel skill xp ───────────────────────────────────────────

	private static LiteralArgumentBuilder<CommandSourceStack> xpBranch() {
		return Commands.literal("xp")
				.then(Commands.literal("add")
						.then(Commands.argument("targets", EntityArgument.players())
								.then(Commands.argument("skill", ResourceLocationArgument.id())
										.suggests(SUGGEST_SKILLS)
										.then(Commands.argument("amount", IntegerArgumentType.integer(1))
												.executes(SkillSubcommand::xpAdd)))))
				.then(Commands.literal("set")
						.then(Commands.argument("targets", EntityArgument.players())
								.then(Commands.argument("skill", ResourceLocationArgument.id())
										.suggests(SUGGEST_SKILLS)
										.then(Commands.argument("amount", IntegerArgumentType.integer(0))
												.executes(SkillSubcommand::xpSet)))))
				.then(Commands.literal("query")
						.then(Commands.argument("target", EntityArgument.player())
								.then(Commands.argument("skill", ResourceLocationArgument.id())
										.suggests(SUGGEST_SKILLS)
										.executes(SkillSubcommand::xpQuery))));
	}

	private static int xpAdd(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		ResourceKey<SkillDefinition> skillKey = resolveSkillKey(ctx);
		SkillDefinition def = resolveDefinition(ctx);
		int amount = IntegerArgumentType.getInteger(ctx, "amount");
		Component skillName = skillDisplayName(def);

		int totalLevelsGained = 0;
		for (ServerPlayer target : targets) {
			totalLevelsGained += JelSkills.addSkillXp(target, skillKey, amount);
		}

		if (targets.size() == 1) {
			ServerPlayer target = targets.iterator().next();
			int levelsGained = totalLevelsGained;
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.xp.add.success.single",
									amount, skillName,
									target.getDisplayName(),
									levelsGained)
							.withStyle(ChatFormatting.GREEN), true);
		} else {
			int count = targets.size();
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.xp.add.success.multiple",
									amount, skillName, count)
							.withStyle(ChatFormatting.GREEN), true);
		}
		return targets.size();
	}

	private static int xpSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		ResourceKey<SkillDefinition> skillKey = resolveSkillKey(ctx);
		SkillDefinition def = resolveDefinition(ctx);
		int amount = IntegerArgumentType.getInteger(ctx, "amount");
		Component skillName = skillDisplayName(def);

		for (ServerPlayer target : targets) {
			JelSkills.setSkillXp(target, skillKey, amount);
		}

		if (targets.size() == 1) {
			ServerPlayer target = targets.iterator().next();
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.xp.set.success.single",
									amount, skillName,
									target.getDisplayName())
							.withStyle(ChatFormatting.GREEN), true);
		} else {
			int count = targets.size();
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.xp.set.success.multiple",
									amount, skillName, count)
							.withStyle(ChatFormatting.GREEN), true);
		}
		return targets.size();
	}

	private static int xpQuery(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
		ResourceKey<SkillDefinition> skillKey = resolveSkillKey(ctx);
		SkillDefinition def = resolveDefinition(ctx);
		Component skillName = skillDisplayName(def);

		SkillProgress prog = JelSkills.getSkillData(target).getProgress(skillKey);
		int xpNext = def.xpCostForNextLevel(prog.level());
		String xpStr = xpNext > 0
				? prog.xp() + "/" + xpNext
				: prog.xp() + " (MAX)";

		ctx.getSource().sendSuccess(() ->
				Component.translatable("commands.jel.skill.xp.query",
								target.getDisplayName(), xpStr, skillName)
						.withStyle(ChatFormatting.WHITE), false);
		return prog.xp();
	}

	// ── /jel skill level ────────────────────────────────────────

	private static LiteralArgumentBuilder<CommandSourceStack> levelBranch() {
		return Commands.literal("level")
				.then(Commands.literal("add")
						.then(Commands.argument("targets", EntityArgument.players())
								.then(Commands.argument("skill", ResourceLocationArgument.id())
										.suggests(SUGGEST_SKILLS)
										.executes(ctx -> levelAdd(ctx, 1))
										.then(Commands.argument("levels", IntegerArgumentType.integer(1))
												.executes(ctx -> levelAdd(ctx,
														IntegerArgumentType.getInteger(ctx, "levels")))))))
				.then(Commands.literal("set")
						.then(Commands.argument("targets", EntityArgument.players())
								.then(Commands.argument("skill", ResourceLocationArgument.id())
										.suggests(SUGGEST_SKILLS)
										.then(Commands.argument("level", IntegerArgumentType.integer(0))
												.executes(SkillSubcommand::levelSet)))))
				.then(Commands.literal("query")
						.then(Commands.argument("target", EntityArgument.player())
								.then(Commands.argument("skill", ResourceLocationArgument.id())
										.suggests(SUGGEST_SKILLS)
										.executes(SkillSubcommand::levelQuery))));
	}

	private static int levelAdd(CommandContext<CommandSourceStack> ctx, int levels) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		ResourceKey<SkillDefinition> skillKey = resolveSkillKey(ctx);
		SkillDefinition def = resolveDefinition(ctx);
		Component skillName = skillDisplayName(def);

		int totalGained = targets.stream().mapToInt(target -> JelSkills.addLevelsFree(target, skillKey, levels)).sum();

		if (targets.size() == 1) {
			ServerPlayer target = targets.iterator().next();
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.level.add.success.single",
									totalGained, skillName,
									target.getDisplayName())
							.withStyle(ChatFormatting.GREEN), true);
		} else {
			int count = targets.size();
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.level.add.success.multiple",
									totalGained, skillName, count)
							.withStyle(ChatFormatting.GREEN), true);
		}
		return targets.size();
	}

	private static int levelSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		ResourceKey<SkillDefinition> skillKey = resolveSkillKey(ctx);
		SkillDefinition def = resolveDefinition(ctx);
		int level = IntegerArgumentType.getInteger(ctx, "level");
		Component skillName = skillDisplayName(def);

		for (ServerPlayer target : targets) {
			JelSkills.setSkillLevel(target, skillKey, level);
		}

		if (targets.size() == 1) {
			ServerPlayer target = targets.iterator().next();
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.level.set.success.single",
									skillName, level,
									target.getDisplayName())
							.withStyle(ChatFormatting.GREEN), true);
		} else {
			int count = targets.size();
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.level.set.success.multiple",
									skillName, level, count)
							.withStyle(ChatFormatting.GREEN), true);
		}
		return targets.size();
	}

	private static int levelQuery(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
		ResourceKey<SkillDefinition> skillKey = resolveSkillKey(ctx);
		SkillDefinition def = resolveDefinition(ctx);
		Component skillName = skillDisplayName(def);

		int level = JelSkills.getSkillData(target).getLevel(skillKey);
		int maxLevel = def.maxLevel();

		ctx.getSource().sendSuccess(() ->
				Component.translatable("commands.jel.skill.level.query",
								target.getDisplayName(), level, maxLevel, skillName)
						.withStyle(ChatFormatting.WHITE), false);
		return level;
	}

	// ── /jel skill refund ───────────────────────────────────────

	private static LiteralArgumentBuilder<CommandSourceStack> refundBranch() {
		return Commands.literal("refund")
				.then(Commands.argument("targets", EntityArgument.players())
						.then(Commands.argument("skill", ResourceLocationArgument.id())
								.suggests(SUGGEST_SKILLS)
								.executes(SkillSubcommand::refund)));
	}

	private static int refund(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		ResourceKey<SkillDefinition> skillKey = resolveSkillKey(ctx);
		SkillDefinition def = resolveDefinition(ctx);
		Component skillName = skillDisplayName(def);

		for (ServerPlayer target : targets) {
			int vanillaXp = JelSkills.refundSkill(target, skillKey);
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.refund.success",
									skillName, vanillaXp, target.getDisplayName())
							.withStyle(ChatFormatting.GREEN), true);
		}
		return targets.size();
	}

	// ── /jel skill reset ────────────────────────────────────────

	private static LiteralArgumentBuilder<CommandSourceStack> resetBranch() {
		return Commands.literal("reset")
				.then(Commands.argument("targets", EntityArgument.players())
						.executes(SkillSubcommand::resetAll)
						.then(Commands.argument("skill", ResourceLocationArgument.id())
								.suggests(SUGGEST_SKILLS)
								.executes(SkillSubcommand::resetOne)));
	}

	private static int resetAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");

		for (ServerPlayer target : targets) {
			JelSkills.resetAll(target);
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.reset.success.all",
									target.getDisplayName())
							.withStyle(ChatFormatting.GREEN), true);
		}
		return targets.size();
	}

	private static int resetOne(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		ResourceKey<SkillDefinition> skillKey = resolveSkillKey(ctx);
		SkillDefinition def = resolveDefinition(ctx);
		Component skillName = skillDisplayName(def);

		for (ServerPlayer target : targets) {
			JelSkills.resetSkill(target, skillKey);
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.skill.reset.success.single",
									skillName, target.getDisplayName())
							.withStyle(ChatFormatting.GREEN), true);
		}
		return targets.size();
	}

	private SkillSubcommand() {
	}
}
