package twilightforest.item.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.level.Level;
import twilightforest.init.TFRecipes;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;

public class NoTemplateSmithingRecipe implements SmithingRecipe {

	private final Ingredient base;
	private final Optional<Ingredient> addition;
	private final List<TypedDataComponent<?>> additionalData;
	@Nullable
	private PlacementInfo placementInfo;

	public NoTemplateSmithingRecipe(Ingredient base, Optional<Ingredient> addition, List<TypedDataComponent<?>> additionalData) {
		this.base = base;
		this.addition = addition;
		this.additionalData = additionalData;
	}

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	@Override
	public boolean matches(SmithingRecipeInput input, Level level) {
		ItemStack armor = input.getItem(1);

		for (TypedDataComponent<?> data : this.additionalData)
			if (armor.has(data.type()))
				return false;

		return SmithingRecipe.super.matches(input, level);
	}

	@Override
	public Optional<Ingredient> templateIngredient() {
		return Optional.empty();
	}

	@Override
	public Ingredient baseIngredient() {
		return this.base;
	}

	@Override
	public Optional<Ingredient> additionIngredient() {
		return this.addition;
	}

	@Override
	public ItemStack assemble(SmithingRecipeInput input) {
		return Util.make(input.getItem(1).copy(), this::setComponents);
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public String group() {
		return "";
	}

	private List<TypedDataComponent<?>> additionalData() {
		return this.additionalData;
	}

	private void setComponents(ItemStack itemstack) {
		for (TypedDataComponent<?> data : this.additionalData)
			setComponent(data, itemstack);
	}

	private static <T> void setComponent(TypedDataComponent<T> data, ItemStack stack) {
		stack.set(data.type(), data.value());
	}

	private static <T> void setComponent(TypedDataComponent<T> data, DataComponentMap.Builder builder) {
		builder.set(data.type(), data.value());
	}

	@Override
	public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
		return TFRecipes.NO_TEMPLATE_SMITHING_SERIALIZER;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (this.placementInfo == null) {
			this.placementInfo = PlacementInfo.createFromOptionals(List.of(Optional.empty(), Optional.of(this.base), this.addition));
		}

		return this.placementInfo;
	}

	@Override
	public List<RecipeDisplay> display() {
		SlotDisplay slotdisplay = this.base.display();
		SlotDisplay slotdisplay1 = Ingredient.optionalIngredientToDisplay(this.addition);
		return List.of(
			new SmithingRecipeDisplay(
				SlotDisplay.Empty.INSTANCE,
				slotdisplay,
				slotdisplay1,
				slotdisplay,
				new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
			)
		);
	}

	private static final Codec<List<TypedDataComponent<?>>> DATA_COMPONENT_CODEC = DataComponentMap.CODEC.xmap(typedDataComponents -> typedDataComponents.stream().toList(), typedDataComponents -> {
		DataComponentMap.Builder builder = DataComponentMap.builder();

		for (TypedDataComponent<?> typedDataComponent : typedDataComponents)
			setComponent(typedDataComponent, builder);

		return builder.build();
	});

	public static final MapCodec<NoTemplateSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Ingredient.CODEC.fieldOf("base").forGetter(NoTemplateSmithingRecipe::baseIngredient),
		Ingredient.CODEC.optionalFieldOf("addition").forGetter(NoTemplateSmithingRecipe::additionIngredient),
		DATA_COMPONENT_CODEC.optionalFieldOf("additional_data", List.of()).forGetter(NoTemplateSmithingRecipe::additionalData)
	).apply(instance, NoTemplateSmithingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, NoTemplateSmithingRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, NoTemplateSmithingRecipe::baseIngredient,
		Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, NoTemplateSmithingRecipe::additionIngredient,
		TypedDataComponent.STREAM_CODEC.apply(ByteBufCodecs.list()), NoTemplateSmithingRecipe::additionalData,
		NoTemplateSmithingRecipe::new
	);

	public static final RecipeSerializer<NoTemplateSmithingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
