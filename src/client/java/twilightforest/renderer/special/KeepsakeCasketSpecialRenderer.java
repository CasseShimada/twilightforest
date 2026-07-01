package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.KeepsakeCasketModel;
import twilightforest.client.renderer.block.KeepsakeCasketRenderer;
import twilightforest.client.renderer.block.SkullChestRenderer;
import twilightforest.init.TFDataComponents;

public record KeepsakeCasketSpecialRenderer(KeepsakeCasketModel model, float openness) implements SpecialModelRenderer<Integer> {

	@Override
	public Integer extractArgument(ItemStack stack) {
		return stack.getOrDefault(TFDataComponents.CASKET_DAMAGE.get(), 0);
	}

	@Override
	public void submit(Integer damage, PoseStack stack, SubmitNodeCollector nodeCollector, int light, int overlay, boolean foil, int outlineColor) {
		stack.pushPose();
		SkullChestRenderer.applyModelTransform(stack, SkullChestRenderer.ITEM_FACING);

		float lidRotation = 1.0F - this.openness;
		lidRotation = 1.0F - lidRotation * lidRotation * lidRotation;
		this.model.setupAnim(lidRotation);

		nodeCollector.submitModel(this.model, Unit.INSTANCE, stack, this.model.renderType(KeepsakeCasketRenderer.getTextureLocation(damage)), light, OverlayTexture.NO_OVERLAY, 0, null);
		stack.popPose();
	}

	@Override
	public void getExtents(java.util.function.Consumer<Vector3fc> output) {
		PoseStack poseStack = new PoseStack();
		SkullChestRenderer.applyModelTransform(poseStack, SkullChestRenderer.ITEM_FACING);

		float lidRotation = 1.0F - this.openness;
		lidRotation = 1.0F - lidRotation * lidRotation * lidRotation;
		this.model.setupAnim(lidRotation);
		this.model.root().getExtentsForGui(poseStack, output);
	}

	public record Unbaked(float openness) implements SpecialModelRenderer.Unbaked<Integer> {
		public static final MapCodec<KeepsakeCasketSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.FLOAT.optionalFieldOf("openness", 0.0F).forGetter(KeepsakeCasketSpecialRenderer.Unbaked::openness))
			.apply(instance, KeepsakeCasketSpecialRenderer.Unbaked::new));

		public Unbaked() {
			this(0.0F);
		}

		@Override
		public MapCodec<KeepsakeCasketSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<Integer> bake(SpecialModelRenderer.BakingContext context) {
			KeepsakeCasketModel model = new KeepsakeCasketModel(context.entityModelSet().bakeLayer(TFModelLayers.KEEPSAKE_CASKET));
			return new KeepsakeCasketSpecialRenderer(model, this.openness);
		}
	}
}
