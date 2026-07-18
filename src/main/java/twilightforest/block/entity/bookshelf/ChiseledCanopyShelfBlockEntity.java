package twilightforest.block.entity.bookshelf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.ChiseledCanopyShelfBlock;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFBlocks;
import twilightforest.util.SaveDebug;

public class ChiseledCanopyShelfBlockEntity extends ChiseledBookShelfBlockEntity implements Spawner {
	static final String LEGACY_OVERFLOW_TAG = "legacy_tome_overflow";
	static final String LEGACY_STATE_SYNC_TAG = "legacy_tome_state_sync";
	private int legacyOverflowSpawns;
	private boolean legacyStateSyncPending;

	private final BookshelfSpawner spawner = new BookshelfSpawner() {
		@Override
		public void broadcastEvent(Level level, BlockPos pos, int id) {
			level.blockEvent(pos, TFBlocks.CHISELED_CANOPY_BOOKSHELF, id, 0);
		}

		@Override
		public void setNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData data) {
			super.setNextSpawnData(level, pos, data);
			if (level != null) {
				BlockState blockstate = level.getBlockState(pos);
				level.sendBlockUpdated(pos, blockstate, blockstate, 4);
			}
		}

	};

	public ChiseledCanopyShelfBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	@Override
	public boolean isValidBlockState(BlockState state) {
		return TFBlockEntities.CHISELED_CANOPY_BOOKSHELF.isValid(state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ChiseledCanopyShelfBlockEntity te) {
		if (!level.isClientSide()) {
			BlockState synchronizedState = te.synchronizeLegacyState((ServerLevel) level, state);
			if (synchronizedState.getValue(ChiseledCanopyShelfBlock.SPAWNER)) {
				te.spawner.serverTick((ServerLevel) level, pos, synchronizedState);
			}
		}
	}

	private BlockState synchronizeLegacyState(ServerLevel level, BlockState state) {
		if (!this.legacyStateSyncPending) {
			return state;
		}
		BlockState updated = state;
		boolean hasVisibleBook = false;
		for (int slot = 0; slot < ChiseledCanopyShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); slot++) {
			boolean occupied = !this.getItem(slot).isEmpty();
			hasVisibleBook |= occupied;
			updated = updated.setValue(ChiseledCanopyShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(slot), occupied);
		}
		updated = updated.setValue(ChiseledCanopyShelfBlock.SPAWNER, hasVisibleBook || this.legacyOverflowSpawns > 0);
		this.legacyStateSyncPending = false;
		this.setChanged();
		if (updated != state) {
			level.setBlockAndUpdate(this.worldPosition, updated);
		}
		return updated;
	}

	@Override
	public BlockEntityType<?> getType() {
		return TFBlockEntities.CHISELED_CANOPY_BOOKSHELF;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		var legacy = BookshelfSpawner.readLegacyState(input);
		if (legacy.isPresent()) {
			BookshelfSpawner.LegacyTomeSpawnerState state = legacy.get();
			this.spawner.loadLegacyState(this.level, this.worldPosition, state);
			this.initializeLegacyBooks(state.remainingSpawns());
		} else {
			this.spawner.load(this.level, this.worldPosition, input);
			this.legacyOverflowSpawns = input.getIntOr(LEGACY_OVERFLOW_TAG, 0);
			this.legacyStateSyncPending = input.getBooleanOr(LEGACY_STATE_SYNC_TAG, false);
			if (this.legacyOverflowSpawns < 0 || this.legacyOverflowSpawns > 4) {
				throw new IllegalStateException("Invalid legacy Tome Spawner overflow: " + this.legacyOverflowSpawns);
			}
			if (this.legacyOverflowSpawns > 0 && this.isEmpty()) {
				throw new IllegalStateException("Legacy Tome Spawner overflow has no visible book slot");
			}
		}
	}

	private void initializeLegacyBooks(int remainingSpawns) {
		for (int slot = 0; slot < this.getContainerSize(); slot++) {
			this.getItems().set(slot, slot < legacyVisibleBookCount(remainingSpawns)
				? new ItemStack(Items.BOOK) : ItemStack.EMPTY);
		}
		this.legacyOverflowSpawns = legacyOverflowCount(remainingSpawns);
		this.legacyStateSyncPending = true;
	}

	static int legacyVisibleBookCount(int remainingSpawns) {
		return Math.min(remainingSpawns, 6);
	}

	static int legacyOverflowCount(int remainingSpawns) {
		return Math.max(0, remainingSpawns - 6);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		this.spawner.save(output);
		if (this.legacyOverflowSpawns > 0) {
			output.putInt(LEGACY_OVERFLOW_TAG, this.legacyOverflowSpawns);
		}
		if (this.legacyStateSyncPending) {
			output.putBoolean(LEGACY_STATE_SYNC_TAG, true);
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag compoundtag = this.saveCustomOnly(provider);
		compoundtag.remove("SpawnPotentials");
		return compoundtag;
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		return this.spawner.onEventTriggered(this.level, id) || super.triggerEvent(id, type);
	}

	@Override
	public void setEntityId(EntityType<?> type, RandomSource random) {
		this.spawner.setEntityId(type, this.level, random, this.worldPosition);
		if (this.level != null) {
			this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(ChiseledCanopyShelfBlock.SPAWNER, true));
		}
		this.setChanged();
	}

	@Override
	public void setRemoved() {
		Level level = this.level;
		if (level instanceof ServerLevel serverLevel) {
			if (SaveDebug.isShutdownInProgress() || !level.hasChunkAt(this.worldPosition)) {
				super.setRemoved();
				return;
			}
			BlockState newState = level.getBlockState(this.worldPosition);
			BlockState oldState = this.getBlockState();
			if (newState.getBlock() instanceof BaseFireBlock
				&& (oldState.getValue(ChiseledCanopyShelfBlock.SPAWNER) || this.legacyStateSyncPending)) {
				int firstOccupiedSlot = -1;
				for (int slot = 0; slot < ChiseledCanopyShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); slot++) {
					if (!this.getItem(slot).isEmpty()) {
						firstOccupiedSlot = slot;
						break;
					}
				}
				int overflowAttempts = this.legacyOverflowSpawns;
				var facing = oldState.getValue(HorizontalDirectionalBlock.FACING);
				for (int attempt = 0; attempt < overflowAttempts && firstOccupiedSlot >= 0; attempt++) {
					this.spawner.attemptSpawnTome(firstOccupiedSlot, serverLevel, this.worldPosition,
						facing, true, null, 5);
				}
				for (int i = 0; i < ChiseledCanopyShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); i++) {
					if (!this.getItem(i).isEmpty()) {
						this.spawner.attemptSpawnTome(i, serverLevel, this.worldPosition,
							facing, true, null, 5);
					}
				}
			}
		}
		super.setRemoved();
	}

	public BookshelfSpawner getSpawner() {
		return this.spawner;
	}

	public void consumeSpawnerBook(int slot) {
		if (this.legacyOverflowSpawns > 0) {
			this.legacyOverflowSpawns--;
			this.setChanged();
			return;
		}
		this.setItem(slot, ItemStack.EMPTY);
	}

	int getLegacyOverflowSpawns() {
		return this.legacyOverflowSpawns;
	}
}
