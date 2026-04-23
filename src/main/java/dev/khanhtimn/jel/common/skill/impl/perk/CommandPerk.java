package dev.khanhtimn.jel.common.skill.impl.perk;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Executes a command string when the perk is granted or revoked.
 * <p>
 * Commands execute as the player ({@code @s}) with permission level 2,
 * output suppressed. The revoke command is optional.
 * <p>
 * This is the inline alternative to {@link FunctionPerk} — no separate
 * mcfunction file needed.
 * <p>
 * Uses {@link ApplyMode#EVENT} — fires once on threshold crossing.
 *
 * <h2>JSON</h2>
 * <pre>{@code
 * { "type": "jel:command", "grant_command": "give @s diamond 1",
 *   "revoke_command": "clear @s diamond 1", "unlock_level": 10 }
 * }</pre>
 */
public record CommandPerk(
		int unlockLevel,
		String grantCommand,
		Optional<String> revokeCommand
) implements Perk {

	public static final MapCodec<CommandPerk> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					Codec.INT.fieldOf("unlock_level")
							.forGetter(CommandPerk::unlockLevel),
					Codec.STRING.fieldOf("grant_command")
							.forGetter(CommandPerk::grantCommand),
					Codec.STRING.optionalFieldOf("revoke_command")
							.forGetter(CommandPerk::revokeCommand)
			).apply(instance, CommandPerk::new));

	public static CommandPerk of(String grantCommand, String revokeCommand, int unlockLevel) {
		return new CommandPerk(unlockLevel, grantCommand,
				revokeCommand != null ? Optional.of(revokeCommand) : Optional.empty());
	}

	public static CommandPerk of(String grantCommand, int unlockLevel) {
		return new CommandPerk(unlockLevel, grantCommand, Optional.empty());
	}

	@Override
	public PerkType<?> type() {
		return PerkType.COMMAND;
	}

	@Override
	public ApplyMode applyMode() {
		return ApplyMode.EVENT;
	}

	@Override
	public void apply(PerkContext ctx, int currentLevel) {
		executeCommand(ctx.player(), grantCommand);
	}

	@Override
	public void revoke(PerkContext ctx) {
		revokeCommand.ifPresent(cmd -> executeCommand(ctx.player(), cmd));
	}

	private static void executeCommand(ServerPlayer player, String command) {
		MinecraftServer server = player.getServer();
		if (server == null) return;

		CommandSourceStack source = player.createCommandSourceStack()
				.withPermission(2)
				.withSuppressedOutput();

		server.getCommands().performPrefixedCommand(source, command);
	}
}
