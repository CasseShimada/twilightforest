package twilightforest.item.recipe.travellers;

import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;

import java.util.List;
import java.util.Optional;

public class TravellersVestGlovesMergeRecipe extends CustomRecipe {
	@Override
	public boolean matches(CraftingInput input, Level level) {
		return resolve(input).map(pair -> !pair.vest().has(TFDataComponents.TRAVELLERS_HAS_GLOVES)).orElse(false);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		Optional<InputPair> pair = resolve(input);
		if (pair.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack vest = pair.get().vest().copy();
		vest.set(TFDataComponents.TRAVELLERS_HAS_GLOVES, Unit.INSTANCE);
		return vest;
	}

	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<TravellersVestGlovesMergeRecipe> getSerializer() {
		return TFRecipes.TRAVELLERS_VEST_GLOVES_MERGE_RECIPE_SERIALIZER;
	}

	private static Optional<InputPair> resolve(CraftingInput input) {
		List<ItemStack> items = input.items().stream().filter(stack -> !stack.isEmpty()).toList();
		if (items.size() != 2) {
			return Optional.empty();
		}

		Optional<ItemStack> vest = items.stream().filter(stack -> stack.is(TFItems.TRAVELLERS_VEST)).findFirst();
		Optional<ItemStack> gloves = items.stream().filter(stack -> stack.is(TFItems.TRAVELLERS_GLOVES)).findFirst();
		return vest.flatMap(vestStack -> gloves.map(glovesStack -> new InputPair(vestStack, glovesStack)));
	}

	private record InputPair(ItemStack vest, ItemStack gloves) {
	}
}
