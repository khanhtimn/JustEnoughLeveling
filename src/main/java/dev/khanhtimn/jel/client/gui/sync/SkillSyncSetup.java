package dev.khanhtimn.jel.client.gui.sync;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SimpleBinding;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEvent;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import dev.khanhtimn.jel.api.JelRegistries;
import dev.khanhtimn.jel.api.JelSkills;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.common.PlayerSkillData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class SkillSyncSetup {

	private final Map<ResourceLocation, SimpleBinding<Integer>> levelBindings = new LinkedHashMap<>();
	private final Map<ResourceLocation, SimpleBinding<Integer>> xpBindings = new LinkedHashMap<>();
	private final RPCEvent levelUpRPC;
	private final Player player;

	public SkillSyncSetup(Player player) {
		this.player = player;
		Registry<SkillDefinition> registry = player.level().registryAccess()
				.registryOrThrow(JelRegistries.SKILL_REGISTRY_KEY);
		PlayerSkillData data = JelSkills.getSkillData(player);

		for (var entry : registry.entrySet()) {
			ResourceLocation skillId = entry.getKey().location();

			var levelBinding = DataBindingBuilder.intValS2C(
					() -> JelSkills.getSkillData(this.player).getLevel(skillId))
					.name("lvl_" + skillId)
					.initialValue(data.getLevel(skillId))
					.build();
			levelBindings.put(skillId, levelBinding);

			var xpBinding = DataBindingBuilder.intValS2C(
					() -> JelSkills.getSkillData(this.player).getXp(skillId))
					.name("xp_" + skillId)
					.initialValue(data.getXp(skillId))
					.build();
			xpBindings.put(skillId, xpBinding);
		}

		levelUpRPC = RPCEventBuilder.simple(ResourceLocation.class, (ResourceLocation skillId) -> {
			if (this.player instanceof ServerPlayer sp) {
				var key = ResourceKey.create(JelRegistries.SKILL_REGISTRY_KEY, skillId);
				JelSkills.tryLevelUpOnce(sp, key);
			}
		});
	}

	public SimpleBinding<Integer> getLevelBinding(ResourceLocation skillId) {
		return levelBindings.get(skillId);
	}

	public SimpleBinding<Integer> getXpBinding(ResourceLocation skillId) {
		return xpBindings.get(skillId);
	}

	public RPCEvent getLevelUpRPC() {
		return levelUpRPC;
	}

	public Set<ResourceLocation> getSkillIds() {
		return levelBindings.keySet();
	}

	public Player getPlayer() {
		return player;
	}
}
