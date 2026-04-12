package twilightforest.item.recipe;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;

public class EmperorsClothRecipe extends CustomRecipe {

	public EmperorsClothRecipe() {}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean foundInk = false;
		boolean foundItem = false;

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.is(TFItems.EMPERORS_CLOTH.get()) && !foundInk) {
					foundInk = true;
				} else if (!foundItem) {
					if (isHumanoidArmor(stack) && stack.getItem().getCraftingRemainder().create().isEmpty() && stack.get(TFDataComponents.EMPERORS_CLOTH.get()) == null) {
						foundItem = true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		}

		return foundInk && foundItem;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack item = ItemStack.EMPTY;

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (!stack.isEmpty() && isHumanoidArmor(stack) && item.isEmpty()) {
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

	private static boolean isHumanoidArmor(ItemStack stack) {
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		return equippable != null && equippable.slot().isArmor();
	}
}
