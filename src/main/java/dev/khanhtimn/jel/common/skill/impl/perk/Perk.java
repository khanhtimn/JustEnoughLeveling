package dev.khanhtimn.jel.common.skill.impl.perk;

import com.mojang.serialization.Codec;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

/**
 * A non-attribute effect granted by a skill at a certain level.
 * <p>
 * Attribute effects are handled separately by
 * {@link dev.khanhtimn.jel.common.skill.impl.AttributeEffect}.
 * <p>
 * Each perk type is registered in {@link PerkType}. Third-party mods
 * add custom types via {@link PerkType#register(ResourceLocation, MapCodec)}.
 *
 * <h2>Built-in types</h2>
 * <ul>
 *   <li>{@code jel:tag} — vanilla player tag ({@link TagPerk}, STATE)</li>
 *   <li>{@code jel:effect} — mob effect with infinite duration ({@link EffectPerk}, STATE)</li>
 *   <li>{@code jel:function} — datapack function call ({@link FunctionPerk}, EVENT)</li>
 *   <li>{@code jel:command} — inline command execution ({@link CommandPerk}, EVENT)</li>
 *   <li>{@code jel:loot_table} — loot table reward ({@link LootTablePerk}, EVENT)</li>
 * </ul>
 *
 * @see ApplyMode
 * @see PerkType
 */
public interface Perk {

	Codec<Perk> CODEC = PerkType.DISPATCH_CODEC;

	PerkType<?> type();

	int unlockLevel();

	/**
	 * Apply this perk's effect.
	 * <p>
	 * {@link ApplyMode#STATE} implementations <b>must be idempotent</b> —
	 * this method is called on every recompute cycle.
	 */
	void apply(PerkContext ctx, int currentLevel);

	/**
	 * Remove this perk's effect. For {@link ApplyMode#STATE} perks, called
	 * before each recompute cycle. For {@link ApplyMode#EVENT} perks,
	 * called on downward threshold crossing only.
	 */
	void revoke(PerkContext ctx);

	default ApplyMode applyMode() {
		return ApplyMode.STATE;
	}

	static Perk tag(String tag, int unlockLevel) {
		return new TagPerk(unlockLevel, tag);
	}

	static Perk function(String grant, String revoke, int unlockLevel) {
		return FunctionPerk.of(unlockLevel, grant, revoke);
	}

	static Perk function(ResourceLocation grant, ResourceLocation revoke, int unlockLevel) {
		return FunctionPerk.of(unlockLevel, grant, revoke);
	}

	static Perk function(String grant, int unlockLevel) {
		return FunctionPerk.of(unlockLevel, grant, null);
	}

	static Perk function(ResourceLocation grant, int unlockLevel) {
		return FunctionPerk.of(unlockLevel, grant);
	}

	static Perk effect(Holder<MobEffect> effect, int amplifier, int unlockLevel) {
		return EffectPerk.of(effect, amplifier, unlockLevel);
	}

	static Perk effect(Holder<MobEffect> effect, int amplifier, boolean showParticles, int unlockLevel) {
		return EffectPerk.of(effect, amplifier, showParticles, unlockLevel);
	}

	static Perk effect(ResourceLocation effect, int amplifier, int unlockLevel) {
		return EffectPerk.of(effect, amplifier, unlockLevel);
	}

	static Perk effect(String effect, int amplifier, int unlockLevel) {
		return EffectPerk.of(effect, amplifier, unlockLevel);
	}

	static Perk command(String grantCommand, String revokeCommand, int unlockLevel) {
		return CommandPerk.of(grantCommand, revokeCommand, unlockLevel);
	}

	static Perk command(String grantCommand, int unlockLevel) {
		return CommandPerk.of(grantCommand, unlockLevel);
	}

	static Perk lootTable(ResourceLocation lootTable, int unlockLevel) {
		return LootTablePerk.of(lootTable, unlockLevel);
	}

	static Perk lootTable(String lootTable, int unlockLevel) {
		return LootTablePerk.of(lootTable, unlockLevel);
	}
}
