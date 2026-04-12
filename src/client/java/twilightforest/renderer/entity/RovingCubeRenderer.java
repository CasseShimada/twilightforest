package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.CubeOfAnnihilationModel;
import twilightforest.entity.RovingCube;

public class RovingCubeRenderer extends EntityRenderer<RovingCube, EntityRenderState> {

	private static final Identifier TEXTURE = TwilightForestMod.getModelTexture("cubeofannihilation.png");
	private final Model<EntityRenderState> model;

	public RovingCubeRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new CubeOfAnnihilationModel(context.bakeLayer(TFModelLayers.CUBE_OF_ANNIHILATION));
	}

	@Override
	public void submit(EntityRenderState state, PoseStack stack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		stack.pushPose();

		stack.scale(2.0F, 2.0F, 2.0F);
		stack.mulPose(Axis.YP.rotationDegrees(Mth.wrapDegrees(state.ageInTicks) * 11.0F));
		stack.translate(0.0F, 0.75F, 0.0F);
		nodeCollector.submitModel(this.model, state, stack, this.model.renderType(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null);

		stack.popPose();
		super.submit(state, stack, nodeCollector, cameraRenderState);
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}
}
