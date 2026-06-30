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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.block.BrazierBlock;
import twilightforest.block.entity.BrazierBlockEntity;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.block.BrazierModel;
import twilightforest.client.renderer.RenderStateUtil;
import twilightforest.enums.BrazierLight;

public class BrazierRenderer implements BlockEntityRenderer<BrazierBlockEntity, BrazierRenderer.RenderState> {

	private final BrazierModel model;
	public static final Identifier TEXTURE_OFF = TwilightForestMod.getModelTexture("brazier/brazier.png");
	public static final Identifier TEXTURE_ON = TwilightForestMod.getModelTexture("brazier/brazier_lit.png");
	public static final Identifier TEXTURE_OVERLAY = TwilightForestMod.getModelTexture("brazier/brazier_overlay.png");

	public BrazierRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new BrazierModel(context.bakeLayer(TFModelLayers.BRAZIER));
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(BrazierBlockEntity blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		renderState.light = blockEntity.getBlockState().getValue(BrazierBlock.LIGHT);
	}

	@Override
	public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		BrazierLight lit = renderState.light;

		poseStack.pushPose();
		BlockState fire = Blocks.FIRE.defaultBlockState();
		float y = 0.35F * lit.getFireSize();
		poseStack.translate(0.26F, 1.6F, 0.5F);
		poseStack.scale(0.35F, y, 0.35F);
		poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
		if (lit.isLit() && y > 0.0F) {
			MovingBlockRenderState fireState = new MovingBlockRenderState();
			RenderStateUtil.populateMovingBlockRenderState(fireState, fire, Minecraft.getInstance().level, renderState.blockPos, renderState.blockPos);
			nodeCollector.submitMovingBlock(poseStack, fireState, 0);
		}
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		Identifier loc = lit.isLit() ? TEXTURE_ON : TEXTURE_OFF;
		nodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, this.model.renderType(loc), renderState.lightCoords, 0, -1, null, 0, renderState.breakProgress);
		if (lit.isLit()) {
			nodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, this.model.renderType(TEXTURE_OVERLAY), 0x0F00F0, 0, -1, null, 0, renderState.breakProgress);
		}
		poseStack.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	public static class RenderState extends BlockEntityRenderState {
		public BrazierLight light = BrazierLight.OFF;
	}
}
