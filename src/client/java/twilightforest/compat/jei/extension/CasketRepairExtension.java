package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFItems;
import twilightforest.item.recipe.CasketRepairRecipe;

import java.util.List;

public final class CasketRepairExtension implements ICraftingCategoryExtension<CasketRepairRecipe> {
	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<CasketRepairRecipe> recipeHolder) {
		ItemStack damagedCasket = new ItemStack(TFItems.KEEPSAKE_CASKET);
		damagedCasket.set(TFDataComponents.CASKET_DAMAGE, 2);
		return List.of(
			JEIExtensionHelper.stackDisplay(damagedCasket),
			new SlotDisplay.ItemSlotDisplay(TFItems.CHARM_OF_KEEPING_3)
		);
	}

	@Override
	public void setRecipe(RecipeHolder<CasketRepairRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		craftingGridHelper.createAndSetIngredientsFromDisplays(builder, this.getIngredients(recipeHolder), 0, 0);
		builder.setShapeless();

		ItemStack repairedCasket = new ItemStack(TFItems.KEEPSAKE_CASKET);
		repairedCasket.set(TFDataComponents.CASKET_DAMAGE, 1);
		craftingGridHelper.createAndSetOutputs(builder, List.of(repairedCasket));
	}
}
