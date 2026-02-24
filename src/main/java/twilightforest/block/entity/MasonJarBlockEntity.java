package twilightforest.block.entity;

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
import twilightforest.network.PacketDistributor;
import twilightforest.init.TFBlockEntities;
import twilightforest.network.SetMasonJarItemPacket;

import java.util.List;
import java.util.Optional;

import static net.minecraft.world.level.block.entity.DecoratedPotBlockEntity.WobbleStyle;

public class MasonJarBlockEntity extends JarBlockEntity {
	public static final String TAG_ITEM = "item";
	public static final String TAG_ANGLE = "rotation";

	protected final MasonJarItemStackHandler item;
	protected int itemRotation = 0;

	public MasonJarBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.MASON_JAR.get(), pos, state);
		this.item = new MasonJarItemStackHandler(this);
	}

	public MasonJarItemStackHandler getItemHandler() {
		return this.item;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store(TAG_ITEM, ItemStack.OPTIONAL_CODEC, this.item.getItem());
		output.putInt(TAG_ANGLE, this.itemRotation);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.item.setItem(input.read(TAG_ITEM, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
		this.itemRotation = input.getIntOr(TAG_ANGLE, 0);
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
	}

	@Override
	@SuppressWarnings("deprecation")
	public void setChanged() {
		super.setChanged();
		if (this.level != null) {
			BlockPos pos = this.getBlockPos();
			this.level.getLightEngine().checkBlock(pos);
		}
		if (this.level instanceof ServerLevel serverLevel) {
			PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(this.getBlockPos()), new SetMasonJarItemPacket(this.getBlockPos(), this.item.getItem(), this.itemRotation));
		}
	}

	public int getItemRotation() {
		return this.itemRotation;
	}

	public void setItemRotation(int itemRotation) {
		this.itemRotation = itemRotation;
	}

	public static class MasonJarItemStackHandler {
		protected final MasonJarBlockEntity jarEntity;
		private ItemStack stack = ItemStack.EMPTY;

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
			this.stack = itemStack;
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
