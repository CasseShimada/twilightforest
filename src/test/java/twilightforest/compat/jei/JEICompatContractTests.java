package twilightforest.compat.jei;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JEICompatContractTests {
	private static final Path JEI_ROOT = Path.of("src/client/java/twilightforest/compat/jei");
	private static final Path PLUGIN = JEI_ROOT.resolve("JEICompat.java");

	@Test
	void everyTwilightForestSpecialCraftingRecipeHasAnExtension() throws IOException {
		String plugin = compact(PLUGIN);
		List<String> recipeTypes = List.of(
			"CasketRepairRecipe",
			"EmperorsClothRecipe",
			"EssenceRepairRecipe",
			"MagicMapCloningRecipe",
			"MazeMapCloningRecipe",
			"ScepterRepairRecipe",
			"TravellersGearModifierShapedRecipe",
			"TravellersGearModifierShapelessRecipe",
			"TravellersVestGlovesMergeRecipe"
		);

		for (String recipeType : recipeTypes) {
			assertTrue(plugin.contains("getCraftingCategory().addExtension(" + recipeType + ".class,"),
				() -> recipeType + " is a special recipe and must not fall through JEI's empty-display filter");
		}
		assertTrue(plugin.contains("addRecipeCategories(newMoonwormQueenCategory("),
			"Moonworm repair uses its dedicated synthetic category instead of duplicating itself in vanilla crafting");
	}

	@Test
	void scepterExtensionPublishesAllRepairIngredients() throws IOException {
		String extension = compact(JEI_ROOT.resolve("extension/ScepterRepairExtension.java"));
		assertTrue(extension.contains("getRepairItems().stream()"));
		assertTrue(extension.contains("map(net.minecraft.world.item.crafting.Ingredient::display)"));
		assertTrue(extension.contains("builder.setShapeless();"));
		assertFalse(extension.contains("FIXME"));
		assertFalse(extension.contains("returnList.of();"));
	}

	@Test
	void uncraftingCategoryTransfersItsSingleInputIntoTheTableInputSlot() throws IOException {
		String plugin = compact(PLUGIN);
		assertTrue(plugin.contains("addRecipeTransferHandler(UncraftingMenu.class,TFMenuTypes.UNCRAFTING,JEIUncraftingCategory.UNCRAFTING,0,1,20,36);"));
	}

	@Test
	void travellersModifiersAreVisibleInCraftingAndGrindstoneCategories() throws IOException {
		String plugin = compact(PLUGIN);
		String getter = compact(JEI_ROOT.resolve("util/GrindstoneTravellersRecipesGetter.java"));
		assertTrue(plugin.contains("registration.addRecipes(RecipeTypes.GRINDSTONE,GrindstoneTravellersRecipesGetter.getRecipes("));
		assertTrue(getter.contains("instanceofTravellersGearModifierRecipe"));
		assertTrue(getter.contains("ItemStack.isSameItemSameComponents"), "rotated modifier recipes must not duplicate identical grindstone entries");
		assertTrue(getter.contains("factory.createGrindstoneRecipe("));
	}

	@Test
	void dynamicSmithingOutputIsAssembledFromTheDisplayedInputs() throws IOException {
		String extension = compact(JEI_ROOT.resolve("extension/NoTemplateSmithingExtension.java"));
		assertTrue(extension.contains("recipe.assemble(newSmithingRecipeInput(ItemStack.EMPTY,base,addition))"));
		assertTrue(extension.contains("outputSlot.createDisplayOverrides().add(output);"));
	}

	@Test
	void mutableFakeItemIngredientsAreDefensivelyCopied() throws IOException {
		String helper = compact(JEI_ROOT.resolve("renderers/FakeItemEntityHelper.java"));
		assertTrue(helper.contains("returnnewFakeItemEntity(ingredient.stack().copy());"));
	}

	@Test
	void infiniteGlassSwordIsRegisteredAsAComponentBackedSubtype() throws IOException {
		String plugin = compact(PLUGIN);
		assertTrue(plugin.contains("registration.registerFromDataComponentTypes(TFItems.GLASS_SWORD,TFDataComponents.INFINITE_GLASS_SWORD);"),
			"JEI must keep the ordinary and infinite glass sword variants distinct");
	}

	private static String compact(Path source) throws IOException {
		return Files.readString(source).replaceAll("\\s+", "");
	}
}
