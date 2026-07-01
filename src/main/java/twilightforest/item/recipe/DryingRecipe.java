package twilightforest.item.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import twilightforest.init.TFRecipes;

import java.util.List;

public class DryingRecipe implements Recipe<SingleRecipeInput> {

	private final Ingredient ingredient;
	private final ItemStackTemplate result;
	private final int dryingTime;
	private PlacementInfo placementInfo;

	public DryingRecipe(Ingredient ingredient, ItemStackTemplate result, int dryingTime) {
		this.ingredient = ingredient;
		this.result = result;
		this.dryingTime = dryingTime;
	}

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return this.ingredient.test(input.item());
	}

	@Override
	public ItemStack assemble(SingleRecipeInput input) {
		return this.result.create();
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
		return TFRecipes.DRYING_SERIALIZER;
	}

	@Override
	public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
		return TFRecipes.DRYING_RECIPE;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (this.placementInfo == null) {
			this.placementInfo = PlacementInfo.create(this.ingredient);
		}
		return this.placementInfo;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

	public Ingredient getInput() {
		return this.ingredient;
	}

	public ItemStack getResult() {
		return this.result.create();
	}

	public ItemStackTemplate getResultTemplate() {
		return this.result;
	}

	public int getDryingTime() {
		return this.dryingTime;
	}

	public static final MapCodec<DryingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Ingredient.CODEC.fieldOf("input").forGetter(DryingRecipe::getInput),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(DryingRecipe::getResultTemplate),
		Codec.INT.fieldOf("filter_time").forGetter(DryingRecipe::getDryingTime)
	).apply(instance, DryingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, DryingRecipe::getInput,
		ItemStackTemplate.STREAM_CODEC, DryingRecipe::getResultTemplate,
		ByteBufCodecs.INT, DryingRecipe::getDryingTime,
		DryingRecipe::new
	);
}
