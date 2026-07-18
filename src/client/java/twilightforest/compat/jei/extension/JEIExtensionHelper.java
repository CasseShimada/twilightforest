package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;
import java.util.Optional;

final class JEIExtensionHelper {
	private JEIExtensionHelper() {
	}

	static SlotDisplay stackDisplay(ItemStack stack) {
		return new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
	}

	static Optional<IRecipeSlotDrawable> firstSlot(List<IRecipeSlotDrawable> slots, RecipeIngredientRole role) {
		return slots.stream().filter(slot -> slot.getRole() == role).findFirst();
	}

	static List<IRecipeSlotDrawable> slots(List<IRecipeSlotDrawable> slots, RecipeIngredientRole role) {
		return slots.stream().filter(slot -> slot.getRole() == role).toList();
	}
}
