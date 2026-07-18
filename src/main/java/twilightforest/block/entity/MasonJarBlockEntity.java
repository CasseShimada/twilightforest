package twilightforest.block.entity;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import twilightforest.block.MasonJarBlock;
import twilightforest.init.TFBlockEntities;
import twilightforest.network.SetMasonJarItemPacket;

import java.util.List;
import java.util.Optional;

import static net.minecraft.world.level.block.entity.DecoratedPotBlockEntity.WobbleStyle;

public class MasonJarBlockEntity extends JarBlockEntity {
	public static final String TAG_ITEM = "item";
	public static final String LEGACY_TAG_ITEMS = "stacks";
	private static final Codec<List<ItemStack>> LEGACY_ITEMS_CODEC = ItemStack.OPTIONAL_CODEC.listOf();
	public static final String TAG_ANGLE = "rotation";

	protected final MasonJarItemStackHandler item;
	protected int itemRotation = 0;

	public MasonJarBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.MASON_JAR, pos, state);
		this.item = new MasonJarItemStackHandler(this);
	}

	public MasonJarItemStackHandler getItemHandler() {
		return this.item;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ItemStack storedItem = this.item.getItem();
		output.store(TAG_ITEM, ItemStack.OPTIONAL_CODEC, storedItem);
		// NeoForge 26.1 ItemStacksResourceHandler used this one-element list.
		output.store(LEGACY_TAG_ITEMS, LEGACY_ITEMS_CODEC, List.of(storedItem));
		output.putInt(TAG_ANGLE, this.itemRotation);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.item.setItem(readStoredItem(input));
		this.itemRotation = input.getIntOr(TAG_ANGLE, 0);
	}

	static ItemStack readStoredItem(ValueInput input) {
		Optional<ItemStack> current = input.read(TAG_ITEM, ItemStack.OPTIONAL_CODEC);
		if (current.isPresent()) return current.get();

		List<ItemStack> legacyItems = input.read(LEGACY_TAG_ITEMS, LEGACY_ITEMS_CODEC).orElse(List.of());
		for (int slot = 1; slot < legacyItems.size(); slot++) {
			if (!legacyItems.get(slot).isEmpty()) {
				throw new IllegalStateException("Mason Jar legacy stacks contained data outside its only valid slot");
			}
		}
		return legacyItems.isEmpty() ? ItemStack.EMPTY : legacyItems.getFirst();
	}

	public boolean fillFromLootTable(ResourceKey<LootTable> lootTableKey, long seed, ServerLevel level) {
		MinecraftServer currentServer = level.getServer();
		return this.fillFromLootTable(lootTableKey, seed, level, currentServer.reloadableRegistries());
	}

	public boolean fillFromLootTable(ResourceKey<LootTable> lootTableKey, long seed, ServerLevel serverLevel, ReloadableServerRegistries.Holder holder) {
		LootTable lootTable = holder.getLootTable(lootTableKey);

		if (lootTable == LootTable.EMPTY) return false;

		LootParams params = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.getBlockPos())).create(LootContextParamSets.CHEST);

		lootTable.getRandomItemsRaw(new LootContext.Builder(params).withOptionalRandomSeed(seed).create(Optional.of(lootTableKey.identifier())), this::acceptLootTable);

		return true;
	}

	private void acceptLootTable(ItemStack stack) {
		MasonJarItemStackHandler jarInv = this.getItemHandler();
		if (jarInv.isEmpty()) {
			jarInv.setItem(stack);
		} else {
			ItemStack contained = jarInv.peekItem();
			// Merge stack in if there's already an item inside
			if (ItemStack.isSameItemSameComponents(contained, stack)) {
				contained.setCount(Math.min(contained.getCount() + stack.getCount(), contained.getMaxStackSize()));
			}
		}
	}

	public void setFromItem(ItemStack stack) {
		this.applyComponentsFromItemStack(stack);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(this.item.getItem())));
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter input) {
		super.applyImplicitComponents(input);
		this.item.setItem(input.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne());
	}

	@Override
	public void removeComponentsFromTag(ValueOutput output) {
		super.removeComponentsFromTag(output);
		output.discard(TAG_ITEM);
		output.discard(LEGACY_TAG_ITEMS);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void setChanged() {
		super.setChanged();
		if (this.level != null) {
			this.syncContainedLight();
		}
		if (this.level instanceof ServerLevel serverLevel) {
			SetMasonJarItemPacket packet = new SetMasonJarItemPacket(this.getBlockPos(), this.item.getItem(), this.itemRotation);
			PlayerLookup.tracking(serverLevel, ChunkPos.containing(this.getBlockPos())).forEach(player -> ServerPlayNetworking.send(player, packet));
		}
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.getServer().execute(() -> {
				if (this.level == serverLevel && !this.isRemoved()) this.syncContainedLight();
			});
		}
	}

	private void syncContainedLight() {
		BlockPos pos = this.getBlockPos();
		BlockState state = this.getBlockState();
		if (state.hasProperty(MasonJarBlock.LIGHT_LEVEL)) {
			int lightLevel = this.item.getItem().getItem() instanceof BlockItem blockItem
				? blockItem.getBlock().defaultBlockState().getLightEmission()
				: 0;
			if (state.getValue(MasonJarBlock.LIGHT_LEVEL) != lightLevel) {
				this.level.setBlock(pos, state.setValue(MasonJarBlock.LIGHT_LEVEL, lightLevel), Block.UPDATE_ALL);
			}
		}
		this.level.getLightEngine().checkBlock(pos);
	}

	public int getItemRotation() {
		return this.itemRotation;
	}

	public void setItemRotation(int itemRotation) {
		this.itemRotation = itemRotation;
	}

	public static class MasonJarItemStackHandler extends SingleStackStorage {
		protected final MasonJarBlockEntity jarEntity;
		private ItemStack stack = ItemStack.EMPTY;
		private ItemStack pendingInitialStack;

		public MasonJarItemStackHandler(MasonJarBlockEntity jarEntity) {
			this.jarEntity = jarEntity;
		}

		// Used for simple checks of what the one item is, without going through all the hoops. Used by the renderer and when saving contents to item
		public ItemStack getItem() {
			return this.stack.copy();
		}

		// Peeks at the stored item, without cloning it
		private ItemStack peekItem() {
			return this.stack;
		}

		// Used when syncing to client and when placing a jar that already has stored items
		public void setItem(ItemStack itemStack) {
			this.pendingInitialStack = null;
			this.stack = itemStack;
		}

		@Override
		protected ItemStack getStack() {
			return this.stack;
		}

		@Override
		protected void setStack(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		protected boolean canInsert(ItemVariant resource) {
			return resource.getItem().canFitInsideContainerItems();
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			ItemStack before = this.stack.copy();
			long inserted = super.insert(resource, maxAmount, transaction);
			if (inserted > 0 && this.pendingInitialStack == null) this.pendingInitialStack = before;
			return inserted;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			ItemStack before = this.stack.copy();
			long extracted = super.extract(resource, maxAmount, transaction);
			if (extracted > 0 && this.pendingInitialStack == null) this.pendingInitialStack = before;
			return extracted;
		}

		@Override
		protected void onFinalCommit() {
			ItemStack initial = this.pendingInitialStack;
			this.pendingInitialStack = null;
			if (initial == null || ItemStack.isSameItemSameComponents(initial, this.stack) && initial.getCount() == this.stack.getCount()) {
				return;
			}

			boolean inserted = initial.isEmpty() || !this.stack.isEmpty()
				&& ItemStack.isSameItemSameComponents(initial, this.stack)
				&& this.stack.getCount() > initial.getCount();
			this.jarEntity.wobble(inserted ? WobbleStyle.POSITIVE : WobbleStyle.NEGATIVE);
			this.jarEntity.setChanged();
		}

		public boolean isItemValid(int slot, ItemStack stack) {
			return slot == 0 && stack.getItem().canFitInsideContainerItems();
		}

		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot != 0 || amount <= 0 || this.stack.isEmpty()) return ItemStack.EMPTY;

			int extracted = Math.min(amount, this.stack.getCount());
			ItemStack extractedStack = this.stack.copy();
			extractedStack.setCount(extracted);

			if (!simulate) {
				this.pendingInitialStack = null;
				this.stack.shrink(extracted);
				if (this.stack.isEmpty()) this.stack = ItemStack.EMPTY;
				this.jarEntity.wobble(WobbleStyle.NEGATIVE);
				this.jarEntity.setChanged();
			}

			return extractedStack;
		}

		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot != 0 || stack.isEmpty() || !isItemValid(slot, stack)) return stack;

			ItemStack existing = this.stack;

			if (existing.isEmpty()) {
				int toInsert = Math.min(stack.getCount(), stack.getMaxStackSize());
				if (!simulate) {
					this.pendingInitialStack = null;
					this.stack = stack.copy();
					this.stack.setCount(toInsert);
					this.jarEntity.wobble(WobbleStyle.POSITIVE);
					this.jarEntity.setChanged();
				}

				if (toInsert >= stack.getCount()) return ItemStack.EMPTY;
				ItemStack remainder = stack.copy();
				remainder.shrink(toInsert);
				return remainder;
			}

			if (!ItemStack.isSameItemSameComponents(existing, stack)) return stack;

			int space = existing.getMaxStackSize() - existing.getCount();
			if (space <= 0) return stack;

			int toAdd = Math.min(space, stack.getCount());
			if (!simulate) {
				this.pendingInitialStack = null;
				existing.grow(toAdd);
				this.jarEntity.wobble(WobbleStyle.POSITIVE);
				this.jarEntity.setChanged();
			}

			if (toAdd >= stack.getCount()) return ItemStack.EMPTY;
			ItemStack remainder = stack.copy();
			remainder.shrink(toAdd);
			return remainder;
		}

		public boolean isEmpty() {
			return this.stack.isEmpty();
		}
	}
}
