package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.util.Unit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.item.recipe.EmperorsClothRecipe;
import twilightforest.tags.TFItemTags;

import java.util.List;

public final class EmperorsClothExtension implements ICraftingCategoryExtension<EmperorsClothRecipe> {
	private static final Ingredient APPLICABLE_ITEMS = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(TFItemTags.EMPERORS_CLOTH_APPLICABLE));

	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<EmperorsClothRecipe> recipeHolder) {
		return List.of(APPLICABLE_ITEMS.display(), new SlotDisplay.ItemSlotDisplay(TFItems.EMPERORS_CLOTH));
	}

	@Override
	public void setRecipe(RecipeHolder<EmperorsClothRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		List<ItemStack> applicableItems = APPLICABLE_ITEMS.items().map(ItemStack::new).toList();
		List<ItemStack> outputs = applicableItems.stream().map(ItemStack::copy).peek(EmperorsClothExtension::applyCloth).toList();
		craftingGridHelper.createAndSetInputs(builder, List.of(applicableItems, List.of(new ItemStack(TFItems.EMPERORS_CLOTH))), 0, 0);
		craftingGridHelper.createAndSetOutputs(builder, outputs);
		builder.setShapeless();
	}

	@Override
	public void onDisplayedIngredientsUpdate(RecipeHolder<EmperorsClothRecipe> recipeHolder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
		IRecipeSlotDrawable output = JEIExtensionHelper.firstSlot(recipeSlots, RecipeIngredientRole.OUTPUT).orElse(null);
		if (output == null) {
			return;
		}

		JEIExtensionHelper.slots(recipeSlots, RecipeIngredientRole.INPUT).stream()
			.map(slot -> slot.getDisplayedItemStack().orElse(ItemStack.EMPTY))
			.filter(APPLICABLE_ITEMS)
			.findFirst()
			.ifPresent(input -> {
				ItemStack result = input.copy();
				applyCloth(result);
				output.createDisplayOverrides().add(result);
			});
	}

	private static void applyCloth(ItemStack stack) {
		stack.set(TFDataComponents.EMPERORS_CLOTH, Unit.INSTANCE);
	}
}
