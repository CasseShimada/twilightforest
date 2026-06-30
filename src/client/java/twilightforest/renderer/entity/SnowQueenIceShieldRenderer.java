package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import twilightforest.client.renderer.RenderStateUtil;
import twilightforest.client.state.PartEntityState;
import twilightforest.entity.boss.SnowQueenIceShield;

public class SnowQueenIceShieldRenderer extends EntityRenderer<SnowQueenIceShield, PartEntityState> {

	public SnowQueenIceShieldRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void submit(PartEntityState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		poseStack.pushPose();
		poseStack.translate(-0.5D, 0.0, -0.5D);
		MovingBlockRenderState blockState = new MovingBlockRenderState();
		BlockPos pos = BlockPos.containing(state.x, state.y, state.z);
		RenderStateUtil.populateMovingBlockRenderState(blockState, Blocks.PACKED_ICE.defaultBlockState(), Minecraft.getInstance().level, pos, pos);
		nodeCollector.submitMovingBlock(poseStack, blockState, 0);
		poseStack.popPose();
		super.submit(state, poseStack, nodeCollector, cameraRenderState);
	}

	@Override
	public PartEntityState createRenderState() {
		return new PartEntityState();
	}

	@Override
	public void extractRenderState(SnowQueenIceShield entity, PartEntityState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.rendererId = entity.renderer();
	}
}
