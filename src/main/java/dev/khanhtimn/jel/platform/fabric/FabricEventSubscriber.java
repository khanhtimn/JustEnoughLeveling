package dev.khanhtimn.jel.platform.fabric;

//? fabric {

/*import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.common.JelLootPoolInjector;
import dev.khanhtimn.jel.common.PlayerDataHelper;
import dev.khanhtimn.jel.common.PlayerSkillData;
import dev.khanhtimn.jel.common.TraitCooldownTracker;
import dev.khanhtimn.jel.core.ModCommands;
import dev.khanhtimn.jel.event.CombatEvents;
import dev.khanhtimn.jel.event.FarmingEvents;
import dev.khanhtimn.jel.event.FoodEvents;
import dev.khanhtimn.jel.event.MiningEvents;
import dev.khanhtimn.jel.event.SkillEvents;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootPool;

public class FabricEventSubscriber {

	public static final AttachmentType<PlayerSkillData> SKILL_DATA =
			AttachmentRegistry.create(
					ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skill_data"),
					builder -> builder
							.persistent(PlayerSkillData.CODEC)
							.syncWith(PlayerSkillData.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
							.initializer(PlayerSkillData::new)
							.copyOnDeath());

	public static final AttachmentType<TraitCooldownTracker> COOLDOWNS =
			AttachmentRegistry.create(
					ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "cooldowns"),
					builder -> builder
							.persistent(TraitCooldownTracker.CODEC)
							.syncWith(TraitCooldownTracker.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
							.initializer(TraitCooldownTracker::new)
							.copyOnDeath());

	public static void registerEvents() {
		DynamicRegistries.registerSynced(
				JelRegistries.SKILL_REGISTRY_KEY,
				SkillDefinition.CODEC,
				SkillDefinition.NETWORK_CODEC
		);

		registerLootTableModifications();
		registerServerEvents();
	}

	private static void registerLootTableModifications() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!source.isBuiltin()) return;

			for (LootPool.Builder pool : JelLootPoolInjector.buildMiningPools(key)) {
				tableBuilder.withPool(pool);
			}
		});
	}

	private static void registerServerEvents() {
		ServerPlayerEvents.JOIN.register(player -> {
			SkillEvents.onPlayerReady(player);
			PlayerDataHelper.syncSkillData(player);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			SkillEvents.onPlayerReady(newPlayer);
			FoodEvents.scaleFoodOnRespawn(newPlayer);
			PlayerDataHelper.syncSkillData(newPlayer);
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayer sp) {
				CombatEvents.onPlayerDeath(sp, source);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
				CombatEvents.onPlayerTick(sp);
				FarmingEvents.onPlayerTick(sp);
				MiningEvents.onPlayerTick(sp);
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				ModCommands.register(dispatcher));
	}
}
*///?}
