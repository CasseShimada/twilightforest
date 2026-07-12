package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import twilightforest.TwilightForestMod;
import twilightforest.item.recipe.*;

import java.util.function.Supplier;

// TODO: Update recipes to use new codec serialization system. Check RecipeSerializers and ShapedRecipe classes for reference implementation.
public class TFRecipes {
	public static final RecipeSerializer<CasketRepairRecipe> CASKET_REPAIR_RECIPE = registerSerializer("casket_repair_recipe", simpleSerializer(CasketRepairRecipe::new));
	public static final RecipeSerializer<EmperorsClothRecipe> EMPERORS_CLOTH_RECIPE = registerSerializer("emperors_cloth_recipe", simpleSerializer(EmperorsClothRecipe::new));
	public static final RecipeSerializer<EssenceRepairRecipe> ESSENCE_REPAIR_RECIPE = registerSerializer("essence_repair_recipe", simpleSerializer(EssenceRepairRecipe::new));
	public static final RecipeSerializer<MagicMapCloningRecipe> MAGIC_MAP_CLONING_RECIPE = registerSerializer("magic_map_cloning_recipe", simpleSerializer(MagicMapCloningRecipe::new));
	public static final RecipeSerializer<MazeMapCloningRecipe> MAZE_MAP_CLONING_RECIPE = registerSerializer("maze_map_cloning_recipe", simpleSerializer(MazeMapCloningRecipe::new));
	public static final RecipeSerializer<MoonwormQueenRepairRecipe> MOONWORM_QUEEN_REPAIR_RECIPE = registerSerializer("moonworm_queen_repair_recipe", simpleSerializer(MoonwormQueenRepairRecipe::new));
	public static final RecipeSerializer<ScepterRepairRecipe> SCEPTER_REPAIR_RECIPE = registerSerializer("scepter_repair", ScepterRepairRecipe.SERIALIZER);
	public static final RecipeSerializer<UncraftingRecipe> UNCRAFTING_SERIALIZER = registerSerializer("uncrafting", UncraftingRecipe.SERIALIZER);
	public static final RecipeSerializer<NoTemplateSmithingRecipe> NO_TEMPLATE_SMITHING_SERIALIZER = registerSerializer("no_template_smithing", NoTemplateSmithingRecipe.SERIALIZER);
	public static final RecipeSerializer<DryingRecipe> DRYING_SERIALIZER = registerSerializer("drying", new RecipeSerializer<>(DryingRecipe.CODEC, DryingRecipe.STREAM_CODEC));

	public static final RecipeType<CraftingRecipe> UNCRAFTING_RECIPE = registerType("uncrafting", new RecipeType<>() {
		@Override
		public String toString() {
			return TwilightForestMod.prefix("uncrafting").toString();
		}
	});
	public static final RecipeType<DryingRecipe> DRYING_RECIPE = registerType("drying", new RecipeType<>() {
		@Override
		public String toString() {
			return TwilightForestMod.prefix("drying").toString();
		}
	});

	public static void init() {
	}

	private static <T extends Recipe<?>> RecipeSerializer<T> registerSerializer(String name, RecipeSerializer<T> serializer) {
		return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix(name), serializer);
	}

	private static <T extends Recipe<?>> RecipeType<T> registerType(String name, RecipeType<T> type) {
		return Registry.register(BuiltInRegistries.RECIPE_TYPE, TwilightForestMod.prefix(name), type);
	}

	private static <T extends CustomRecipe> RecipeSerializer<T> simpleSerializer(Supplier<T> factory) {
		T recipe = factory.get();
		return new RecipeSerializer<>(MapCodec.unit(recipe), StreamCodec.unit(recipe));
	}
}
