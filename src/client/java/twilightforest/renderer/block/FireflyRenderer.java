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

	private final FireflyModel baseModel;
	private final FireflyModel glowModel;
	public static final Identifier TEXTURE = TwilightForestMod.getModelTexture("firefly-tiny.png");

	public FireflyRenderer(BlockEntityRendererProvider.Context context) {
		this.baseModel = new FireflyModel(context.bakeLayer(TFModelLayers.FIREFLY));
		this.glowModel = new FireflyModel(context.bakeLayer(TFModelLayers.FIREFLY));
		this.baseModel.setupBasePass();
		this.glowModel.setupGlowPass();
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
		renderFirefly(this.baseModel, this.glowModel, state.yaw, state.glow, state.rotation, state.facing, stack, collector, state.lightCoords);
	}

	public static void renderFirefly(FireflyModel baseModel, FireflyModel glowModel, int yaw, float glow, float rotation, Direction facing, PoseStack stack, SubmitNodeCollector collector, int light) {
		stack.pushPose();
		stack.translate(0.5F, 0.5F, 0.5F);
		stack.mulPose(facing.getRotation());
		stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		stack.mulPose(Axis.YP.rotationDegrees(180.0F + rotation));
		stack.mulPose(Axis.YN.rotationDegrees(yaw));

		collector.submitCustomGeometry(stack, RenderTypes.entityCutout(TEXTURE), (pose, consumer) -> {
			PoseStack baseStack = new PoseStack();
			baseStack.last().pose().set(pose.pose());
			baseStack.last().normal().set(pose.normal());
			baseModel.root().render(baseStack, consumer, light, OverlayTexture.NO_OVERLAY, -1);
		});

		collector.submitCustomGeometry(stack, RenderTypes.entityTranslucentEmissive(TEXTURE), (pose, consumer) -> {
			PoseStack glowStack = new PoseStack();
			glowStack.last().pose().set(pose.pose());
			glowStack.last().normal().set(pose.normal());
			glowModel.renderGlow(glowStack, consumer, OverlayTexture.NO_OVERLAY, glow);
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
