package dev.khanhtimn.jel.api.skill;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

/**
 * Discriminated icon type: either an item reference (renders item model)
 * or a raw texture path (renders sprite).
 * <pre>{@code
 * // JSON — item shorthand (datapack-friendly):
 * "icon": "minecraft:bow"
 *
 * // JSON — explicit texture:
 * "icon": { "texture": "mymod:textures/gui/skill/custom_icon.png" }
 * }</pre>
 */
public sealed interface SkillIcon {

	/**
	 * Plain string → {@link ItemIcon}; object with {@code "texture"} key → {@link TextureIcon}.
	 */
	Codec<SkillIcon> CODEC = Codec.either(
			ResourceLocation.CODEC,
			TextureIcon.CODEC
	).xmap(
			either -> either.map(ItemIcon::new, Function.identity()),
			icon -> switch (icon) {
				case ItemIcon i -> Either.left(i.itemId());
				case TextureIcon t -> Either.right(t);
			}
	);

	IGuiTexture toGuiTexture();

	record ItemIcon(ResourceLocation itemId) implements SkillIcon {
		@Override
		public IGuiTexture toGuiTexture() {
			var item = BuiltInRegistries.ITEM.get(itemId);
			return new ItemStackTexture(new ItemStack(item));
		}
	}

	record TextureIcon(ResourceLocation texture) implements SkillIcon {
		public static final Codec<TextureIcon> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						ResourceLocation.CODEC.fieldOf("texture").forGetter(TextureIcon::texture)
				).apply(instance, TextureIcon::new)
		);

		@Override
		public IGuiTexture toGuiTexture() {
			return SpriteTexture.of(texture);
		}
	}
}
