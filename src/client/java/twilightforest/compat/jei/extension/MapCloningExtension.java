package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public final class MapCloningExtension<R extends CraftingRecipe> implements ICraftingCategoryExtension<R> {
	private final Item filledMap;
	private final Item emptyMap;

	public MapCloningExtension(Item filledMap, Item emptyMap) {
		this.filledMap = filledMap;
		this.emptyMap = emptyMap;
	}

	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<R> recipeHolder) {
		return List.of(new SlotDisplay.ItemSlotDisplay(this.filledMap), new SlotDisplay.ItemSlotDisplay(this.emptyMap));
	}

	@Override
	public void setRecipe(RecipeHolder<R> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		craftingGridHelper.createAndSetIngredientsFromDisplays(builder, this.getIngredients(recipeHolder), 0, 0);
		craftingGridHelper.createAndSetOutputs(builder, List.of(new ItemStack(this.filledMap, 2)));
		builder.setShapeless();
	}

	@Override
	public void onDisplayedIngredientsUpdate(RecipeHolder<R> recipeHolder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
		IRecipeSlotDrawable output = JEIExtensionHelper.firstSlot(recipeSlots, RecipeIngredientRole.OUTPUT).orElse(null);
		if (output == null) {
			return;
		}

		JEIExtensionHelper.slots(recipeSlots, RecipeIngredientRole.INPUT).stream()
			.map(slot -> slot.getDisplayedItemStack().orElse(ItemStack.EMPTY))
			.filter(stack -> stack.is(this.filledMap))
			.findFirst()
			.ifPresent(filled -> {
				ItemStack result = filled.copyWithCount(2);
				output.createDisplayOverrides().add(result);
			});
	}
}
