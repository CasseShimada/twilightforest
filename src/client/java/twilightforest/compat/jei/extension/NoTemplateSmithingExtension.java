package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import twilightforest.item.recipe.NoTemplateSmithingRecipe;

@SuppressWarnings("NonExtendableApiUsage")
public class NoTemplateSmithingExtension implements ISmithingCategoryExtension<NoTemplateSmithingRecipe> {

	@Override
	public <T extends IIngredientAcceptor<T>> void setTemplate(NoTemplateSmithingRecipe recipe, T ingredientAcceptor) {

	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setBase(NoTemplateSmithingRecipe recipe, T acceptor) {
		acceptor.add(recipe.baseIngredient());
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setAddition(NoTemplateSmithingRecipe recipe, T acceptor) {
		recipe.additionIngredient().ifPresent(acceptor::add);
	}

	@Override
	public void onDisplayedIngredientsUpdate(NoTemplateSmithingRecipe recipe, IRecipeSlotDrawable templateSlot, IRecipeSlotDrawable baseSlot,
											 IRecipeSlotDrawable additionSlot, IRecipeSlotDrawable outputSlot, IFocusGroup focuses) {
		ItemStack base = baseSlot.getDisplayedItemStack().orElse(ItemStack.EMPTY);
		ItemStack addition = additionSlot.getDisplayedItemStack().orElse(ItemStack.EMPTY);
		ItemStack output = recipe.assemble(new SmithingRecipeInput(ItemStack.EMPTY, base, addition));
		if (!output.isEmpty()) {
			outputSlot.createDisplayOverrides().add(output);
		}
	}
}
