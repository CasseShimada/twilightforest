package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.item.recipe.travellers.TravellersVestGlovesMergeRecipe;

import java.util.List;

public final class TravellersVestGlovesMergeExtension implements ICraftingCategoryExtension<TravellersVestGlovesMergeRecipe> {
	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<TravellersVestGlovesMergeRecipe> recipeHolder) {
		return List.of(new SlotDisplay.ItemSlotDisplay(TFItems.TRAVELLERS_VEST), new SlotDisplay.ItemSlotDisplay(TFItems.TRAVELLERS_GLOVES));
	}

	@Override
	public void setRecipe(RecipeHolder<TravellersVestGlovesMergeRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		craftingGridHelper.createAndSetIngredientsFromDisplays(builder, this.getIngredients(recipeHolder), 0, 0);
		builder.setShapeless();

		ItemStack output = new ItemStack(TFItems.TRAVELLERS_VEST);
		output.set(TFDataComponents.TRAVELLERS_HAS_GLOVES, Unit.INSTANCE);
		craftingGridHelper.createAndSetOutputs(builder, List.of(output));
	}
}
