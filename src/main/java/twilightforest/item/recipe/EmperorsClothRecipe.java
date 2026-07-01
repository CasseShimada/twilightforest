package twilightforest.item.recipe;

import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;
import twilightforest.tags.TFItemTags;

public class EmperorsClothRecipe extends CustomRecipe {

	public EmperorsClothRecipe() {}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundCloth = false;
		boolean foundItem = false;

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.is(TFItems.EMPERORS_CLOTH.get()) && !foundCloth) {
					foundCloth = true;
				} else if (!foundItem) {
					if (isApplicable(stack) && stack.getItem().getCraftingRemainder() == null && stack.get(TFDataComponents.EMPERORS_CLOTH.get()) == null) {
						foundItem = true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		}

		return foundCloth && foundItem;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack item = ItemStack.EMPTY;

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (!stack.isEmpty() && isApplicable(stack) && item.isEmpty()) {
				item = stack;
			}
		}

		ItemStack copy = item.copy();
		copy.set(TFDataComponents.EMPERORS_CLOTH.get(), Unit.INSTANCE);
		return copy;
	}

	@Override
	public RecipeSerializer<EmperorsClothRecipe> getSerializer() {
		return TFRecipes.EMPERORS_CLOTH_RECIPE.get();
	}

	private static boolean isApplicable(ItemStack stack) {
		return stack.is(TFItemTags.EMPERORS_CLOTH_APPLICABLE);
	}
}
