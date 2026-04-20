package dev.khanhtimn.jel.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mrcrayfish.framework.api.event.ServerEvents;
import dev.khanhtimn.jel.common.command.SkillCommand;
import net.minecraft.commands.CommandSourceStack;

/**
 * Registers all mod commands via Framework's ServerEvents.STARTING.
 */
public final class ModCommands {

	public static void init() {
		ServerEvents.STARTING.register(server -> {
			CommandDispatcher<CommandSourceStack> dispatcher =
					server.getCommands().getDispatcher();
			SkillCommand.register(dispatcher);
		});
	}

	private ModCommands() {}
}
