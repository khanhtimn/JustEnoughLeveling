package dev.khanhtimn.jel.platform.neoforge;

//? neoforge {

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.loot.JelLootItemConditions;
import dev.khanhtimn.jel.api.loot.JelLootItemFunctions;
import dev.khanhtimn.jel.common.PlayerSkillData;
import dev.khanhtimn.jel.common.TraitCooldownTracker;
import dev.khanhtimn.jel.core.ModAttributes;
import dev.khanhtimn.jel.core.ModCommands;
import dev.khanhtimn.jel.event.CombatEvents;
import dev.khanhtimn.jel.event.FarmingEvents;
import dev.khanhtimn.jel.event.FoodEvents;
import dev.khanhtimn.jel.event.MiningEvents;
import dev.khanhtimn.jel.event.SkillEvents;
import dev.khanhtimn.jel.platform.neoforge.loot.JelBlockLootModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

public class NeoforgeEventSubscriber {

	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
			DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

	public static final Supplier<AttachmentType<PlayerSkillData>> SKILL_DATA =
			ATTACHMENT_TYPES.register("skill_data", () ->
					AttachmentType.builder(PlayerSkillData::new)
							.serialize(PlayerSkillData.CODEC)
							.copyOnDeath()
							.build());

	public static final Supplier<AttachmentType<TraitCooldownTracker>> COOLDOWNS =
			ATTACHMENT_TYPES.register("cooldowns", () ->
					AttachmentType.builder(TraitCooldownTracker::new)
							.serialize(TraitCooldownTracker.CODEC)
							.copyOnDeath()
							.build());

	public static void registerModBusEvents(IEventBus modBus) {
		ATTACHMENT_TYPES.register(modBus);
		modBus.addListener(NeoforgeEventSubscriber::registerDatapackRegistries);
		modBus.addListener(NeoforgeEventSubscriber::onRegister);
	}

	private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
		event.dataPackRegistry(
				JelRegistries.SKILL_REGISTRY_KEY,
				SkillDefinition.CODEC,
				SkillDefinition.NETWORK_CODEC
		);
	}

	private static void onRegister(RegisterEvent event) {
		event.register(
				BuiltInRegistries.LOOT_CONDITION_TYPE.key(),
				Constants.rl("skill_level_check"),
				() -> JelLootItemConditions.SKILL_LEVEL_CHECK
		);
		event.register(
				BuiltInRegistries.LOOT_CONDITION_TYPE.key(),
				Constants.rl("trait_chance"),
				() -> JelLootItemConditions.TRAIT_CHANCE
		);
		event.register(
				BuiltInRegistries.LOOT_FUNCTION_TYPE.key(),
				Constants.rl("auto_smelt"),
				() -> JelLootItemFunctions.AUTO_SMELT
		);
		event.register(
				BuiltInRegistries.ATTRIBUTE.key(),
				Constants.rl("max_food_level"),
				() -> ModAttributes.MAX_FOOD_LEVEL
		);
		event.register(
				NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.key(),
				Constants.rl("jel_block_drops"),
				() -> JelBlockLootModifier.CODEC
		);
	}

	@EventBusSubscriber(modid = Constants.MOD_ID)
	public static class GameBusEvents {

		@SubscribeEvent
		public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
			if (event.getEntity() instanceof ServerPlayer sp) {
				SkillEvents.onPlayerReady(sp);
			}
		}

		@SubscribeEvent
		public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
			if (event.getEntity() instanceof ServerPlayer sp) {
				SkillEvents.onPlayerReady(sp);
				FoodEvents.scaleFoodOnRespawn(sp);
			}
		}

		@SubscribeEvent
		public static void onPlayerDeath(LivingDeathEvent event) {
			if (event.getEntity() instanceof ServerPlayer sp) {
				CombatEvents.onPlayerDeath(sp, event.getSource());
			}
		}

		@SubscribeEvent
		public static void onPlayerTick(PlayerTickEvent.Post event) {
			if (!(event.getEntity() instanceof ServerPlayer sp)) return;
			CombatEvents.onPlayerTick(sp);
			FarmingEvents.onPlayerTick(sp);
			MiningEvents.onPlayerTick(sp);
		}

		@SubscribeEvent
		public static void onRegisterCommands(RegisterCommandsEvent event) {
			ModCommands.register(event.getDispatcher());
		}
	}
}
//?}
