package twilightforest.item.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import twilightforest.init.TFRecipes;

public class UncraftingRecipe extends ShapedRecipe {

	private final int cost;
	private final Ingredient input;
	private final int count;
	private final ShapedRecipePattern pattern;

	public UncraftingRecipe(int cost, Ingredient input, int count, ShapedRecipePattern pattern) {
		super(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "uncrafting"), pattern, new ItemStackTemplate(Items.AIR, count));
		this.cost = cost;
		this.input = input;
		this.count = count;
		this.pattern = pattern;
	}

	@Override //This method is never used, but it has to be implemented
	public boolean matches(CraftingInput input, Level level) {
		return false;
	}

	@Override //We have to implement this method, can't really be used since we have multiple outputs
	public ItemStack assemble(CraftingInput input) {
		return ItemStack.EMPTY;
	}

	//Checks if the itemStack is a part of the ingredient when UncraftingMenu's getRecipesFor() method iterates through all recipes.
	public boolean isItemStackAnIngredient(ItemStack stack) {
		return this.input.items().anyMatch(holder -> stack.is(holder.value()) && stack.getCount() >= this.count);
	}

	@Override
	@SuppressWarnings("unchecked")
	public RecipeSerializer<ShapedRecipe> getSerializer() {
		return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) TFRecipes.UNCRAFTING_SERIALIZER.get();
	}

	@Override
	public RecipeType<CraftingRecipe> getType() {
		return TFRecipes.UNCRAFTING_RECIPE.get();
	}

	public Ingredient getInput() {
		return this.input;
	}

	public int getCount() {
		return this.count;
	}

	public int getCost() {
		return this.cost;
	}

	public static final MapCodec<UncraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.INT.optionalFieldOf("cost", -1).forGetter(UncraftingRecipe::getCost),
		Ingredient.CODEC.fieldOf("input").forGetter(UncraftingRecipe::getInput),
		Codec.INT.optionalFieldOf("input_count", 1).forGetter(UncraftingRecipe::getCount),
		ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern)
	).apply(instance, UncraftingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, UncraftingRecipe> STREAM_CODEC = StreamCodec.of(
		(buf, recipe) -> {
			buf.writeInt(recipe.cost);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input);
			buf.writeInt(recipe.count);
			ShapedRecipePattern.STREAM_CODEC.encode(buf, recipe.pattern);
		},
		buf -> new UncraftingRecipe(
			buf.readInt(),
			Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
			buf.readInt(),
			ShapedRecipePattern.STREAM_CODEC.decode(buf)
		)
	);

	public static final RecipeSerializer<UncraftingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
