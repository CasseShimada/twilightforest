package twilightforest.compat;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import twilightforest.config.TFConfig;
import twilightforest.init.TFDataMaps;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;
import twilightforest.inventory.UncraftingMenu;
import twilightforest.tags.TFItemTags;
import twilightforest.util.datamaps.EntityTransformation;

import java.util.ArrayList;
import java.util.List;

public class RecipeViewerConstants {
	public static final int GENERIC_RECIPE_WIDTH = 116;
	public static final int GENERIC_RECIPE_HEIGHT = 54;
	public static final Component MOONWORM_QUEEN_TOOLTIP = Component.translatable("item.twilightforest.moonworm_queen.jei_info_message").withStyle(ChatFormatting.GREEN);

	public static final ItemStack DAMAGED_MOONWORM_QUEEN = Util.make(new ItemStack(TFItems.MOONWORM_QUEEN.get()), stack -> stack.setDamageValue(256));
	//trickery is afoot
	public static final List<ItemStack> BERRY_2_LIST = List.of(ItemStack.EMPTY, new ItemStack(TFItems.TORCHBERRIES), new ItemStack(TFItems.TORCHBERRIES), new ItemStack(TFItems.TORCHBERRIES));
	public static final List<ItemStack> BERRY_3_LIST = List.of(ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(TFItems.TORCHBERRIES), new ItemStack(TFItems.TORCHBERRIES));
	public static final List<ItemStack> BERRY_4_LIST = List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(TFItems.TORCHBERRIES));
	public static final List<ItemStack> MOONWORM_QUEEN_LIST = List.of(
		Util.make(new ItemStack(TFItems.MOONWORM_QUEEN.get()), stack -> stack.setDamageValue(192)),
		Util.make(new ItemStack(TFItems.MOONWORM_QUEEN.get()), stack -> stack.setDamageValue(128)),
		Util.make(new ItemStack(TFItems.MOONWORM_QUEEN.get()), stack -> stack.setDamageValue(64)),
		new ItemStack(TFItems.MOONWORM_QUEEN.get()));

	public static List<RecipeHolder<? extends CraftingRecipe>> getAllUncraftingRecipes(RecipeMap manager) {
		List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
		if (!TFConfig.disableUncraftingOnly) { //we only do this if uncrafting is not disabled
			for (RecipeHolder<? extends CraftingRecipe> recipeHolder : manager.byType(RecipeType.CRAFTING)) {
				CraftingRecipe recipe = recipeHolder.value();
				ItemStack result = getRecipeResult(recipe);
				if (recipe instanceof ShapedRecipe && isRecipeChill(recipeHolder, result)) {
					recipes.add(recipeHolder);
				} else if (recipe instanceof ShapelessRecipe && TFConfig.allowShapelessUncrafting && isRecipeChill(recipeHolder, result)) {
					recipes.add(recipeHolder);
				}
			}
		}
		recipes.addAll(manager.byType(TFRecipes.UNCRAFTING_RECIPE));
		return recipes;
	}

	private static final ContextMap EMPTY_DISPLAY_CONTEXT = new ContextMap.Builder()
		.create(new ContextKeySet.Builder().build());

	public static ItemStack getRecipeResult(CraftingRecipe recipe) {
		for (RecipeDisplay display : recipe.display()) {
			if (display instanceof ShapedCraftingRecipeDisplay shaped) {
				return shaped.result().resolveForFirstStack(EMPTY_DISPLAY_CONTEXT);
			}
			if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
				return shapeless.result().resolveForFirstStack(EMPTY_DISPLAY_CONTEXT);
			}
		}
		return ItemStack.EMPTY;
	}

	public static boolean isRecipeChill(RecipeHolder<?> holder, ItemStack result) {
		return !result.isEmpty() &&  //get rid of empty items
			!result.is(TFItemTags.BANNED_UNCRAFTABLES) &&  //Prevents things that are tagged as banned from showing up
			TFConfig.reverseRecipeBlacklist == TFConfig.disableUncraftingRecipes.contains(holder.id().toString()) && //remove disabled recipes
			TFConfig.flipUncraftingModIdList == TFConfig.blacklistedUncraftingModIds.contains(holder.id().identifier().getNamespace());
	}

	//all recipe viewers run this once when initializing recipes
	public static List<TransformationPowderInfo> getTransformationPowderRecipes() {
		List<EntityType<?>> inputs = new ArrayList<>();
		List<TransformationPowderInfo> info = new ArrayList<>();
		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			if (TFDataMaps.getTransformation(type) != null) {
				inputs.add(type);
			}
		}

		for (EntityType<?> input : new ArrayList<>(inputs)) {
			var output = TFDataMaps.getTransformation(input);
			if (output != null) {
				TransformationPowderInfo dummy = new TransformationPowderInfo(output.result(), input, true);
				if (!info.contains(dummy)) {
					if (inputs.contains(output.result())) {
						info.add(new TransformationPowderInfo(input, output.result(), true));
					} else {
						info.add(new TransformationPowderInfo(input, output.result(), false));
					}
				}
			}
		}
		return info;
	}

	//all recipe viewers run this once when initializing recipes
	@SuppressWarnings("deprecation")
	public static List<OminousFireInfo> getOminousFireRecipes() {
		List<EntityType<?>> inputs = new ArrayList<>();
		List<OminousFireInfo> info = new ArrayList<>();
		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			if (TFDataMaps.getOminousFire(type) != null) {
				inputs.add(type);
			}
		}

		for (EntityType<?> input : new ArrayList<>(inputs)) {
			EntityTransformation output = TFDataMaps.getOminousFire(input);
			if (output != null) {
				OminousFireInfo dummy = new OminousFireInfo(output.result(), input);
				if (!info.contains(dummy)) {
					info.add(new OminousFireInfo(input, output.result()));
				}
			}
		}
		return info;
	}

	//all recipe viewers run this once when initializing recipes
	public static List<Pair<Block, Block>> getCrumbleHornRecipes() {
		List<Pair<Block, Block>> info = new ArrayList<>();
		for (Block input : BuiltInRegistries.BLOCK) {
			var output = TFDataMaps.getCrumble(input);
			if (output != null) {
				info.add(Pair.of(input, output.result()));
			}
		}
		return info;
	}

	public static int getRecipeCost(List<ItemStack> inputs) {
		int cost = 0;
		for (ItemStack stack : inputs) {
			if (UncraftingMenu.isDamageableComponent(stack) && !UncraftingMenu.isIngredientProblematic(stack) && !UncraftingMenu.isMarked(stack)) {
				cost++;
			}
		}
		return cost;
	}

	public static int getXPColor(int cost) {
		if (Minecraft.getInstance().player.experienceLevel < cost && !Minecraft.getInstance().player.getAbilities().instabuild) {
			return 0xA00000;
		} else {
			return 0x80FF20;
		}
	}

	public record TransformationPowderInfo(EntityType<?> input, EntityType<?> output, boolean reversible) {
	}

	public record OminousFireInfo(EntityType<?> input, EntityType<?> output) {
	}
}
