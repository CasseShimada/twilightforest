package twilightforest.block.entity;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFBlocks;

public class CinderFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
	private static final int SMELT_LOG_FACTOR = 10;
	private static final Component DEFAULT_NAME = Component.translatable("block.twilightforest.cinder_furnace");

	public CinderFurnaceBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.CINDER_FURNACE, pos, state, RecipeType.SMELTING);
	}

	@Override
	protected Component getDefaultName() {
		return DEFAULT_NAME;
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new FurnaceMenu(containerId, inventory, this, this.dataAccess);
	}

	// Vanilla AbstractFurnaceBlockEntity.serverTick with the historical Cinder Furnace speed,
	// output multiplication, and nearby-log conversion behavior restored.
	public static void serverTick(Level level, BlockPos pos, BlockState state, CinderFurnaceBlockEntity entity) {
		if (!(level instanceof ServerLevel serverLevel)) return;

		boolean changed = false;
		int litTimeRemaining = entity.dataAccess.get(DATA_LIT_TIME);
		boolean wasLit = litTimeRemaining > 0;
		if (wasLit) {
			litTimeRemaining--;
			entity.dataAccess.set(DATA_LIT_TIME, litTimeRemaining);
		}
		boolean isLit = litTimeRemaining > 0;

		ItemStack fuel = entity.items.get(SLOT_FUEL);
		ItemStack ingredient = entity.items.get(SLOT_INPUT);
		boolean hasIngredient = !ingredient.isEmpty();
		boolean hasFuel = !fuel.isEmpty();
		if (isLit || hasFuel && hasIngredient) {
			if (hasIngredient) {
				SingleRecipeInput input = new SingleRecipeInput(ingredient);
				RecipeHolder<? extends AbstractCookingRecipe> recipe = serverLevel.recipeAccess()
					.getRecipeFor(RecipeType.SMELTING, input, serverLevel)
					.orElse(null);
				if (recipe != null) {
					ItemStack burnResult = recipe.value().assemble(input);
					if (!burnResult.isEmpty() && entity.canBurn(serverLevel, entity.getMaxStackSize(), burnResult)) {
						if (!isLit) {
							int newLitTime = entity.getBurnDuration(serverLevel.fuelValues(), fuel);
							entity.dataAccess.set(DATA_LIT_TIME, newLitTime);
							entity.dataAccess.set(DATA_LIT_DURATION, newLitTime);
							litTimeRemaining = newLitTime;
							if (newLitTime > 0) {
								consumeFuel(entity.items, fuel);
								isLit = true;
								changed = true;
							}
						}

						if (isLit) {
							int cookingTimer = entity.dataAccess.get(DATA_COOKING_PROGRESS)
								+ entity.getCurrentSpeedMultiplier(serverLevel);
							int cookingTotalTime = entity.dataAccess.get(DATA_COOKING_TOTAL_TIME);
							entity.dataAccess.set(DATA_COOKING_PROGRESS, cookingTimer);
							if (cookingTimer >= cookingTotalTime) {
								entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
								entity.dataAccess.set(DATA_COOKING_TOTAL_TIME, recipe.value().cookingTime());
								entity.burn(serverLevel, ingredient, burnResult);
								entity.setRecipeUsed(recipe);
								changed = true;
							}
						} else {
							entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
						}
					} else {
						entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
					}
				}
			} else {
				entity.dataAccess.set(DATA_COOKING_PROGRESS, 0);
			}
		} else {
			int cookingTimer = entity.dataAccess.get(DATA_COOKING_PROGRESS);
			if (cookingTimer > 0) {
				entity.dataAccess.set(DATA_COOKING_PROGRESS, Mth.clamp(
					cookingTimer - BURN_COOL_SPEED, 0, entity.dataAccess.get(DATA_COOKING_TOTAL_TIME)));
			}
		}

		if (wasLit != isLit) {
			changed = true;
			state = state.setValue(AbstractFurnaceBlock.LIT, isLit);
			serverLevel.setBlock(pos, state, Block.UPDATE_ALL);
		}

		if (isLit && litTimeRemaining % 5 == 0) {
			entity.cinderizeNearbyLog(serverLevel, pos);
		}

		if (changed) {
			setChanged(serverLevel, pos, state);
		}
	}

	private void cinderizeNearbyLog(ServerLevel level, BlockPos origin) {
		RandomSource random = level.getRandom();
		int dx = random.nextInt(2) - random.nextInt(2);
		int dy = random.nextInt(2) - random.nextInt(2);
		int dz = random.nextInt(2) - random.nextInt(2);
		BlockPos pos = origin.offset(dx, dy, dz);

		if (level.hasChunkAt(pos)) {
			BlockState nearbyBlock = level.getBlockState(pos);
			if (!nearbyBlock.is(TFBlocks.CINDER_LOG) && nearbyBlock.is(BlockTags.LOGS)) {
				level.setBlock(pos, TFBlocks.CINDER_LOG.withPropertiesOf(nearbyBlock), Block.UPDATE_CLIENTS);
				level.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, pos, 0);
				level.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, pos, 0);
				level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
		}
	}

	private int getCurrentSpeedMultiplier(Level level) {
		return calculateMultiplier(this.countNearbyCinderLogs(level), 2, level.getRandom().nextInt(2));
	}

	private int countNearbyCinderLogs(Level level) {
		int count = 0;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos pos = this.getBlockPos().offset(dx, dy, dz);
					if (level.hasChunkAt(pos) && level.getBlockState(pos).is(TFBlocks.CINDER_LOG)) {
						count++;
					}
				}
			}
		}
		return count;
	}

	private boolean canBurn(Level level, int maxStackSize, ItemStack burnResult) {
		ItemStack resultStack = this.items.get(SLOT_RESULT);
		if (resultStack.isEmpty()) return true;
		if (!ItemStack.isSameItemSameComponents(resultStack, burnResult)) return false;

		int resultCount = resultStack.getCount()
			+ this.getMaxOutputStacks(level, this.items.get(SLOT_INPUT), burnResult);
		return resultCount <= Math.min(maxStackSize, burnResult.getMaxStackSize());
	}

	public int getMaxOutputStacks(Level level, ItemStack input, ItemStack output) {
		return output.getCount() * (this.canMultiply(input)
			? maxOutputMultiplier(this.countNearbyCinderLogs(level))
			: 1);
	}

	private static void consumeFuel(NonNullList<ItemStack> items, ItemStack fuel) {
		Item fuelItem = fuel.getItem();
		fuel.shrink(1);
		if (fuel.isEmpty()) {
			ItemStackTemplate remainder = fuelItem.getCraftingRemainder();
			items.set(SLOT_FUEL, remainder != null ? remainder.create() : ItemStack.EMPTY);
		}
	}

	private void burn(Level level, ItemStack input, ItemStack recipeResult) {
		ItemStack multipliedResult = recipeResult.copy();
		if (this.canMultiply(input)) {
			multipliedResult.setCount(recipeResult.getCount() * calculateMultiplier(
				this.countNearbyCinderLogs(level), SMELT_LOG_FACTOR, level.getRandom().nextInt(SMELT_LOG_FACTOR)));
		}

		ItemStack resultStack = this.items.get(SLOT_RESULT);
		if (resultStack.isEmpty()) {
			this.items.set(SLOT_RESULT, multipliedResult);
		} else {
			resultStack.grow(multipliedResult.getCount());
		}

		if (input.is(Items.WET_SPONGE) && this.items.get(SLOT_FUEL).is(Items.BUCKET)) {
			this.items.set(SLOT_FUEL, new ItemStack(Items.WATER_BUCKET));
		}
		input.shrink(1);
	}

	private boolean canMultiply(ItemStack input) {
		return input.is(ItemTags.LOGS) || input.is(ConventionalItemTags.ORES);
	}

	static int calculateMultiplier(int cinderLogs, int factor, int randomRoll) {
		if (factor <= 0 || randomRoll < 0 || randomRoll >= factor) {
			throw new IllegalArgumentException("factor must be positive and randomRoll must be within its range");
		}
		if (cinderLogs < factor) return 1;
		return cinderLogs / factor + (randomRoll < cinderLogs % factor ? 1 : 0);
	}

	static int maxOutputMultiplier(int cinderLogs) {
		return Math.max(1, (cinderLogs + SMELT_LOG_FACTOR - 1) / SMELT_LOG_FACTOR);
	}
}
