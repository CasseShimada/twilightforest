package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.block.entity.RedThreadBlockEntity;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.RedThreadModel;
import twilightforest.client.renderer.RenderStateUtil;
import twilightforest.init.TFBlocks;

public class RedThreadRenderer implements BlockEntityRenderer<RedThreadBlockEntity, RedThreadRenderer.RenderState> {
	private static final Identifier TEXTURE = TwilightForestMod.getModelTexture("red_thread.png");
	private final RedThreadModel redThreadModel;

	public RedThreadRenderer(BlockEntityRendererProvider.Context context) {
		this.redThreadModel = new RedThreadModel(context.bakeLayer(TFModelLayers.RED_THREAD));
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(RedThreadBlockEntity blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		renderState.state = blockEntity.getBlockState();
		renderState.level = blockEntity.getLevel();
		renderState.pos = blockEntity.getBlockPos();
		renderState.glow = Minecraft.getInstance().player != null && Minecraft.getInstance().player.isHolding(TFBlocks.RED_THREAD.asItem());
	}

	@Override
	public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		if (renderState.state == null) return;

		RenderType renderType = renderState.glow ? RenderTypes.entityTranslucentEmissive(TEXTURE) : RenderTypes.entityCutout(TEXTURE);
		int light = renderState.glow ? RenderStateUtil.FULL_BRIGHT : renderState.lightCoords;

		for (Direction face : Direction.values()) {
			if (renderState.state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(face))) {
				poseStack.pushPose();
				Vec3 xyz = getXYZ(face);
				poseStack.translate(xyz.x, xyz.y, xyz.z);
				poseStack.mulPose(Axis.ZP.rotationDegrees(getZPDegrees(face)));
				poseStack.mulPose(Axis.XP.rotationDegrees(getXPDegrees(face)));
				poseStack.pushPose();
				poseStack.translate(0.5D, 0D, 0.5D);

				submitFace(renderState, face, poseStack, nodeCollector, renderType, light);

				poseStack.popPose();
				poseStack.popPose();
			}
		}
	}

	private void submitFace(RenderState renderState, Direction face, PoseStack poseStack, SubmitNodeCollector nodeCollector, RenderType renderType, int light) {
		Level level = renderState.level;
		BlockPos pos = renderState.pos;
		BlockState blockState = renderState.state;

		nodeCollector.submitModelPart(this.redThreadModel.centerPart(), poseStack, renderType, light, OverlayTexture.NO_OVERLAY, null);

		for (Direction direction : Direction.values()) {
			if (direction.getAxis().equals(face.getAxis())) continue;

			boolean connected = blockState.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction));
			if (!connected && level != null && pos != null) {
				BlockState sideState = level.getBlockState(pos.relative(direction));
				connected = sideState.getBlock().equals(TFBlocks.RED_THREAD) && sideState.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(face));

				if (!connected) {
					sideState = level.getBlockState(pos.relative(direction).relative(face));
					boolean threadBlocked = level.getBlockState(pos.relative(direction)).isFaceSturdy(level, pos, direction.getOpposite());
					connected = sideState.is(TFBlocks.RED_THREAD) && !threadBlocked && sideState.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction.getOpposite()));
				}
			}

			if (connected) {
				nodeCollector.submitModelPart(this.redThreadModel.getPart(face, direction), poseStack, renderType, light, OverlayTexture.NO_OVERLAY, null);
			}
		}
	}

	private static Vec3 getXYZ(Direction face) {
		return new Vec3(
			face == Direction.EAST || face == Direction.UP ? 1D : 0D,
			face == Direction.WEST || face == Direction.UP || face == Direction.NORTH ? 1D : 0D,
			face == Direction.SOUTH ? 1D : 0D);
	}

	private static float getZPDegrees(Direction face) {
		return switch (face) {
			case EAST -> 90.0F;
			case UP -> 180.0F;
			case WEST -> 270.0F;
			default -> 0.0F;
		};
	}

	private static float getXPDegrees(Direction face) {
		return switch (face) {
			case NORTH -> 90.0F;
			case SOUTH -> 270.0F;
			default -> 0.0F;
		};
	}

	public static class RenderState extends BlockEntityRenderState {
		public BlockState state;
		public Level level;
		public BlockPos pos;
		public boolean glow;
	}
}
