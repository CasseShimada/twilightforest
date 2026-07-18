package twilightforest.item.recipe.travellers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import twilightforest.TFRegistries;
import twilightforest.init.TFRecipes;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.List;
import java.util.stream.Stream;

public class TravellersGearModifierShapelessRecipe extends TravellersGearModifierRecipe {
	public static final MapCodec<TravellersGearModifierShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients),
		RegistryFixedCodec.create(TFRegistries.Keys.TRAVELLERS_MODIFIERS).fieldOf("modifier_key").forGetter(recipe -> recipe.travellersModifier)
	).apply(instance, TravellersGearModifierShapelessRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, TravellersGearModifierShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.ingredients,
		ByteBufCodecs.holderRegistry(TFRegistries.Keys.TRAVELLERS_MODIFIERS), recipe -> recipe.travellersModifier,
		TravellersGearModifierShapelessRecipe::new
	);
	public static final RecipeSerializer<TravellersGearModifierShapelessRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final List<Ingredient> ingredients;

	public TravellersGearModifierShapelessRecipe(List<Ingredient> ingredients, Holder<TravellersModifier> travellersModifier) {
		super(travellersModifier);
		this.ingredients = List.copyOf(ingredients);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (!super.matches(input, level) || input.ingredientCount() != this.ingredients.size()) {
			return false;
		}
		if (this.ingredients.stream().noneMatch(Ingredient::requiresTesting)) {
			return input.stackedContents().canCraft(this.ingredients, null);
		}
		List<net.minecraft.world.item.ItemStack> items = input.items().stream().filter(stack -> !stack.isEmpty()).toList();
		return matchesIngredients(items, new boolean[items.size()], 0);
	}

	private boolean matchesIngredients(List<net.minecraft.world.item.ItemStack> items, boolean[] used, int ingredientIndex) {
		if (ingredientIndex == this.ingredients.size()) {
			return true;
		}

		Ingredient ingredient = this.ingredients.get(ingredientIndex);
		for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
			if (used[itemIndex] || !ingredient.test(items.get(itemIndex))) {
				continue;
			}
			used[itemIndex] = true;
			if (matchesIngredients(items, used, ingredientIndex + 1)) {
				return true;
			}
			used[itemIndex] = false;
		}
		return false;
	}

	public boolean canCraftInDimensions(int width, int height) {
		return this.ingredients.size() <= width * height;
	}

	public List<Ingredient> getIngredients() {
		return this.ingredients;
	}

	@Override
	protected Stream<Ingredient> ingredientValues() {
		return this.ingredients.stream();
	}

	@Override
	public int getWidth() {
		return this.ingredients.size() > 4 ? 3 : 2;
	}

	@Override
	public int getHeight() {
		return this.ingredients.size() > 4 ? 3 : 2;
	}

	@Override
	public boolean isShapeless() {
		return true;
	}

	@Override
	public RecipeSerializer<TravellersGearModifierShapelessRecipe> getSerializer() {
		return TFRecipes.MODIFIER_SHAPELESS_RECIPE_SERIALIZER;
	}
}
