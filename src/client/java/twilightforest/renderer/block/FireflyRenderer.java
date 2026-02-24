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
import twilightforest.block.entity.FireflyBlockEntity;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.FireflyModel;

public class FireflyRenderer implements BlockEntityRenderer<FireflyBlockEntity, FireflyRenderer.FireflyRenderState> {

	private final FireflyModel fireflyModel;
	public static final Identifier TEXTURE = TwilightForestMod.getModelTexture("firefly-tiny.png");

	public FireflyRenderer(BlockEntityRendererProvider.Context context) {
		this.fireflyModel = new FireflyModel(context.bakeLayer(TFModelLayers.FIREFLY));
	}

	@Override
	public FireflyRenderState createRenderState() {
		return new FireflyRenderState();
	}

	@Override
	public void extractRenderState(FireflyBlockEntity entity, FireflyRenderState state, float partialTick, Vec3 cameraPos, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
		BlockEntityRenderState.extractBase(entity, state, overlay);
		state.yaw = entity.currentYaw;
		state.glow = entity.glowIntensity;
		state.rotation = entity.randRot;
		state.facing = entity.getBlockState().getValue(DirectionalBlock.FACING);
	}

	@Override
	public void submit(FireflyRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		renderFirefly(this.fireflyModel, state.yaw, state.glow, state.rotation, state.facing, stack, collector, state.lightCoords);
	}

	public static void renderFirefly(FireflyModel model, int yaw, float glow, float rotation, Direction facing, PoseStack stack, SubmitNodeCollector collector, int light) {
		stack.pushPose();
		stack.translate(0.5F, 0.5F, 0.5F);
		stack.mulPose(facing.getRotation());
		stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		stack.mulPose(Axis.YP.rotationDegrees(180.0F + rotation));
		stack.mulPose(Axis.YN.rotationDegrees(yaw));

		model.setupGlow();
		collector.submitModel(model, null, stack, RenderTypes.entityCutoutNoCull(TEXTURE), light, OverlayTexture.NO_OVERLAY, -1, null);

		collector.submitCustomGeometry(stack, RenderTypes.entityTranslucentEmissive(TEXTURE), (pose, consumer) -> {
			PoseStack glowStack = new PoseStack();
			glowStack.last().pose().set(pose.pose());
			glowStack.last().normal().set(pose.normal());
			model.renderGlow(glowStack, consumer, OverlayTexture.NO_OVERLAY, glow);
		});

		stack.popPose();
	}

	public static class FireflyRenderState extends BlockEntityRenderState {
		public int yaw;
		public float glow;
		public float rotation;
		public Direction facing = Direction.NORTH;
	}
}
