package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity.WobbleStyle;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.entity.JarBlockEntity;
import twilightforest.block.entity.MasonJarBlockEntity;
import twilightforest.client.renderer.RenderStateUtil;

import java.util.ArrayList;
import java.util.List;

public class JarRenderer<T extends JarBlockEntity, S extends JarRenderer.JarRenderState> implements BlockEntityRenderer<T, S> {
	protected static final float WOBBLE_AMPLITUDE = 0.125F;

	public JarRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public S createRenderState() {
		return (S) new JarRenderState();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public void extractRenderState(T blockEntity, S renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);
		renderState.state = blockEntity.getBlockState();
		renderState.level = blockEntity.getLevel() instanceof ClientLevel level ? level : null;
		renderState.lidItem = blockEntity.lid;
		renderState.lidState = resolveLidState(blockEntity.lid);
		renderState.wobbleRotX = 0.0F;
		renderState.wobbleRotY = 0.0F;
		renderState.wobbleRotZ = 0.0F;
		RenderStateUtil.populateMovingBlockRenderState(renderState.blockRenderState, renderState.state, renderState.level, renderState.blockPos, renderState.blockPos);
		if (renderState.lidState != null) {
			RenderStateUtil.populateMovingBlockRenderState(renderState.lidRenderState, renderState.lidState, renderState.level, renderState.blockPos, renderState.blockPos);
		}

		WobbleStyle wobbleStyle = blockEntity.lastWobbleStyle;
		if (wobbleStyle != null && blockEntity.getLevel() != null) {
			float f = ((float) (blockEntity.getLevel().getGameTime() - blockEntity.wobbleStartedAtTick) + partialTick) / (float) wobbleStyle.duration;
			if (f >= 0.0F && f <= 1.0F) {
				if (wobbleStyle == WobbleStyle.POSITIVE) {
					float f2 = f * (float) (Math.PI * 2);
					float f3 = -1.5F * (Mth.cos(f2) + 0.5F) * Mth.sin(f2 / 2.0F);
					renderState.wobbleRotX = f3 * 0.015625F;
					renderState.wobbleRotZ = Mth.sin(f2) * 0.015625F;
				} else {
					float f5 = Mth.sin(-f * 3.0F * (float) Math.PI) * WOBBLE_AMPLITUDE;
					float f6 = 1.0F - f;
					renderState.wobbleRotY = f5 * f6;
				}
			}
		}
	}

	@Override
	public void submit(S renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		poseStack.pushPose();
		poseStack.translate(0.5, 0.0, 0.5);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.translate(-0.5, 0.0, -0.5);

		if (renderState.wobbleRotX != 0.0F) {
			poseStack.rotateAround(Axis.XP.rotation(renderState.wobbleRotX), 0.5F, 0.0F, 0.5F);
		}
		if (renderState.wobbleRotY != 0.0F) {
			poseStack.rotateAround(Axis.YP.rotation(renderState.wobbleRotY), 0.5F, 0.0F, 0.5F);
		}
		if (renderState.wobbleRotZ != 0.0F) {
			poseStack.rotateAround(Axis.ZP.rotation(renderState.wobbleRotZ), 0.5F, 0.0F, 0.5F);
		}

		nodeCollector.submitMovingBlock(poseStack, renderState.blockRenderState, 0);

		if (renderState.lidState != null && renderState.lidItem != null) {
			poseStack.pushPose();
			BlockStateModel lidModel = JarLidModels.getModel(renderState.lidItem);
			if (lidModel != null) {
				List<BlockStateModelPart> parts = new ArrayList<>();
				lidModel.collectParts(RandomSource.create(0L), parts);
				nodeCollector.submitBlockModel(poseStack, RenderTypes.solidMovingBlock(), parts, BlockModelRenderState.EMPTY_TINTS, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			} else {
				poseStack.translate(0.5D, 0.875D, 0.5D);
				poseStack.scale(0.5F, 0.25F, 0.5F);
				poseStack.translate(-0.5D, -0.5D, -0.5D);
				nodeCollector.submitMovingBlock(poseStack, renderState.lidRenderState, 0);
			}
			poseStack.popPose();
		}

		renderContents(renderState, poseStack, nodeCollector);
		poseStack.popPose();
	}

	protected void renderContents(S renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
	}

	private static @Nullable BlockState resolveLidState(@Nullable Item lid) {
		if (lid == null) {
			return null;
		}
		if (lid instanceof BlockItem blockItem) {
			return blockItem.getBlock().defaultBlockState();
		}
		return Blocks.OAK_LOG.defaultBlockState();
	}

	public static class JarRenderState extends BlockEntityRenderState {
		public BlockState state = Blocks.AIR.defaultBlockState();
		public @Nullable ClientLevel level;
		public final MovingBlockRenderState blockRenderState = new MovingBlockRenderState();
		public final MovingBlockRenderState lidRenderState = new MovingBlockRenderState();
		public Item lidItem;
		public @Nullable BlockState lidState = Blocks.OAK_LOG.defaultBlockState();
		public float wobbleRotX;
		public float wobbleRotY;
		public float wobbleRotZ;
	}

	public static class MasonJarRenderer extends JarRenderer<MasonJarBlockEntity, MasonJarRenderer.MasonJarRenderState> {
		private final ItemModelResolver resolver;

		public MasonJarRenderer(BlockEntityRendererProvider.Context context) {
			super(context);
			this.resolver = context.itemModelResolver();
		}

		@Override
		public MasonJarRenderState createRenderState() {
			return new MasonJarRenderState();
		}

		@Override
		public void extractRenderState(MasonJarBlockEntity blockEntity, MasonJarRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
			super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
			renderState.itemStack = blockEntity.getItemHandler().getItem().copy();
			renderState.itemRotation = blockEntity.getItemRotation();

			this.resolver.updateForTopItem(renderState.itemRenderState, renderState.itemStack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
		}

		@Override
		protected void renderContents(MasonJarRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
			if (renderState.itemStack.isEmpty()) return;

			poseStack.pushPose();
			poseStack.translate(0.5D, 0.4375D, 0.5D);
			poseStack.mulPose(Axis.YN.rotationDegrees(RotationSegment.convertToDegrees(renderState.itemRotation)));
			poseStack.scale(0.5F, 0.5F, 0.5F);

			renderState.itemRenderState.submit(poseStack, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}

		public static final class MasonJarRenderState extends JarRenderState {
			public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
			public ItemStack itemStack = ItemStack.EMPTY;
			public int itemRotation;
		}
	}
}
