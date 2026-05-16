package dev.khanhtimn.jel.core;

import com.mojang.brigadier.CommandDispatcher;
import dev.khanhtimn.jel.command.JelCommand;
import net.minecraft.commands.CommandSourceStack;

public final class ModCommands {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		JelCommand.register(dispatcher);
	}

	private ModCommands() {
	}
}
