package twilightforest.block.entity;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import twilightforest.block.AbstractSkullCandleBlock;
import twilightforest.components.item.SkullCandles;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFDataComponents;

import java.util.Optional;

public class SkullCandleBlockEntity extends SkullBlockEntity {

	private SkullCandles candleInfo = SkullCandles.DEFAULT;
	private boolean needsStateCountMigration;

	private int animationTickCount;
	private boolean isAnimating;

	public SkullCandleBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	public SkullCandleBlockEntity(BlockPos pos, BlockState state, int color) {
		super(pos, state);
		this.candleInfo = new SkullCandles(color, candleCount(state));
	}

	@Override
	public boolean isValidBlockState(BlockState state) {
		return this.getType().isValid(state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, SkullCandleBlockEntity entity) {
		if (entity.needsStateCountMigration && state.hasProperty(AbstractSkullCandleBlock.CANDLES)) {
			int storedCount = entity.candleInfo.count();
			if (state.getValue(AbstractSkullCandleBlock.CANDLES) == storedCount) {
				entity.needsStateCountMigration = false;
			} else if (!level.isClientSide()) {
				entity.needsStateCountMigration = false;
				level.setBlock(pos, state.setValue(AbstractSkullCandleBlock.CANDLES, storedCount), Block.UPDATE_ALL);
			}
		}

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
		SkullCandles info = this.currentCandleInfo();
		output.store("info", SkullCandles.CODEC, info);
		output.putInt("CandleColor", info.color());
		output.putInt("CandleAmount", info.count());
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		int stateCount = candleCount(this.getBlockState());
		Optional<SkullCandles> current = input.read("info", SkullCandles.CODEC);
		Optional<Integer> legacyAmount = input.read("CandleAmount", Codec.INT);
		this.candleInfo = current.orElseGet(() -> legacyInfo(input, legacyAmount.orElse(stateCount)));
		this.needsStateCountMigration = (current.isPresent() || legacyAmount.isPresent())
			&& this.candleInfo.count() != stateCount;
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		SkullCandles info = components.getOrDefault(TFDataComponents.SKULL_CANDLES, SkullCandles.DEFAULT);
		this.candleInfo = new SkullCandles(info.color(), sanitizeCount(info.count()));
		this.needsStateCountMigration = this.candleInfo.count() != candleCount(this.getBlockState());
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		components.set(TFDataComponents.SKULL_CANDLES, this.currentCandleInfo());
	}

	@Override
	public void removeComponentsFromTag(ValueOutput output) {
		super.removeComponentsFromTag(output);
		output.discard("info");
		output.discard("CandleColor");
		output.discard("CandleAmount");
	}

	public int getCandleColor() {
		return this.candleInfo.color();
	}

	public void setCandleColor(int color) {
		this.setCandleInfo(new SkullCandles(color, this.currentCandleInfo().count()));
	}

	public void setCandleInfo(SkullCandles info) {
		this.candleInfo = new SkullCandles(info.color(), sanitizeCount(info.count()));
		this.needsStateCountMigration = this.candleInfo.count() != candleCount(this.getBlockState());
		this.setChanged();
		if (this.getLevel() != null) {
			this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
		}
	}

	static SkullCandles readCandleInfo(ValueInput input, int fallbackCount) {
		return input.read("info", SkullCandles.CODEC)
			.orElseGet(() -> legacyInfo(input, input.read("CandleAmount", Codec.INT).orElse(fallbackCount)));
	}

	private static SkullCandles legacyInfo(ValueInput input, int count) {
		return new SkullCandles(input.getIntOr("CandleColor", 0), sanitizeCount(count));
	}

	private SkullCandles currentCandleInfo() {
		return this.needsStateCountMigration
			? this.candleInfo
			: new SkullCandles(this.candleInfo.color(), candleCount(this.getBlockState()));
	}

	private static int candleCount(BlockState state) {
		return state.hasProperty(AbstractSkullCandleBlock.CANDLES)
			? state.getValue(AbstractSkullCandleBlock.CANDLES)
			: SkullCandles.DEFAULT.count();
	}

	private static int sanitizeCount(int count) {
		return Math.max(1, Math.min(4, count));
	}

	@Override
	public float getAnimation(float partialTick) {
		return this.isAnimating ? (float) this.animationTickCount + partialTick : (float) this.animationTickCount;
	}
}
