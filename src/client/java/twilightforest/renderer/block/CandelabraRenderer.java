package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import twilightforest.block.CandelabraBlock;
import twilightforest.block.LightableBlock;
import twilightforest.block.entity.CandelabraBlockEntity;
import twilightforest.client.renderer.RenderStateUtil;
import twilightforest.components.item.CandelabraData;

public class CandelabraRenderer<T extends CandelabraBlockEntity> implements BlockEntityRenderer<T, CandelabraRenderer.RenderState> {

	public CandelabraRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(T blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		renderState.state = blockEntity.getBlockState();
		renderState.candles = blockEntity.getCandles();
	}

	@Override
	public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		renderCandles(renderState.state, renderState.candles, poseStack, nodeCollector, renderState.lightCoords);
	}

	public static void renderCandles(BlockState state, CandelabraData data, PoseStack stack, SubmitNodeCollector nodeCollector, int packedLight) {
		renderCandles(state, data, stack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, 0);
	}

	public static void renderCandles(BlockState state, CandelabraData data, PoseStack stack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, int outlineColor) {
		Direction direction = state.getValue(CandelabraBlock.FACING);

		for (int i = 0; i < data.ordered().size(); i++) {
			stack.pushPose();
			float offset = (0.315F - 0.315F * i);
			if (state.getValue(CandelabraBlock.ON_WALL)) {
				stack.translate(-Math.abs(direction.getStepZ()) * offset + (direction.getStepX() * 0.25D), 0.44F, -Math.abs(direction.getStepX()) * offset + (direction.getStepZ() * 0.25D));
			} else {
				stack.translate(-Math.abs(direction.getStepZ()) * offset, 0.44F, -Math.abs(direction.getStepX()) * offset);
			}
			BlockState candle = CandelabraData.getItem(data.ordered(), i).orElse(Blocks.AIR).defaultBlockState();
			if (candle.hasProperty(CandleBlock.LIT))
				candle = candle.setValue(CandleBlock.LIT, state.getValue(CandelabraBlock.LIGHTING) == LightableBlock.Lighting.NORMAL);
			if (!candle.isAir()) {
				MovingBlockRenderState candleState = new MovingBlockRenderState();
				RenderStateUtil.populateMovingBlockRenderState(candleState, candle, Minecraft.getInstance().level, BlockPos.ZERO, BlockPos.ZERO);
				nodeCollector.submitMovingBlock(stack, candleState);
			}
			stack.popPose();
		}
	}

	public static class RenderState extends BlockEntityRenderState {
		public BlockState state = Blocks.AIR.defaultBlockState();
		public CandelabraData candles = CandelabraData.EMPTY;
	}
}
