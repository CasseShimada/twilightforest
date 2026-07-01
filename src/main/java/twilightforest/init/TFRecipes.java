package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import twilightforest.TwilightForestMod;
import twilightforest.item.recipe.*;

import java.util.function.Supplier;

// TODO: Update recipes to use new codec serialization system. Check RecipeSerializers and ShapedRecipe classes for reference implementation.
public class TFRecipes {
	private static boolean typesRegistered;
	private static boolean serializersRegistered;

	public static final RecipeSerializer<CasketRepairRecipe> CASKET_REPAIR_RECIPE = simpleSerializer(CasketRepairRecipe::new);
	public static final RecipeSerializer<EmperorsClothRecipe> EMPERORS_CLOTH_RECIPE = simpleSerializer(EmperorsClothRecipe::new);
	public static final RecipeSerializer<EssenceRepairRecipe> ESSENCE_REPAIR_RECIPE = simpleSerializer(EssenceRepairRecipe::new);
	public static final RecipeSerializer<MagicMapCloningRecipe> MAGIC_MAP_CLONING_RECIPE = simpleSerializer(MagicMapCloningRecipe::new);
	public static final RecipeSerializer<MazeMapCloningRecipe> MAZE_MAP_CLONING_RECIPE = simpleSerializer(MazeMapCloningRecipe::new);
	public static final RecipeSerializer<MoonwormQueenRepairRecipe> MOONWORM_QUEEN_REPAIR_RECIPE = simpleSerializer(MoonwormQueenRepairRecipe::new);
	public static final RecipeSerializer<ScepterRepairRecipe> SCEPTER_REPAIR_RECIPE = ScepterRepairRecipe.SERIALIZER;
	public static final RecipeSerializer<UncraftingRecipe> UNCRAFTING_SERIALIZER = UncraftingRecipe.SERIALIZER;
	public static final RecipeSerializer<NoTemplateSmithingRecipe> NO_TEMPLATE_SMITHING_SERIALIZER = NoTemplateSmithingRecipe.SERIALIZER;
	public static final RecipeSerializer<DryingRecipe> DRYING_SERIALIZER = new RecipeSerializer<>(DryingRecipe.CODEC, DryingRecipe.STREAM_CODEC);

	public static final RecipeType<CraftingRecipe> UNCRAFTING_RECIPE = new RecipeType<>() {
		@Override
		public String toString() {
			return TwilightForestMod.prefix("uncrafting").toString();
		}
	};
	public static final RecipeType<DryingRecipe> DRYING_RECIPE = new RecipeType<>() {
		@Override
		public String toString() {
			return TwilightForestMod.prefix("drying").toString();
		}
	};

	public static void registerTypes() {
		if (typesRegistered) {
			return;
		}

		typesRegistered = true;
		Registry.register(BuiltInRegistries.RECIPE_TYPE, TwilightForestMod.prefix("uncrafting"), UNCRAFTING_RECIPE);
		Registry.register(BuiltInRegistries.RECIPE_TYPE, TwilightForestMod.prefix("drying"), DRYING_RECIPE);
	}

	public static void registerSerializers() {
		if (serializersRegistered) {
			return;
		}

		serializersRegistered = true;
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix("casket_repair_recipe"), CASKET_REPAIR_RECIPE);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix("emperors_cloth_recipe"), EMPERORS_CLOTH_RECIPE);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix("essence_repair_recipe"), ESSENCE_REPAIR_RECIPE);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix("magic_map_cloning_recipe"), MAGIC_MAP_CLONING_RECIPE);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix("maze_map_cloning_recipe"), MAZE_MAP_CLONING_RECIPE);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix("moonworm_queen_repair_recipe"), MOONWORM_QUEEN_REPAIR_RECIPE);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix("scepter_repair"), SCEPTER_REPAIR_RECIPE);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix("uncrafting"), UNCRAFTING_SERIALIZER);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix("no_template_smithing"), NO_TEMPLATE_SMITHING_SERIALIZER);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TwilightForestMod.prefix("drying"), DRYING_SERIALIZER);
	}

	private static <T extends CustomRecipe> RecipeSerializer<T> simpleSerializer(Supplier<T> factory) {
		T recipe = factory.get();
		return new RecipeSerializer<>(MapCodec.unit(recipe), StreamCodec.unit(recipe));
	}
}
