package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.client.renderer.RenderStateUtil;
import twilightforest.entity.projectile.ThrownBlock;

/**
 * [VanillaCopy] of {@link net.minecraft.client.renderer.entity.FallingBlockRenderer} because of generic type restrictions
 */
public class ThrownBlockRenderer extends EntityRenderer<ThrownBlock, FallingBlockRenderState> {

	public ThrownBlockRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
	}

	@Override
	public boolean shouldRender(ThrownBlock entity, Frustum frustum, double x, double y, double z) {
		return super.shouldRender(entity, frustum, x, y, z) && entity.getBlockState() != entity.level().getBlockState(entity.blockPosition());
	}

	@Override
	public void submit(FallingBlockRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		BlockState blockState = state.movingBlockRenderState.blockState;
		if (blockState.getRenderShape() == RenderShape.MODEL) {
			poseStack.pushPose();
			poseStack.translate(-0.5, 0.0, -0.5);
			nodeCollector.submitMovingBlock(poseStack, state.movingBlockRenderState);
			poseStack.popPose();
			super.submit(state, poseStack, nodeCollector, cameraRenderState);
		}
	}

	@Override
	public FallingBlockRenderState createRenderState() {
		return new FallingBlockRenderState();
	}

	@Override
	public void extractRenderState(ThrownBlock entity, FallingBlockRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		BlockPos blockPos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
		RenderStateUtil.populateMovingBlockRenderState(
			state.movingBlockRenderState,
			entity.getBlockState(),
			entity.level() instanceof net.minecraft.client.multiplayer.ClientLevel level ? level : null,
			blockPos,
			entity.getOwner() != null ? entity.getOwner().blockPosition() : entity.blockPosition()
		);
	}
}
