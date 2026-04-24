package dev.khanhtimn.jel.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class DebugCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("debug")
				.requires(source -> source.hasPermission(3));
	}

	private DebugCommand() {
	}
}
