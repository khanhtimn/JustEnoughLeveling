package dev.khanhtimn.jel.api.perk;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.khanhtimn.jel.Constants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Calls a datapack function when the perk is granted or revoked.
 * <p>
 * Functions execute as the player ({@code @s}) with permission level 2,
 * output suppressed. The revoke function is optional.
 * <p>
 * Uses {@link ApplyMode#EVENT} — fires once on threshold crossing,
 * not on login/respawn.
 *
 * <h2>JSON</h2>
 * <pre>{@code
 * { "type": "jel:function", "grant": "jel:perks/combat/grant",
 *   "revoke": "jel:perks/combat/revoke", "unlock_level": 5 }
 * }</pre>
 */
public record FunctionPerk(
		int unlockLevel,
		ResourceLocation grant,
		Optional<ResourceLocation> revoke
) implements Perk {

	public static final MapCodec<FunctionPerk> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					Codec.INT.fieldOf("unlock_level")
							.forGetter(FunctionPerk::unlockLevel),
					ResourceLocation.CODEC.fieldOf("grant")
							.forGetter(FunctionPerk::grant),
					ResourceLocation.CODEC.optionalFieldOf("revoke")
							.forGetter(FunctionPerk::revoke)
			).apply(instance, FunctionPerk::new));

	public static FunctionPerk of(int unlockLevel, String grant, String revoke) {
		return new FunctionPerk(
				unlockLevel,
				ResourceLocation.parse(grant),
				revoke != null ? Optional.of(ResourceLocation.parse(revoke)) : Optional.empty()
		);
	}

	public static FunctionPerk of(int unlockLevel, ResourceLocation grant, ResourceLocation revoke) {
		return new FunctionPerk(
				unlockLevel,
				grant,
				Optional.ofNullable(revoke)
		);
	}

	public static FunctionPerk of(int unlockLevel, ResourceLocation grant) {
		return new FunctionPerk(unlockLevel, grant, Optional.empty());
	}

	@Override
	public PerkType<?> type() {
		return PerkType.FUNCTION;
	}

	@Override
	public ApplyMode applyMode() {
		return ApplyMode.EVENT;
	}

	@Override
	public void apply(PerkContext ctx, int currentLevel) {
		executeFunction(ctx.player(), grant);
	}

	@Override
	public void revoke(PerkContext ctx) {
		revoke.ifPresent(fn -> executeFunction(ctx.player(), fn));
	}

	private static void executeFunction(ServerPlayer player, ResourceLocation functionId) {
		MinecraftServer server = player.getServer();
		if (server == null) return;

		var functionOpt = server.getFunctions().get(functionId);
		if (functionOpt.isEmpty()) {
			Constants.LOG.warn("Perk function not found: {}", functionId);
			return;
		}

		CommandSourceStack source = player.createCommandSourceStack()
				.withPermission(2)
				.withSuppressedOutput();
		server.getFunctions().execute(functionOpt.get(), source);
	}
}
