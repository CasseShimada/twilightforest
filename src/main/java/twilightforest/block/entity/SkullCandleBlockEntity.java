package twilightforest.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import twilightforest.init.TFBlockEntities;

public class SkullCandleBlockEntity extends SkullBlockEntity {

	private int candleColor;

	private int animationTickCount;
	private boolean isAnimating;

	public SkullCandleBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	public SkullCandleBlockEntity(BlockPos pos, BlockState state, int color) {
		super(pos, state);
		this.candleColor = color;
	}

	@Override
	public boolean isValidBlockState(BlockState state) {
		return this.getType().isValid(state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, SkullCandleBlockEntity entity) {
		if (level.hasNeighborSignal(pos)) {
			entity.isAnimating = true;
			++entity.animationTickCount;
		} else {
			entity.isAnimating = false;
		}

	}

	@Override
	public BlockEntityType<?> getType() {
		return TFBlockEntities.SKULL_CANDLE;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("CandleColor", this.candleColor);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.candleColor = input.getIntOr("CandleColor", 0);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		tag.putInt("CandleColor", this.candleColor);
		return tag;
	}

	public int getCandleColor() {
		return this.candleColor;
	}

	public void setCandleColor(int color) {
		this.candleColor = color;
		this.setChanged();
		this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
	}

	@Override
	public float getAnimation(float partialTick) {
		return this.isAnimating ? (float) this.animationTickCount + partialTick : (float) this.animationTickCount;
	}
}
