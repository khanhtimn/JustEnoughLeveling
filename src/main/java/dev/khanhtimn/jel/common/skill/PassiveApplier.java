package dev.khanhtimn.jel.common.skill;

import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.core.ModRegistries;
import dev.khanhtimn.jel.data.skill.SkillsTracker;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.Map;

/**
 * Applies and revokes all passive effects from skill definitions
 * based on the player's current skill levels.
 * <p>
 * Delegates entirely to {@link SkillPassive#apply} and {@link SkillPassive#revoke}
 * for the actual effect logic.
 */
public final class PassiveApplier {

	private PassiveApplier() {}

	/**
	 * Full recompute: revoke all passives, then re-apply based on current levels.
	 */
	public static void recomputeAll(
			ServerPlayer player,
			SkillsTracker tracker,
			RegistryAccess access
	) {
		revokeAll(player, tracker, access);
		applyAll(player, tracker, access);
	}

	/**
	 * Revoke all skill-granted effects:
	 * - Clear ability flags (bulk)
	 * - Remove all skill-based attribute modifiers
	 */
	private static void revokeAll(
			ServerPlayer player,
			SkillsTracker tracker,
			RegistryAccess access
	) {
		// Bulk-clear ability flags (they'll be re-granted in applyAll)
		tracker.clearAbilityFlags();

		// Remove all attribute modifiers owned by this mod's skill system
		player.getAttributes().getSyncableAttributes().forEach(instance -> {
			var modsSnapshot = List.copyOf(instance.getModifiers());
			for (AttributeModifier mod : modsSnapshot) {
				ResourceLocation id = mod.id();
				if (Constants.MOD_ID.equals(id.getNamespace())
						&& id.getPath().startsWith("skill/")) {
					instance.removeModifier(id);
				}
			}
		});
	}

	/**
	 * Apply passives for all skills the player has progress in.
	 */
	private static void applyAll(
			ServerPlayer player,
			SkillsTracker tracker,
			RegistryAccess access
	) {
		Registry<SkillDefinition> skillRegistry =
				access.registryOrThrow(ModRegistries.SKILL_REGISTRY_KEY);

		for (Map.Entry<ResourceLocation, SkillProgress> entry :
				tracker.getAllSkills().entrySet()) {

			ResourceLocation skillId = entry.getKey();
			int level = entry.getValue().level();
			if (level <= 0) continue;

			ResourceKey<SkillDefinition> skillKey =
					ResourceKey.create(ModRegistries.SKILL_REGISTRY_KEY, skillId);

			SkillDefinition def = skillRegistry.get(skillKey);
			if (def == null) continue;

			int passiveIndex = -1;
			for (SkillPassive passive : def.passives()) {
				passiveIndex++;

				SkillPassive.Context ctx = new SkillPassive.Context(
						player, tracker, access, skillId, passiveIndex
				);

				if (level >= passive.unlockLevel()) {
					passive.apply(ctx, level);
				}
			}
		}
	}
}
