package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import twilightforest.client.BugModelAnimationHelper;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.FireflyModel;
import twilightforest.client.renderer.block.FireflyRenderer;
import org.joml.Vector3fc;

public record FireflySpecialRenderer(FireflyModel baseModel, FireflyModel glowModel) implements NoDataSpecialModelRenderer {

	@Override
	public void submit(ItemDisplayContext context, PoseStack stack, SubmitNodeCollector nodeCollector, int light, int overlay, boolean foil, int outlineColor) {
		FireflyRenderer.renderFirefly(this.baseModel(), this.glowModel(), BugModelAnimationHelper.currentYaw, BugModelAnimationHelper.glowIntensity, 0.0F, Direction.NORTH, stack, nodeCollector, light);
	}

	@Override
	public void getExtents(java.util.function.Consumer<Vector3fc> output) {
		PoseStack poseStack = new PoseStack();
		this.baseModel.root().getExtentsForGui(poseStack, output);
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked {
		public static final MapCodec<FireflySpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(FireflySpecialRenderer.Unbaked::new);

		@Override
		public MapCodec<FireflySpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
			FireflyModel baseModel = new FireflyModel(context.entityModelSet().bakeLayer(TFModelLayers.FIREFLY));
			FireflyModel glowModel = new FireflyModel(context.entityModelSet().bakeLayer(TFModelLayers.FIREFLY));
			baseModel.setupBasePass();
			glowModel.setupGlowPass();
			return new FireflySpecialRenderer(baseModel, glowModel);
		}
	}
}
