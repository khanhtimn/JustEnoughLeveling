package dev.khanhtimn.jel.core;

import dev.khanhtimn.jel.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

//? fabric {
/*import net.minecraft.core.Registry;
*///?}

public final class ModAttributes {

	public static final ResourceLocation MAX_FOOD_LEVEL_ID =
			Constants.rl("max_food_level");

	public static final ResourceKey<Attribute> MAX_FOOD_LEVEL_KEY =
			ResourceKey.create(Registries.ATTRIBUTE, MAX_FOOD_LEVEL_ID);

	public static final Attribute MAX_FOOD_LEVEL =
			new RangedAttribute("attribute.jel.max_food_level", 20.0, 0.0, 1024.0)
					.setSyncable(true);

	//? fabric {
	/*/^*
	 * On Fabric, DefaultAttributes clinit triggers Player.createAttributes()
	 * before mod entrypoints run. Eagerly register here so the holder exists
	 * whenever this class is first loaded (which happens via the mixin).
	 ^/
	private static final Holder.Reference<Attribute> MAX_FOOD_LEVEL_HOLDER =
			Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, MAX_FOOD_LEVEL_KEY, MAX_FOOD_LEVEL);
	*///?} else {
	private static Holder.Reference<Attribute> MAX_FOOD_LEVEL_HOLDER;
	//?}

	public static Holder<Attribute> maxFoodLevel() {
		//? fabric {
		/*return MAX_FOOD_LEVEL_HOLDER;
		 *///?} else {
		return BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(MAX_FOOD_LEVEL_KEY);
		//?}
	}

	private ModAttributes() {
	}
}
