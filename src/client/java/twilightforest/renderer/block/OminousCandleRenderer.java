package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import twilightforest.block.OminousCandleBlock;
import twilightforest.block.entity.OminousCandleBlockEntity;
import twilightforest.client.renderer.RenderStateUtil;

import java.util.List;

public class OminousCandleRenderer implements BlockEntityRenderer<OminousCandleBlockEntity, OminousCandleRenderer.RenderState> {
	private final Minecraft minecraft;

	public OminousCandleRenderer(BlockEntityRendererProvider.Context context) {
		this.minecraft = Minecraft.getInstance();
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public void extractRenderState(OminousCandleBlockEntity blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		renderState.time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0L;
		renderState.state = blockEntity.getBlockState();
		renderState.candles = renderState.state.getValue(OminousCandleBlock.CANDLES);
		renderState.partialTick = partialTick;

		for (int i = 0; i < renderState.candles && i < renderState.heights.length; i++) {
			double targetHeight = OminousCandleBlock.getCurrentY(renderState.time, partialTick, renderState.blockPos, i + 1);
			double yHeight = exponentialDecay(blockEntity.getVisualHeight(i), targetHeight, 0.05);
			blockEntity.setVisualHeightScalar(yHeight, i);
			renderState.heights[i] = yHeight;
		}
	}

	@Override
	public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.0D, 0.5D);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

		BlockState state = renderState.state;
		BlockState candle = (state.getBlock() instanceof OminousCandleBlock candleBlock ? candleBlock.candle : Blocks.CANDLE)
			.defaultBlockState()
			.setValue(CandleBlock.LIT, true);
		List<Vec2> offsets = OminousCandleBlock.CANDLE_OFFSETS.get(renderState.candles);

		for (int i = 0; i < renderState.candles; i++) {
			poseStack.pushPose();
			double yHeight = i < renderState.heights.length ? renderState.heights[i] : 0.0;
			poseStack.translate(-offsets.get(i).x, yHeight, -offsets.get(i).y);
			MovingBlockRenderState candleState = new MovingBlockRenderState();
			RenderStateUtil.populateMovingBlockRenderState(candleState, candle, this.minecraft.level, renderState.blockPos, renderState.blockPos);
			nodeCollector.submitMovingBlock(poseStack, candleState, 0);
			poseStack.popPose();
		}

		poseStack.popPose();
	}

	private double exponentialDecay(double prevValue, double targetValue, double decayFactor) {
		return targetValue + (prevValue - targetValue) * Math.exp(-decayFactor * this.minecraft.getDeltaTracker().getGameTimeDeltaTicks());
	}

	public static class RenderState extends BlockEntityRenderState {
		public BlockState state = Blocks.AIR.defaultBlockState();
		public long time;
		public float partialTick;
		public int candles;
		public final double[] heights = new double[4];
	}
}
