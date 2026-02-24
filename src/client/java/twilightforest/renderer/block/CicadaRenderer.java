package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.block.entity.CicadaBlockEntity;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.CicadaModel;

public class CicadaRenderer implements BlockEntityRenderer<CicadaBlockEntity, CicadaRenderer.CicadaRenderState> {

	private final CicadaModel baseModel;
	private final CicadaModel wingModel;
	public static final Identifier TEXTURE = TwilightForestMod.getModelTexture("cicada-model.png");

	public CicadaRenderer(BlockEntityRendererProvider.Context context) {
		this.baseModel = new CicadaModel(context.bakeLayer(TFModelLayers.CICADA));
		this.wingModel = new CicadaModel(context.bakeLayer(TFModelLayers.CICADA));
		this.baseModel.setupBasePass();
		this.wingModel.setupWingPass();
	}

	@Override
	public CicadaRenderState createRenderState() {
		return new CicadaRenderState();
	}

	@Override
	public void extractRenderState(CicadaBlockEntity entity, CicadaRenderState state, float partialTick, Vec3 cameraPos, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
		BlockEntityRenderState.extractBase(entity, state, overlay);
		state.yaw = entity.currentYaw;
		state.rotation = entity.randRot;
		state.facing = entity.getBlockState().getValue(DirectionalBlock.FACING);
	}

	@Override
	public void submit(CicadaRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		renderCicada(this.baseModel, this.wingModel, state.yaw, state.rotation, state.facing, stack, collector, state.lightCoords);
	}

	public static void renderCicada(CicadaModel baseModel, CicadaModel wingModel, float yaw, float rotation, Direction facing, PoseStack stack, SubmitNodeCollector collector, int light) {
		stack.pushPose();
		stack.translate(0.5F, 0.5F, 0.5F);
		stack.mulPose(facing.getRotation());
		stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		stack.mulPose(Axis.YP.rotationDegrees(180.0F + rotation));
		stack.mulPose(Axis.YN.rotationDegrees(yaw));

		collector.submitCustomGeometry(stack, RenderTypes.entityCutoutNoCull(TEXTURE), (pose, consumer) -> {
			PoseStack modelStack = new PoseStack();
			modelStack.last().pose().set(pose.pose());
			modelStack.last().normal().set(pose.normal());
			baseModel.root().render(modelStack, consumer, light, OverlayTexture.NO_OVERLAY, -1);
		});

		collector.submitCustomGeometry(stack, RenderTypes.entityTranslucent(TEXTURE), (pose, consumer) -> {
			PoseStack wingStack = new PoseStack();
			wingStack.last().pose().set(pose.pose());
			wingStack.last().normal().set(pose.normal());
			wingModel.root().render(wingStack, consumer, light, OverlayTexture.NO_OVERLAY, -1);
		});
		stack.popPose();
	}

	public static class CicadaRenderState extends BlockEntityRenderState {
		public float yaw;
		public float rotation;
		public Direction facing = Direction.NORTH;
	}
}
