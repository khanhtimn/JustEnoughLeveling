package dev.khanhtimn.jel.api.trait;

import net.minecraft.resources.ResourceLocation;

/**
 * A typed handle for a trait parameter stored in the player's flat trait map.
 * <p>
 * Wraps a pre-computed {@link ResourceLocation} using the flattened convention
 * {@code namespace:trait_name.param_name} (e.g. {@code jel:exhaustion.reduction}).
 * <p>
 * For boolean flag traits, the key is just the trait ID itself
 * (e.g. {@code jel:double_jump}).
 */
public final class TraitKey {
	private final ResourceLocation id;
	private final String paramName;

	private TraitKey(ResourceLocation id, String paramName) {
		this.id = id;
		this.paramName = paramName;
	}

	/**
	 * Creates a TraitKey from a parent trait and a parameter name.
	 * Produces a flattened key: {@code parent.namespace:parent.path.paramName}.
	 */
	public static TraitKey of(ResourceLocation parent, String paramName) {
		return new TraitKey(
				ResourceLocation.fromNamespaceAndPath(parent.getNamespace(), parent.getPath() + "." + paramName),
				paramName
		);
	}

	public static TraitKey of(String namespace, String path) {
		int dot = path.lastIndexOf('.');
		return new TraitKey(
				ResourceLocation.fromNamespaceAndPath(namespace, path),
				dot >= 0 ? path.substring(dot + 1) : null
		);
	}

	public static TraitKey of(ResourceLocation id) {
		String path = id.getPath();
		int dot = path.lastIndexOf('.');
		return new TraitKey(id, dot >= 0 ? path.substring(dot + 1) : null);
	}

	public ResourceLocation id() {
		return id;
	}

	/**
	 * Returns the parameter name portion of this key (e.g. {@code "chance"} for
	 * {@code jel:damage_reflection.chance}), or {@code null} for flag keys.
	 */
	public String paramName() {
		return paramName;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || (o instanceof TraitKey other && id.equals(other.id));
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id.toString();
	}
}
