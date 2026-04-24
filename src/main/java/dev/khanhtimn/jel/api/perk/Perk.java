package dev.khanhtimn.jel.api.perk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.enchantment.LevelBasedValue;

/**
 * A non-attribute effect granted by a skill at a certain level.
 * <p>
 * Attribute effects are handled separately by
 * {@link dev.khanhtimn.jel.api.skill.AttributeEffect}.
 * <p>
 * Each perk type is registered in {@link PerkType}. Third-party mods add custom
 * types via {@link PerkType#register(ResourceLocation, MapCodec)}.
 *
 * <h2>Built-in types</h2>
 * <ul>
 * <li>{@code jel:tag} — vanilla player tag ({@link TagPerk}, STATE)</li>
 * <li>{@code jel:trait} — named, level-scaled parameter ({@link TraitPerk},
 * STATE)</li>
 * <li>{@code jel:effect} — mob effect with level-scaled amplifier
 * ({@link EffectPerk}, STATE)</li>
 * <li>{@code jel:function} — datapack function call ({@link FunctionPerk},
 * EVENT)</li>
 * <li>{@code jel:command} — inline command execution ({@link CommandPerk},
 * EVENT)</li>
 * <li>{@code jel:loot_table} — loot table reward ({@link LootTablePerk},
 * EVENT)</li>
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
     * Levels above unlock, shifted +1 for {@link LevelBasedValue} convention.
     * Returns 1 at unlock level, 2 at unlock+1, etc.
     */
    default int effectiveLevel(int currentLevel) {
        return currentLevel - unlockLevel() + 1;
    }

    /**
     * Apply this perk's effect.
     * <p>
     * {@link ApplyMode#STATE} implementations <b>must be idempotent</b> — this
     * method is called on every recompute cycle.
     */
    void apply(PerkContext ctx, int currentLevel);

    /**
     * Remove this perk's effect. For {@link ApplyMode#STATE} perks, called
     * before each recompute cycle. For {@link ApplyMode#EVENT} perks, called on
     * downward threshold crossing only.
     */
    void revoke(PerkContext ctx);

    default ApplyMode applyMode() {
        return ApplyMode.STATE;
    }

    static Perk tag(String tag, int unlockLevel) {
        return new TagPerk(unlockLevel, tag);
    }

    static Perk trait(ResourceLocation trait, int unlockLevel) {
        return new TraitPerk(unlockLevel, trait, LevelBasedValue.constant(1f));
    }

    static Perk trait(String trait, int unlockLevel) {
        return new TraitPerk(unlockLevel, ResourceLocation.parse(trait), LevelBasedValue.constant(1f));
    }

    static Perk trait(ResourceLocation trait, LevelBasedValue value, int unlockLevel) {
        return new TraitPerk(unlockLevel, trait, value);
    }

    static Perk trait(String trait, LevelBasedValue value, int unlockLevel) {
        return new TraitPerk(unlockLevel, ResourceLocation.parse(trait), value);
    }

    static Perk effect(Holder<MobEffect> effect, int amplifier, int unlockLevel) {
        return EffectPerk.of(effect, amplifier, unlockLevel);
    }

    static Perk effect(Holder<MobEffect> effect, LevelBasedValue amplifier, int unlockLevel) {
        return EffectPerk.of(effect, amplifier, unlockLevel);
    }

    static Perk effect(Holder<MobEffect> effect, int amplifier, boolean ambient, boolean showParticles, boolean showIcon, int unlockLevel) {
        return EffectPerk.of(effect, amplifier, ambient, showParticles, showIcon, unlockLevel);
    }

    static Perk effect(Holder<MobEffect> effect, LevelBasedValue amplifier, boolean ambient, boolean showParticles, boolean showIcon, int unlockLevel) {
        return EffectPerk.of(effect, amplifier, ambient, showParticles, showIcon, unlockLevel);
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
