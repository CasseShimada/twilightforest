package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import twilightforest.item.recipe.travellers.TravellersGearModifierRecipe;
import twilightforest.item.recipe.travellers.TravellersGearModifierShapedRecipe;
import twilightforest.item.recipe.travellers.TravellersGearModifierShapelessRecipe;

import java.util.List;

public final class TravellersGearModifierExtension<R extends TravellersGearModifierRecipe> implements ICraftingCategoryExtension<R> {
	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<R> recipeHolder) {
		return switch (recipeHolder.value()) {
			case TravellersGearModifierShapedRecipe shaped -> shaped.getIngredients().stream()
				.map(Ingredient::optionalIngredientToDisplay)
				.toList();
			case TravellersGearModifierShapelessRecipe shapeless -> shapeless.getIngredients().stream()
				.map(Ingredient::display)
				.toList();
			default -> List.of();
		};
	}

	@Override
	public int getWidth(RecipeHolder<R> recipeHolder) {
		return recipeHolder.value().getWidth();
	}

	@Override
	public int getHeight(RecipeHolder<R> recipeHolder) {
		return recipeHolder.value().getHeight();
	}

	@Override
	public void setRecipe(RecipeHolder<R> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		R recipe = recipeHolder.value();
		List<SlotDisplay> ingredients = this.getIngredients(recipeHolder);
		craftingGridHelper.createAndSetIngredientsFromDisplays(builder, ingredients, recipe.getWidth(), recipe.getHeight());
		if (recipe.isShapeless()) {
			builder.setShapeless();
		}

		ItemStack gear = TravellersGearModifierRecipe.getModifiableArmorFromIngredients(ingredientValues(recipe));
		if (!gear.isEmpty()) {
			ItemStack output = recipe.applyModifier(gear, List.of());
			if (!output.isEmpty()) {
				builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 95, 19)
					.setOutputSlotBackground()
					.add(output);
			}
		}
	}

	private static List<Ingredient> ingredientValues(TravellersGearModifierRecipe recipe) {
		return switch (recipe) {
			case TravellersGearModifierShapedRecipe shaped -> shaped.getIngredients().stream().flatMap(java.util.Optional::stream).toList();
			case TravellersGearModifierShapelessRecipe shapeless -> shapeless.getIngredients();
			default -> List.of();
		};
	}
}
