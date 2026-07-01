package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.KeepsakeCasketModel;

public class SkullChestRenderer<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T, SkullChestRenderer.RenderState> {
	public static final Identifier SKULL_CHEST_TEXTURE = TwilightForestMod.getModelTexture("casket/skull_chest.png");
	public static final Direction ITEM_FACING = Direction.SOUTH;

	private final KeepsakeCasketModel model;

	public SkullChestRenderer(BlockEntityRendererProvider.Context context) {
		this(context, TFModelLayers.SKULL_CHEST);
	}

	public SkullChestRenderer(BlockEntityRendererProvider.Context context, ModelLayerLocation layer) {
		this.model = new KeepsakeCasketModel(context.bakeLayer(layer));
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(T blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		BlockState state = blockEntity.getBlockState();
		renderState.texture = this.getTextureLocation(state);
		renderState.facing = state.getValue(HorizontalDirectionalBlock.FACING);
		renderState.openness = blockEntity.getOpenNess(partialTick);
	}

	protected Identifier getTextureLocation(BlockState state) {
		return SKULL_CHEST_TEXTURE;
	}

	@Override
	public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		poseStack.pushPose();
		applyModelTransform(poseStack, renderState.facing);

		float lidRotation = 1.0F - renderState.openness;
		lidRotation = 1.0F - lidRotation * lidRotation * lidRotation;
		this.model.setupAnim(lidRotation);

		nodeCollector.submitModel(
			this.model,
			Unit.INSTANCE,
			poseStack,
			this.model.renderType(renderState.texture),
			renderState.lightCoords,
			OverlayTexture.NO_OVERLAY,
			0,
			renderState.breakProgress
		);
		poseStack.popPose();
	}

	public static void applyModelTransform(PoseStack poseStack, Direction facing) {
		poseStack.translate(0.5F, 0.0F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
		poseStack.scale(1.0F, -1.0F, -1.0F);
	}

	public static class RenderState extends BlockEntityRenderState {
		public Identifier texture = SKULL_CHEST_TEXTURE;
		public Direction facing = ITEM_FACING;
		public float openness;
	}
}
