package twilightforest.block.entity.bookshelf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.ChiseledCanopyShelfBlock;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFBlocks;
import twilightforest.util.SaveDebug;

public class ChiseledCanopyShelfBlockEntity extends ChiseledBookShelfBlockEntity implements Spawner {

	private final BookshelfSpawner spawner = new BookshelfSpawner() {
		@Override
		public void broadcastEvent(Level level, BlockPos pos, int id) {
			level.blockEvent(pos, TFBlocks.CHISELED_CANOPY_BOOKSHELF.get(), id, 0);
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
		if (!level.isClientSide() && state.getValue(ChiseledCanopyShelfBlock.SPAWNER)) {
			te.spawner.serverTick((ServerLevel) level, pos, state);
		}
	}

	@Override
	public BlockEntityType<?> getType() {
		return TFBlockEntities.CHISELED_CANOPY_BOOKSHELF;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.spawner.load(this.level, this.worldPosition, input);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		this.spawner.save(output);
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
			if (newState.getBlock() instanceof BaseFireBlock && oldState.getValue(ChiseledCanopyShelfBlock.SPAWNER)) {
				for (int i = 0; i < ChiseledCanopyShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); i++) {
					BooleanProperty property = ChiseledCanopyShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(i);
					if (oldState.hasProperty(property) && oldState.getValue(property)) {
						this.spawner.attemptSpawnTome(i, serverLevel, this.worldPosition, true, null, 5);
					}
				}
			}
		}
		super.setRemoved();
	}

	public BookshelfSpawner getSpawner() {
		return this.spawner;
	}
}
