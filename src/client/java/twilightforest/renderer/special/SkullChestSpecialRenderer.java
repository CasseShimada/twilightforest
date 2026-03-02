package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.KeepsakeCasketModel;
import twilightforest.client.renderer.block.SkullChestRenderer;

public record SkullChestSpecialRenderer(KeepsakeCasketModel model, float openness) implements NoDataSpecialModelRenderer {

	@Override
	public void submit(ItemDisplayContext context, PoseStack stack, SubmitNodeCollector nodeCollector, int light, int overlay, boolean foil, int outlineColor) {
		stack.pushPose();
		stack.translate(0.5F, 0.0F, 0.5F);
		stack.mulPose(Axis.YP.rotationDegrees(-Direction.NORTH.toYRot()));
		stack.scale(1.0F, -1.0F, -1.0F);

		float lidRotation = 1.0F - this.openness;
		lidRotation = 1.0F - lidRotation * lidRotation * lidRotation;
		this.model.setupAnim(lidRotation);

		nodeCollector.submitModel(this.model, Unit.INSTANCE, stack, this.model.renderType(SkullChestRenderer.SKULL_CHEST_TEXTURE), light, OverlayTexture.NO_OVERLAY, 0, null);
		stack.popPose();
	}

	@Override
	public void getExtents(java.util.function.Consumer<Vector3fc> output) {
		PoseStack poseStack = new PoseStack();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-Direction.NORTH.toYRot()));
		poseStack.scale(1.0F, -1.0F, -1.0F);

		float lidRotation = 1.0F - this.openness;
		lidRotation = 1.0F - lidRotation * lidRotation * lidRotation;
		this.model.setupAnim(lidRotation);
		this.model.root().getExtentsForGui(poseStack, output);
	}

	public record Unbaked(float openness) implements SpecialModelRenderer.Unbaked {
		public static final MapCodec<SkullChestSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.FLOAT.optionalFieldOf("openness", 0.0F).forGetter(SkullChestSpecialRenderer.Unbaked::openness))
			.apply(instance, SkullChestSpecialRenderer.Unbaked::new));

		public Unbaked() {
			this(0.0F);
		}

		@Override
		public MapCodec<SkullChestSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
			KeepsakeCasketModel model = new KeepsakeCasketModel(context.entityModelSet().bakeLayer(TFModelLayers.SKULL_CHEST));
			return new SkullChestSpecialRenderer(model, this.openness());
		}
	}
}
