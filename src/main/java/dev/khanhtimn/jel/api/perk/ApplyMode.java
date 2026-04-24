package dev.khanhtimn.jel.api.perk;

/**
 * Determines how the perk system invokes a {@link Perk}.
 *
 * <p>This mirrors vanilla's enchantment architecture:
 * <ul>
 *   <li>{@link #STATE} ≈ {@code EnchantmentLocationBasedEffect}
 *       — applied/removed every recompute cycle</li>
 *   <li>{@link #EVENT} ≈ {@code EnchantmentEntityEffect}
 *       — fired once when a trigger condition is met</li>
 * </ul>
 */
public enum ApplyMode {

	/**
	 * The perk represents persistent state that is recomputed every cycle
	 * (login, respawn, level change).
	 * <p>
	 * Implementations <b>must be idempotent</b> — {@code apply()} may be
	 * called many times and must produce the same result.
	 * <p>
	 * Example: {@link TagPerk} — {@code player.addTag()} is a no-op if
	 * the tag already exists.
	 */
	STATE,

	/**
	 * The perk is a one-shot event fired when the player's skill level
	 * crosses the {@link Perk#unlockLevel()} threshold.
	 * <p>
	 * {@code apply()} is called when the level rises above the threshold;
	 * {@code revoke()} is called when the level drops below it.
	 * Neither is called on login or respawn — the effect is expected to
	 * persist independently (items, advancements, etc.).
	 * <p>
	 * Example: {@link FunctionPerk} — executes a datapack function once.
	 */
	EVENT
}
