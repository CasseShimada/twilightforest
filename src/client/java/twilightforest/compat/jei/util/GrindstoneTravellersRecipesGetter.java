package twilightforest.compat.jei.util;

import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import twilightforest.item.recipe.travellers.TravellersGearModifierRecipe;
import twilightforest.item.recipe.travellers.TravellersGearModifierShapedRecipe;
import twilightforest.item.recipe.travellers.TravellersGearModifierShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

public final class GrindstoneTravellersRecipesGetter {
	private GrindstoneTravellersRecipesGetter() {
	}

	public static List<IJeiGrindstoneRecipe> getRecipes(RecipeMap recipeMap, IVanillaRecipeFactory factory) {
		List<IJeiGrindstoneRecipe> recipes = new ArrayList<>();
		List<ItemStack> registeredInputs = new ArrayList<>();

		for (RecipeHolder<? extends CraftingRecipe> holder : recipeMap.byType(RecipeType.CRAFTING)) {
			if (!(holder.value() instanceof TravellersGearModifierRecipe modifierRecipe)) {
				continue;
			}

			List<Ingredient> ingredients = getIngredients(modifierRecipe);
			if (ingredients.isEmpty()) {
				continue;
			}
			ItemStack baseGear = TravellersGearModifierRecipe.getModifiableArmorFromIngredients(ingredients);
			ItemStack modifiedGear = modifierRecipe.applyModifier(baseGear.copy(), List.of());
			if (modifiedGear.isEmpty() || registeredInputs.stream().anyMatch(stack -> ItemStack.isSameItemSameComponents(stack, modifiedGear))) {
				continue;
			}

			registeredInputs.add(modifiedGear);
			recipes.add(factory.createGrindstoneRecipe(
				List.of(modifiedGear),
				List.of(ItemStack.EMPTY),
				List.of(baseGear),
				0,
				0,
				holder.id().identifier().withPrefix("grindstone/remove_modifier/")
			));
		}
		return List.copyOf(recipes);
	}

	private static List<Ingredient> getIngredients(TravellersGearModifierRecipe recipe) {
		return switch (recipe) {
			case TravellersGearModifierShapedRecipe shaped -> shaped.getIngredients().stream().flatMap(java.util.Optional::stream).toList();
			case TravellersGearModifierShapelessRecipe shapeless -> shapeless.getIngredients();
			default -> List.of();
		};
	}
}
