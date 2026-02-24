package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
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
		nodeCollector.submitBlock(poseStack, Blocks.PACKED_ICE.defaultBlockState(), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
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
