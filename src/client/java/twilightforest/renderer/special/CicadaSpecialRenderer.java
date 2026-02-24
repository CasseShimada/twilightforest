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
import twilightforest.client.model.entity.CicadaModel;
import twilightforest.client.renderer.block.CicadaRenderer;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public record CicadaSpecialRenderer(CicadaModel baseModel, CicadaModel wingModel) implements NoDataSpecialModelRenderer {

	@Override
	public void submit(ItemDisplayContext context, PoseStack stack, SubmitNodeCollector nodeCollector, int light, int overlay, boolean foil, int outlineColor) {
		CicadaRenderer.renderCicada(this.baseModel(), this.wingModel(), BugModelAnimationHelper.currentYaw, 0.0F, Direction.NORTH, stack, nodeCollector, light);
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		PoseStack poseStack = new PoseStack();
		this.baseModel.root().getExtentsForGui(poseStack, output);
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked {
		public static final MapCodec<CicadaSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(CicadaSpecialRenderer.Unbaked::new);

		@Override
		public MapCodec<CicadaSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
			CicadaModel baseModel = new CicadaModel(context.entityModelSet().bakeLayer(TFModelLayers.CICADA));
			CicadaModel wingModel = new CicadaModel(context.entityModelSet().bakeLayer(TFModelLayers.CICADA));
			baseModel.setupBasePass();
			wingModel.setupWingPass();
			return new CicadaSpecialRenderer(baseModel, wingModel);
		}
	}
}
