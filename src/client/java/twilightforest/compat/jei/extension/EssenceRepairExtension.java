package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import twilightforest.init.TFItems;
import twilightforest.item.recipe.EssenceRepairRecipe;
import twilightforest.tags.TFItemTags;

import java.util.List;

public final class EssenceRepairExtension implements ICraftingCategoryExtension<EssenceRepairRecipe> {
	private static final Ingredient SCEPTERS = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(TFItemTags.SCEPTERS));

	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<EssenceRepairRecipe> recipeHolder) {
		return List.of(SCEPTERS.display(), new SlotDisplay.ItemSlotDisplay(TFItems.EXANIMATE_ESSENCE));
	}

	@Override
	public void setRecipe(RecipeHolder<EssenceRepairRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		List<ItemStack> repairedScepters = SCEPTERS.items().map(ItemStack::new).toList();
		List<ItemStack> damagedScepters = repairedScepters.stream().map(ItemStack::copy).peek(stack -> stack.setDamageValue(stack.getMaxDamage())).toList();

		craftingGridHelper.createAndSetOutputs(builder, repairedScepters);
		craftingGridHelper.createAndSetInputs(builder, List.of(damagedScepters, List.of(new ItemStack(TFItems.EXANIMATE_ESSENCE))), 0, 0);
		builder.setShapeless();
	}

	@Override
	public void onDisplayedIngredientsUpdate(RecipeHolder<EssenceRepairRecipe> recipeHolder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
		IRecipeSlotDrawable inputSlot = JEIExtensionHelper.firstSlot(recipeSlots, RecipeIngredientRole.INPUT).orElse(null);
		IRecipeSlotDrawable outputSlot = JEIExtensionHelper.firstSlot(recipeSlots, RecipeIngredientRole.OUTPUT).orElse(null);
		if (inputSlot == null || outputSlot == null) {
			return;
		}

		focuses.getItemStackFocuses(RecipeIngredientRole.OUTPUT).findFirst().ifPresent(focus -> {
			ItemStack damaged = focus.getTypedValue().getIngredient().copy();
			damaged.setDamageValue(damaged.getMaxDamage());
			inputSlot.createDisplayOverrides().add(damaged);
		});

		inputSlot.getDisplayedItemStack().ifPresent(displayed -> {
			ItemStack repaired = displayed.copy();
			repaired.setDamageValue(0);
			outputSlot.createDisplayOverrides().add(repaired);
		});
	}
}
