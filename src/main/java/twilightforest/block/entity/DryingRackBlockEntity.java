package twilightforest.block.entity;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import twilightforest.block.DryingRackBlock;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFRecipes;
import twilightforest.item.recipe.DryingRecipe;

import java.util.Optional;

public class DryingRackBlockEntity extends BlockEntity {

	public static final int DEFAULT_DRYING_TIME = 20 * 60 * 5;
	static final Codec<ItemStack> STORED_ITEM_CODEC = ItemStack.OPTIONAL_CODEC;
	private ItemStack stack = ItemStack.EMPTY;
	private final RecipeManager.CachedCheck<SingleRecipeInput, DryingRecipe> quickCheck = RecipeManager.createCheck(TFRecipes.DRYING_RECIPE);

	protected boolean drying;
	protected int dryTime;
	protected int totalDryTime = DEFAULT_DRYING_TIME;

	public DryingRackBlockEntity(BlockPos pos, BlockState blockState) {
		super(TFBlockEntities.DRYING_RACK, pos, blockState);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity entity) {
		if (state.getValue(DryingRackBlock.WATERLOGGED) || !(level instanceof ServerLevel serverLevel)) {
			entity.updateDryingTime(false);
			return;
		}

		if (entity.getTheItem().isEmpty()) {
			entity.updateDryingTime(false);
			return;
		}

		SingleRecipeInput input = new SingleRecipeInput(entity.getTheItem());
		RecipeHolder<DryingRecipe> recipeHolder = entity.quickCheck.getRecipeFor(input, serverLevel).orElse(null);
		entity.updateDryingTime(recipeHolder != null);

		if (recipeHolder == null) {
			return;
		}

		entity.dryTime++;
		if (entity.dryTime >= entity.totalDryTime) {
			entity.setTheItem(recipeHolder.value().assemble(input).copy());
			setChanged(level, pos, state);
		}
	}

	private void updateDryingTime(boolean drying) {
		boolean wasDrying = this.drying;
		this.drying = drying;
		if (wasDrying != drying) {
			this.setChanged();
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (this.level != null) {
			this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
		}
	}

	public ItemStack getTheItem() {
		return this.stack;
	}

	public void setTheItem(ItemStack newItem) {
		boolean updateDryTime = newItem.isEmpty() || !ItemStack.isSameItemSameComponents(this.stack, newItem);
		this.stack = newItem;
		this.stack.limitSize(1);

		if (updateDryTime) {
			this.totalDryTime = this.getDryingTime();
			this.dryTime = 0;
			this.setChanged();

			if (this.level instanceof ServerLevel serverLevel) {
				this.drying = !newItem.isEmpty()
					&& !this.getBlockState().getValue(DryingRackBlock.WATERLOGGED)
					&& this.quickCheck.getRecipeFor(new SingleRecipeInput(newItem), serverLevel).isPresent();
			} else if (newItem.isEmpty()) {
				this.drying = false;
			}
		}
	}

	public ItemStack takeTheItem() {
		ItemStack item = this.getTheItem();
		this.setTheItem(ItemStack.EMPTY);
		return item;
	}

	public boolean fillFromLootTable(ResourceKey<LootTable> lootTableKey, long seed, ServerLevel level) {
		MinecraftServer server = level.getServer();
		return this.fillFromLootTable(lootTableKey, seed, level, server.reloadableRegistries());
	}

	public boolean fillFromLootTable(ResourceKey<LootTable> lootTableKey, long seed, ServerLevel serverLevel, ReloadableServerRegistries.Holder holder) {
		LootTable lootTable = holder.getLootTable(lootTableKey);
		if (lootTable == LootTable.EMPTY) {
			return false;
		}

		LootParams params = new LootParams.Builder(serverLevel)
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.getBlockPos()))
			.create(LootContextParamSets.CHEST);

		lootTable.getRandomItems(new LootContext.Builder(params).withOptionalRandomSeed(seed).create(Optional.of(lootTableKey.identifier())), this::acceptLootTable);
		return true;
	}

	private void acceptLootTable(ItemStack stack) {
		if (this.stack.isEmpty()) {
			this.setTheItem(stack);
		}
	}

	private int getDryingTime() {
		SingleRecipeInput input = new SingleRecipeInput(this.getTheItem());
		if (this.level instanceof ServerLevel serverLevel) {
			return this.quickCheck.getRecipeFor(input, serverLevel).map(holder -> holder.value().getDryingTime()).orElse(DEFAULT_DRYING_TIME);
		}
		return DEFAULT_DRYING_TIME;
	}

	public boolean isDrying() {
		return this.drying;
	}

	public int getDryTime() {
		return this.dryTime;
	}

	public int getTotalDryTime() {
		return this.totalDryTime;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("item", STORED_ITEM_CODEC, this.stack);
		output.putInt("dry_time", this.dryTime);
		output.putInt("total_dry_time", this.totalDryTime);
		output.putBoolean("drying", this.drying);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.stack = input.read("item", STORED_ITEM_CODEC).orElse(ItemStack.EMPTY);
		this.dryTime = input.getIntOr("dry_time", 0);
		this.totalDryTime = input.getIntOr("total_dry_time", DEFAULT_DRYING_TIME);
		this.drying = input.getBooleanOr("drying", false);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveCustomOnly(registries);
	}
}
