package twilightforest.item.recipe.travellers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import twilightforest.TFRegistries;
import twilightforest.init.TFRecipes;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TravellersGearModifierShapedRecipe extends TravellersGearModifierRecipe {
	public static final MapCodec<TravellersGearModifierShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ShapedRecipePattern.MAP_CODEC.fieldOf("pattern").forGetter(recipe -> recipe.pattern),
		RegistryFixedCodec.create(TFRegistries.Keys.TRAVELLERS_MODIFIERS).fieldOf("modifier_key").forGetter(recipe -> recipe.travellersModifier),
		Codec.BOOL.fieldOf("is_rotated").forGetter(recipe -> recipe.isRotated)
	).apply(instance, TravellersGearModifierShapedRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, TravellersGearModifierShapedRecipe> STREAM_CODEC = StreamCodec.composite(
		ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
		ByteBufCodecs.holderRegistry(TFRegistries.Keys.TRAVELLERS_MODIFIERS), recipe -> recipe.travellersModifier,
		ByteBufCodecs.BOOL, recipe -> recipe.isRotated,
		TravellersGearModifierShapedRecipe::new
	);
	public static final RecipeSerializer<TravellersGearModifierShapedRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapedRecipePattern pattern;
	private final boolean isRotated;

	public TravellersGearModifierShapedRecipe(ShapedRecipePattern pattern, Holder<TravellersModifier> travellersModifier, boolean isRotated) {
		super(travellersModifier);
		this.pattern = pattern;
		this.isRotated = isRotated;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return super.matches(input, level) && this.pattern.matches(input);
	}

	public boolean canCraftInDimensions(int width, int height) {
		return getHeight() <= height && getWidth() <= width;
	}

	@Override
	public int getWidth() {
		return this.pattern.width();
	}

	@Override
	public int getHeight() {
		return this.pattern.height();
	}

	@Override
	public boolean isShapeless() {
		return false;
	}

	public List<Optional<Ingredient>> getIngredients() {
		return this.pattern.ingredients();
	}

	@Override
	protected Stream<Ingredient> ingredientValues() {
		return this.pattern.ingredients().stream().flatMap(Optional::stream);
	}

	@Override
	public Identifier getId() {
		return super.getId().withSuffix(this.isRotated ? "_rotated" : "");
	}

	@Override
	public RecipeSerializer<TravellersGearModifierShapedRecipe> getSerializer() {
		return TFRecipes.MODIFIER_SHAPED_RECIPE_SERIALIZER;
	}
}
