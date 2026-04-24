package dev.khanhtimn.jel.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class JelCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("jel")
				.then(InfoCommand.register())
				.then(SkillSubcommand.register())
				.then(DebugCommand.register())
		);
	}

	private JelCommand() {
	}
}
