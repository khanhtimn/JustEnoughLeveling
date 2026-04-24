package dev.khanhtimn.jel.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mrcrayfish.framework.api.event.ServerEvents;
import dev.khanhtimn.jel.command.JelCommand;
import net.minecraft.commands.CommandSourceStack;

public final class ModCommands {

	public static void init() {
		ServerEvents.STARTING.register(server -> {
			CommandDispatcher<CommandSourceStack> dispatcher =
					server.getCommands().getDispatcher();
			JelCommand.register(dispatcher);
		});
	}

	private ModCommands() {
	}
}
