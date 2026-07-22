package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.block.BrazierModel;
import twilightforest.client.renderer.block.BrazierRenderer;

import java.util.function.Consumer;

/** Renders the unlit brazier item using the same model and transform as its block entity. */
public record BrazierSpecialRenderer(BrazierModel model) implements SpecialModelRenderer<Unit> {
	@Override
	public @Nullable Unit extractArgument(ItemStack stack) {
		return Unit.INSTANCE;
	}

	@Override
	public void submit(@Nullable Unit argument, PoseStack poseStack, SubmitNodeCollector nodeCollector, int light, int overlay, boolean foil, int outlineColor) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		nodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, this.model.renderType(BrazierRenderer.TEXTURE_OFF), light, overlay, outlineColor, null);
		poseStack.popPose();
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		output.accept(new Vector3f(0.0F, 0.0F, 0.0F));
		output.accept(new Vector3f(1.0F, 1.5F, 1.0F));
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked<Unit> {
		public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<Unit> bake(SpecialModelRenderer.BakingContext context) {
			return new BrazierSpecialRenderer(new BrazierModel(context.entityModelSet().bakeLayer(TFModelLayers.BRAZIER)));
		}
	}
}
