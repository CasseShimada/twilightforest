package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import twilightforest.util.registry.DeferredHolder;
import twilightforest.util.registry.DeferredRegister;
import twilightforest.TwilightForestMod;
import twilightforest.item.recipe.*;

import java.util.function.Supplier;

// TODO: Update recipes to use new codec serialization system. Check RecipeSerializers and ShapedRecipe classes for reference implementation.
public class TFRecipes {
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TwilightForestMod.ID);
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, TwilightForestMod.ID);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CasketRepairRecipe>> CASKET_REPAIR_RECIPE = RECIPE_SERIALIZERS.register("casket_repair_recipe", simpleSerializer(CasketRepairRecipe::new));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EmperorsClothRecipe>> EMPERORS_CLOTH_RECIPE = RECIPE_SERIALIZERS.register("emperors_cloth_recipe", simpleSerializer(EmperorsClothRecipe::new));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EssenceRepairRecipe>> ESSENCE_REPAIR_RECIPE = RECIPE_SERIALIZERS.register("essence_repair_recipe", simpleSerializer(EssenceRepairRecipe::new));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MagicMapCloningRecipe>> MAGIC_MAP_CLONING_RECIPE = RECIPE_SERIALIZERS.register("magic_map_cloning_recipe", simpleSerializer(MagicMapCloningRecipe::new));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MazeMapCloningRecipe>> MAZE_MAP_CLONING_RECIPE = RECIPE_SERIALIZERS.register("maze_map_cloning_recipe", simpleSerializer(MazeMapCloningRecipe::new));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MoonwormQueenRepairRecipe>> MOONWORM_QUEEN_REPAIR_RECIPE = RECIPE_SERIALIZERS.register("moonworm_queen_repair_recipe", simpleSerializer(MoonwormQueenRepairRecipe::new));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ScepterRepairRecipe>> SCEPTER_REPAIR_RECIPE = RECIPE_SERIALIZERS.register("scepter_repair", () -> ScepterRepairRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<UncraftingRecipe>> UNCRAFTING_SERIALIZER = RECIPE_SERIALIZERS.register("uncrafting", () -> UncraftingRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<NoTemplateSmithingRecipe>> NO_TEMPLATE_SMITHING_SERIALIZER = RECIPE_SERIALIZERS.register("no_template_smithing", () -> NoTemplateSmithingRecipe.SERIALIZER);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DryingRecipe>> DRYING_SERIALIZER = RECIPE_SERIALIZERS.register("drying", () -> new RecipeSerializer<>(DryingRecipe.CODEC, DryingRecipe.STREAM_CODEC));

	public static final DeferredHolder<RecipeType<?>, RecipeType<CraftingRecipe>> UNCRAFTING_RECIPE = RECIPE_TYPES.register("uncrafting", () -> new RecipeType<>() {
		@Override
		public String toString() {
			return TwilightForestMod.prefix("uncrafting").toString();
		}
	});
	public static final DeferredHolder<RecipeType<?>, RecipeType<DryingRecipe>> DRYING_RECIPE = RECIPE_TYPES.register("drying", () -> new RecipeType<>() {
		@Override
		public String toString() {
			return TwilightForestMod.prefix("drying").toString();
		}
	});

	private static <T extends CustomRecipe> Supplier<RecipeSerializer<T>> simpleSerializer(Supplier<T> factory) {
		return () -> {
			T recipe = factory.get();
			return new RecipeSerializer<>(MapCodec.unit(recipe), StreamCodec.unit(recipe));
		};
	}
}
