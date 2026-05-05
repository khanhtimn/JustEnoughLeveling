package dev.khanhtimn.jel.api;

import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.trait.TraitKey;
import dev.khanhtimn.jel.common.ModSyncedDataKeys;
import dev.khanhtimn.jel.common.PlayerSkillData;
import dev.khanhtimn.jel.common.TraitCooldownTracker;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;

public final class JelTraits {

	private JelTraits() {
	}

	// --- Core Trait Queries ---

	public static boolean has(Player player, TraitKey key) {
		return has(player, key.id());
	}

	public static boolean has(Player player, ResourceLocation traitId) {
		PlayerSkillData data = JelSkills.getSkillData(player);
		return data != null && data.hasTrait(traitId);
	}

	public static float value(Player player, TraitKey key) {
		return value(player, key.id());
	}

	public static float value(Player player, ResourceLocation traitId) {
		PlayerSkillData data = JelSkills.getSkillData(player);
		return data != null ? data.getTraitValue(traitId) : 0f;
	}

	public static boolean testChance(Player player, TraitKey key) {
		float v = value(player, key);
		return v > 0 && player.getRandom().nextFloat() < v;
	}

	public static boolean testChance(Player player, TraitKey key, LootContext ctx) {
		float v = value(player, key);
		return v > 0 && ctx.getRandom().nextFloat() < v;
	}

	// --- Branch-Gated Trait Queries ---

	public static float branchValue(Player player, ResourceKey<SkillDefinition> skillKey,
	                                ResourceLocation branchId, TraitKey key) {
		PlayerSkillData data = JelSkills.getSkillData(player);
		if (data == null || !data.isBranch(skillKey, branchId)) return 0f;
		return data.getTraitValue(key.id());
	}

	public static boolean branchHas(Player player, ResourceKey<SkillDefinition> skillKey,
	                                ResourceLocation branchId, ResourceLocation traitId) {
		PlayerSkillData data = JelSkills.getSkillData(player);
		return data != null && data.isBranch(skillKey, branchId) && data.hasTrait(traitId);
	}

	public static boolean branchTestChance(Player player, ResourceKey<SkillDefinition> skillKey,
	                                       ResourceLocation branchId, TraitKey key) {
		float v = branchValue(player, skillKey, branchId, key);
		return v > 0 && player.getRandom().nextFloat() < v;
	}

	// --- Cooldown API ---

	public static boolean isOnCooldown(Player player, ResourceLocation traitId) {
		TraitCooldownTracker cooldowns = ModSyncedDataKeys.TRAIT_COOLDOWNS.getValue(player);
		return cooldowns.isOnCooldown(traitId, player.level().getGameTime());
	}

	public static void setCooldown(Player player, ResourceLocation traitId, int durationTicks) {
		TraitCooldownTracker cooldowns = ModSyncedDataKeys.TRAIT_COOLDOWNS.getValue(player);
		cooldowns.setCooldown(traitId, player.level().getGameTime(), durationTicks);
	}
}
