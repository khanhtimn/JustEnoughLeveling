package dev.khanhtimn.jel.registry.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

public record SkillDisplay(
		Component name,
		Component description,
		ResourceLocation iconItem,
		int color
) {
	public static final Codec<SkillDisplay> CODEC =
			RecordCodecBuilder.create(instance -> instance.group(
					ComponentSerialization.CODEC.fieldOf("name")
							.forGetter(SkillDisplay::name),
					ComponentSerialization.CODEC.optionalFieldOf(
									"description",
									Component.empty()
							)
							.forGetter(SkillDisplay::description),
					ResourceLocation.CODEC.fieldOf("icon_item")
							.forGetter(SkillDisplay::iconItem),
					Codec.INT.optionalFieldOf("color", 0xFFFFFF)
							.forGetter(SkillDisplay::color)
			).apply(instance, SkillDisplay::new));
}
