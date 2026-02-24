package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import twilightforest.client.BugModelAnimationHelper;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.MoonwormModel;
import twilightforest.client.renderer.block.MoonwormRenderer;
import org.joml.Vector3fc;

public record MoonwormSpecialRenderer(MoonwormModel model) implements NoDataSpecialModelRenderer {

	@Override
	public void submit(ItemDisplayContext context, PoseStack stack, SubmitNodeCollector nodeCollector, int light, int overlay, boolean foil, int outlineColor) {
		MoonwormRenderer.renderMoonworm(this.model(), BugModelAnimationHelper.currentRotation, 0.0F, (BugModelAnimationHelper.desiredRotation - BugModelAnimationHelper.currentRotation) - Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks(), BugModelAnimationHelper.yawWriggleDelay, Direction.NORTH, stack, nodeCollector, light);
	}

	@Override
	public void getExtents(java.util.function.Consumer<Vector3fc> output) {
		PoseStack poseStack = new PoseStack();
		this.model.root().getExtentsForGui(poseStack, output);
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked {
		public static final MapCodec<MoonwormSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(MoonwormSpecialRenderer.Unbaked::new);

		@Override
		public MapCodec<MoonwormSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
			return new MoonwormSpecialRenderer(new MoonwormModel(context.entityModelSet().bakeLayer(TFModelLayers.MOONWORM)));
		}
	}
}
