package dev.khanhtimn.jel.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.khanhtimn.jel.common.PlayerSkillData;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.skill.SkillProgress;
import dev.khanhtimn.jel.api.JelRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import static dev.khanhtimn.jel.command.CommandUtil.*;

public final class InfoCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("info")
				.executes(InfoCommand::listAll)
				.then(Commands.argument("skill", ResourceLocationArgument.id())
						.suggests(SUGGEST_SKILLS)
						.executes(InfoCommand::showSkill));
	}

	private static int listAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		PlayerSkillData tracker = JelSkills.getSkillData(player);
		RegistryAccess access = player.level().registryAccess();
		Registry<SkillDefinition> registry = access.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);

		if (registry.keySet().isEmpty()) {
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.info.no_skills")
							.withStyle(ChatFormatting.GRAY), false);
			return 0;
		}

		ctx.getSource().sendSuccess(() ->
				Component.translatable("commands.jel.info.header",
								Component.literal("Skills").withStyle(ChatFormatting.GOLD))
						.withStyle(ChatFormatting.GOLD), false);

		for (var entry : registry.entrySet()) {
			SkillDefinition def = entry.getValue();
			SkillProgress prog = tracker.getProgress(entry.getKey());

			MutableComponent skillName = skillDisplayName(def);
			int maxLevel = def.maxLevel();
			int xpNext = def.xpCostForNextLevel(prog.level());

			MutableComponent levelComp = Component.literal(prog.level() + "/" + maxLevel)
					.withStyle(prog.level() >= maxLevel ? ChatFormatting.GREEN : ChatFormatting.YELLOW);

			MutableComponent xpComp;
			if (xpNext > 0) {
				xpComp = Component.translatable("commands.jel.info.xp",
								String.valueOf(prog.xp()), String.valueOf(xpNext))
						.withStyle(ChatFormatting.GRAY);
			} else {
				xpComp = Component.translatable("commands.jel.info.xp_max",
								String.valueOf(prog.xp()))
						.withStyle(ChatFormatting.GREEN);
			}

			ctx.getSource().sendSuccess(() ->
					Component.literal("  ").append(skillName)
							.append(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY))
							.append(Component.translatable("commands.jel.info.level",
									levelComp, "").withStyle(ChatFormatting.WHITE))
							.append(Component.literal("  "))
							.append(xpComp), false);
		}

		var traitValues = tracker.getTraitEntries();
		if (!traitValues.isEmpty()) {
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.info.traits")
							.withStyle(ChatFormatting.LIGHT_PURPLE), false);
			for (var entry : traitValues.object2FloatEntrySet()) {
				ResourceLocation id = entry.getKey();
				float value = entry.getFloatValue();
				ctx.getSource().sendSuccess(() ->
						Component.literal("  ")
								.append(Component.literal(id.toString())
										.withStyle(ChatFormatting.WHITE))
								.append(Component.literal(" = " + String.format("%.3f", value))
										.withStyle(ChatFormatting.GRAY)), false);
			}
		}

		return 1;
	}

	private static int showSkill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		ResourceKey<SkillDefinition> skillKey = resolveSkillKey(ctx);
		SkillDefinition def = resolveDefinition(ctx);
		PlayerSkillData tracker = JelSkills.getSkillData(player);
		SkillProgress prog = tracker.getProgress(skillKey);

		MutableComponent skillName = skillDisplayName(def);
		int maxLevel = def.maxLevel();

		ctx.getSource().sendSuccess(() ->
				Component.translatable("commands.jel.info.header", skillName)
						.withStyle(ChatFormatting.GOLD), false);

		// Description
		if (!def.description().getString().isEmpty()) {
			ctx.getSource().sendSuccess(() ->
					Component.literal("  ").append(def.description().copy()
							.withStyle(ChatFormatting.GRAY)), false);
		}

		// Level
		MutableComponent levelComp = Component.literal(prog.level() + "/" + maxLevel)
				.withStyle(prog.level() >= maxLevel ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
		ctx.getSource().sendSuccess(() ->
				Component.literal("  ")
						.append(Component.translatable("commands.jel.info.level",
										levelComp, String.valueOf(maxLevel))
								.withStyle(ChatFormatting.WHITE)), false);

		// XP
		int xpNext = def.xpCostForNextLevel(prog.level());
		if (xpNext > 0) {
			ctx.getSource().sendSuccess(() ->
					Component.literal("  ")
							.append(Component.translatable("commands.jel.info.xp",
											String.valueOf(prog.xp()), String.valueOf(xpNext))
									.withStyle(ChatFormatting.WHITE)), false);
		} else {
			ctx.getSource().sendSuccess(() ->
					Component.literal("  ")
							.append(Component.translatable("commands.jel.info.xp_max",
											String.valueOf(prog.xp()))
									.withStyle(ChatFormatting.GREEN)), false);
		}

		// Perks summary
		if (!def.perks().isEmpty()) {
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.info.perks_header")
							.withStyle(ChatFormatting.LIGHT_PURPLE), false);
			for (var perk : def.perks()) {
				int threshold = perk.unlockLevel();
				boolean unlocked = prog.level() >= threshold;
				ctx.getSource().sendSuccess(() ->
								Component.literal("    ")
										.append(Component.literal(unlocked ? "✔ " : "✖ ")
												.withStyle(unlocked ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY))
										.append(Component.translatable("commands.jel.info.perk_entry",
														perk.type().id().toString(),
														String.valueOf(threshold))
												.withStyle(unlocked ? ChatFormatting.WHITE : ChatFormatting.GRAY)),
						false);
			}
		}

		// Attributes summary
		if (!def.attributes().isEmpty()) {
			ctx.getSource().sendSuccess(() ->
					Component.translatable("commands.jel.info.attributes_header")
							.withStyle(ChatFormatting.AQUA), false);
			for (var attr : def.attributes()) {
				boolean active = prog.level() >= attr.unlockLevel();
				ctx.getSource().sendSuccess(() ->
								Component.literal("    ")
										.append(Component.literal(active ? "✔ " : "✖ ")
												.withStyle(active ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY))
										.append(Component.literal(attr.attribute().toString())
												.withStyle(active ? ChatFormatting.WHITE : ChatFormatting.GRAY))
										.append(Component.literal(" (" + attr.operation().getSerializedName() + ")")
												.withStyle(ChatFormatting.DARK_GRAY)),
						false);
			}
		}

		return 1;
	}

	private InfoCommand() {
	}
}
