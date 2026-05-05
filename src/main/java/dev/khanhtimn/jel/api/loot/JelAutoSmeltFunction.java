package dev.khanhtimn.jel.api.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JelAutoSmeltFunction extends LootItemConditionalFunction {

	public static final MapCodec<JelAutoSmeltFunction> CODEC =
			RecordCodecBuilder.mapCodec(instance ->
					commonFields(instance).apply(instance, JelAutoSmeltFunction::new));

	protected JelAutoSmeltFunction(List<LootItemCondition> conditions) {
		super(conditions);
	}

	@NotNull
	@Override
	protected ItemStack run(ItemStack stack, @NotNull LootContext ctx) {
		if (stack.isEmpty()) return stack;

		ServerLevel level = ctx.getLevel();
		SingleRecipeInput input = new SingleRecipeInput(stack);
		return level.getRecipeManager()
				.getRecipeFor(RecipeType.SMELTING, input, level)
				.map(recipe -> {
					ItemStack result = recipe.value().getResultItem(level.registryAccess()).copy();
					result.setCount(stack.getCount());
					return result;
				})
				.orElse(stack);
	}

	@NotNull
	@Override
	public LootItemFunctionType<JelAutoSmeltFunction> getType() {
		return JelLootItemFunctions.AUTO_SMELT;
	}
}
