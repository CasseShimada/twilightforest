package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.DryingRackBlock;
import twilightforest.block.entity.DryingRackBlockEntity;
import twilightforest.tags.TFItemTags;

public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity, DryingRackRenderer.RenderState> {

	private final ItemModelResolver resolver;

	public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
		this.resolver = context.itemModelResolver();
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(DryingRackBlockEntity blockEntity, RenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		ItemStack stack = blockEntity.getTheItem();
		state.facing = blockEntity.getBlockState().getValue(DryingRackBlock.FACING);
		this.resolver.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
		state.renderOffset = state.item.usesBlockLight() ? 0.5F : stack.is(TFItemTags.RENDER_LOWER_ON_DRYING_RACK) ? 0.325F : 0.45F;

		if (stack.is(ItemTags.BANNERS)) {
			state.renderOffset -= 0.4F;
		}
		if (stack.is(Items.SHIELD)) {
			state.renderOffset -= 0.1F;
		}
	}

	@Override
	public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		Direction direction = state.facing;
		poseStack.pushPose();
		poseStack.translate(0.4F * direction.getStepX() + 0.5F, state.renderOffset, 0.4F * direction.getStepZ() + 0.5F);
		poseStack.scale(0.99F, 0.99F, 0.99F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
		state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}

	public static class RenderState extends BlockEntityRenderState {
		public Direction facing = Direction.NORTH;
		public ItemStackRenderState item = new ItemStackRenderState();
		public float renderOffset;
	}
}
