package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.entity.projectile.FallingIce;

/**
 * [VanillaCopy] {@link net.minecraft.client.renderer.entity.FallingBlockRenderer}
 */
public class FallingIceRenderer extends EntityRenderer<FallingIce, FallingBlockRenderState> {

	public FallingIceRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
	}

	@Override
	public boolean shouldRender(FallingIce entity, Frustum frustum, double x, double y, double z) {
		return super.shouldRender(entity, frustum, x, y, z) && entity.getBlockState() != entity.level().getBlockState(entity.blockPosition());
	}

	@Override
	public void submit(FallingBlockRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		BlockState blockState = state.movingBlockRenderState.blockState;
		if (blockState.getRenderShape() == RenderShape.MODEL) {
			poseStack.pushPose();
			poseStack.translate(-0.5D, 0.0, -0.5D);
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
	public void extractRenderState(FallingIce entity, FallingBlockRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		BlockPos blockPos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
		state.movingBlockRenderState.randomSeedPos = entity.getStartPos();
		state.movingBlockRenderState.blockPos = blockPos;
		state.movingBlockRenderState.blockState = entity.getBlockState();
		state.movingBlockRenderState.biome = entity.level().getBiome(blockPos);
		state.movingBlockRenderState.level = entity.level();
	}
}
