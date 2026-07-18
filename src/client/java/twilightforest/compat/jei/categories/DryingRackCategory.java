package twilightforest.compat.jei.categories;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import twilightforest.TwilightForestMod;
import twilightforest.compat.RecipeViewerConstants;
import twilightforest.init.TFBlocks;
import twilightforest.item.recipe.DryingRecipe;

public class DryingRackCategory implements IRecipeCategory<DryingRecipe> {
	public static final IRecipeType<DryingRecipe> DRYING = IRecipeType.create(TwilightForestMod.prefix("drying"), DryingRecipe.class);
	private final IDrawable icon;
	private final Component localizedName;

	public DryingRackCategory(IGuiHelper guiHelper) {
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(TFBlocks.OAK_DRYING_RACK));
		this.localizedName = Component.translatable("gui.twilightforest.drying_jei");
	}

	@Override
	public IRecipeType<DryingRecipe> getRecipeType() {
		return DRYING;
	}

	@Override
	public Component getTitle() {
		return this.localizedName;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public int getWidth() {
		return RecipeViewerConstants.GENERIC_RECIPE_WIDTH;
	}

	@Override
	public int getHeight() {
		return RecipeViewerConstants.GENERIC_RECIPE_HEIGHT;
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, DryingRecipe recipe, IFocusGroup focuses) {
		builder.addAnimatedRecipeArrow(recipe.getDryingTime()).setPosition(47, 18);
		builder.addText(formatDryingTime(recipe.getDryingTime()), 0, 43)
			.setPosition(0, 43, this.getWidth(), 10, HorizontalAlignment.CENTER, VerticalAlignment.TOP)
			.setTextAlignment(HorizontalAlignment.CENTER)
			.setColor(0xFF404040);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 20, 19)
			.setStandardSlotBackground()
			.add(recipe.getInput());

		builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 19)
			.setOutputSlotBackground()
			.add(recipe.getResult());
	}

	private static Component formatDryingTime(int ticks) {
		if (ticks < 20) {
			return Component.translatable("gui.twilightforest.drying_ticks", ticks);
		}

		int seconds = ticks / 20;
		int minutes = seconds / 60;
		seconds %= 60;

		if (minutes > 0 && seconds > 0) {
			return Component.translatable("gui.twilightforest.drying_time", minutes, seconds);
		}
		if (minutes > 0) {
			return Component.translatable(minutes == 1 ? "gui.twilightforest.drying_minute" : "gui.twilightforest.drying_minutes", minutes);
		}
		return Component.translatable(seconds == 1 ? "gui.twilightforest.drying_second" : "gui.twilightforest.drying_seconds", seconds);
	}
}
