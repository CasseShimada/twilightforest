package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import twilightforest.item.recipe.ScepterRepairRecipe;

import java.util.List;

public class ScepterRepairExtension implements ICraftingCategoryExtension<ScepterRepairRecipe> {

	public void setRecipe(RecipeHolder<ScepterRepairRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		craftingGridHelper.createAndSetIngredientsFromDisplays(builder, this.getIngredients(recipeHolder), 0, 0);
		builder.setShapeless();

		ItemStack repairedScepter = new ItemStack(recipeHolder.value().getScepter());
		repairedScepter.setDamageValue(Math.max(0, repairedScepter.getMaxDamage() - recipeHolder.value().getRepairDurability()));
		craftingGridHelper.createAndSetOutputs(builder, List.of(repairedScepter));
	}

	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<ScepterRepairRecipe> recipeHolder) {
		ItemStack damagedScepter = new ItemStack(recipeHolder.value().getScepter());
		damagedScepter.setDamageValue(damagedScepter.getMaxDamage());

		List<SlotDisplay> ingredients = new java.util.ArrayList<>();
		ingredients.add(new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(damagedScepter)));
		recipeHolder.value().getRepairItems().stream()
			.map(net.minecraft.world.item.crafting.Ingredient::display)
			.forEach(ingredients::add);
		return List.copyOf(ingredients);
	}
}
