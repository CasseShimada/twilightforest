package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.client.renderer.RenderStateUtil;
import twilightforest.entity.SlideBlock;

public class SlideBlockRenderer extends EntityRenderer<SlideBlock, FallingBlockRenderState> {

	public SlideBlockRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.0F;
	}

	@Override
	public boolean shouldRender(SlideBlock entity, Frustum frustum, double x, double y, double z) {
		return super.shouldRender(entity, frustum, x, y, z) && entity.getBlockState() != entity.level().getBlockState(entity.blockPosition());
	}

	// [VanillaCopy] FallingBlockRenderer, with spin
	@Override
	public void submit(FallingBlockRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		BlockState blockState = state.movingBlockRenderState.blockState;
		if (blockState.getRenderShape() == RenderShape.MODEL) {
			poseStack.pushPose();
			poseStack.translate(-0.5, 0.0, -0.5);

			// Spin rotated pillars around their center.
			if (blockState.hasProperty(RotatedPillarBlock.AXIS)) {
				Direction.Axis axis = blockState.getValue(RotatedPillarBlock.AXIS);
				float angle = state.ageInTicks * 60.0F;
				poseStack.translate(0.0D, 0.5D, 0.0D);
				if (axis == Direction.Axis.Y) {
					poseStack.mulPose(Axis.YP.rotationDegrees(angle));
				} else if (axis == Direction.Axis.X) {
					poseStack.mulPose(Axis.XP.rotationDegrees(angle));
				} else if (axis == Direction.Axis.Z) {
					poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
				}
				poseStack.translate(0.0D, -0.5D, 0.0D);
			}

			nodeCollector.submitMovingBlock(poseStack, state.movingBlockRenderState, 0);
			poseStack.popPose();
			super.submit(state, poseStack, nodeCollector, cameraRenderState);
		}
	}

	@Override
	public FallingBlockRenderState createRenderState() {
		return new FallingBlockRenderState();
	}

	@Override
	public void extractRenderState(SlideBlock entity, FallingBlockRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		BlockPos blockPos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
		RenderStateUtil.populateMovingBlockRenderState(
			state.movingBlockRenderState,
			entity.getBlockState(),
			entity.level() instanceof net.minecraft.client.multiplayer.ClientLevel level ? level : null,
			blockPos,
			blockPos
		);
	}
}
