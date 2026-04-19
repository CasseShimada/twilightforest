package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.block.entity.MoonwormBlockEntity;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.MoonwormModel;

public class MoonwormRenderer implements BlockEntityRenderer<MoonwormBlockEntity, MoonwormRenderer.MoonwormRenderState> {

	private static final Identifier TEXTURE = TwilightForestMod.getModelTexture("moonworm.png");
	private final MoonwormModel moonwormModel;

	public MoonwormRenderer(BlockEntityRendererProvider.Context context) {
		this.moonwormModel = new MoonwormModel(context.bakeLayer(TFModelLayers.MOONWORM));
	}

	@Override
	public MoonwormRenderState createRenderState() {
		return new MoonwormRenderState();
	}

	@Override
	public void extractRenderState(MoonwormBlockEntity entity, MoonwormRenderState state, float partialTick, Vec3 cameraPos, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
		BlockEntityRenderState.extractBase(entity, state, overlay);
		state.yaw = entity.currentYaw;
		state.rotation = entity.randRot;
		state.wiggleRotation = (entity.desiredYaw - entity.currentYaw) - partialTick;
		state.delay = entity.yawDelay;
		state.facing = entity.getBlockState().getValue(DirectionalBlock.FACING);
	}

	@Override
	public void submit(MoonwormRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		renderMoonworm(this.moonwormModel, state.yaw, state.rotation, state.wiggleRotation, state.delay, state.facing, stack, collector, state.lightCoords);
	}

	public static void renderMoonworm(MoonwormModel model, float yaw, float rotation, float wiggleRotation, int delay, Direction facing, PoseStack stack, SubmitNodeCollector collector, int light) {
		stack.pushPose();
		stack.translate(0.5F, 0.5F, 0.5F);
		stack.mulPose(facing.getRotation());
		stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		stack.mulPose(Axis.YP.rotationDegrees(180.0F + rotation));
		stack.mulPose(Axis.YN.rotationDegrees(yaw));

		model.setupAnim(wiggleRotation, delay);
		collector.submitCustomGeometry(stack, model.renderType(TEXTURE), (pose, consumer) -> {
			PoseStack modelStack = new PoseStack();
			modelStack.last().pose().set(pose.pose());
			modelStack.last().normal().set(pose.normal());
			model.root().render(modelStack, consumer, light, OverlayTexture.NO_OVERLAY, -1);
		});

		stack.popPose();
	}

	public static class MoonwormRenderState extends BlockEntityRenderState {
		public float yaw;
		public float rotation;
		public float wiggleRotation;
		public int delay;
		public Direction facing = Direction.NORTH;
	}
}
