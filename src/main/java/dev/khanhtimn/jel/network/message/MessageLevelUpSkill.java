package dev.khanhtimn.jel.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.common.skill.impl.SkillDefinition;
import dev.khanhtimn.jel.common.skill.impl.SkillLogic;
import dev.khanhtimn.jel.core.ModRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record MessageLevelUpSkill(ResourceLocation skillId) {

	public static final StreamCodec<RegistryFriendlyByteBuf, MessageLevelUpSkill> STREAM_CODEC =
			StreamCodec.composite(
					ResourceLocation.STREAM_CODEC.cast(),
					MessageLevelUpSkill::skillId,
					MessageLevelUpSkill::new
			);

	public static void handle(MessageLevelUpSkill message, MessageContext context) {
		context.execute(() -> context.getPlayer().ifPresent(player -> {
			if (player instanceof ServerPlayer serverPlayer) {
				ResourceKey<SkillDefinition> key = ResourceKey.create(
						ModRegistries.SKILL_REGISTRY_KEY,
						message.skillId()
				);
				boolean success = SkillLogic.tryLevelUpOnce(serverPlayer, key);
				if (!success) {
					Constants.LOG.debug("Level-up denied for {} on skill {} (insufficient XP or max level)",
							serverPlayer.getName().getString(), message.skillId());
				}
			}
		}));
		context.setHandled(true);
	}
}
