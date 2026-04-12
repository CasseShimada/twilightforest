package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import twilightforest.entity.projectile.CubeOfAnnihilation;

public class CubeOfAnnihilationRenderer extends EntityRenderer<CubeOfAnnihilation, EntityRenderState> {

	private static final Identifier TEXTURE = TwilightForestMod.getModelTexture("cubeofannihilation.png");
	private final CubeOfAnnihilationModel model;

	public CubeOfAnnihilationRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new CubeOfAnnihilationModel(context.bakeLayer(TFModelLayers.CUBE_OF_ANNIHILATION));
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}

	@Override
	public void submit(EntityRenderState state, PoseStack stack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		stack.pushPose();
		stack.scale(-1.0F, -1.0F, 1.0F);
		stack.mulPose(Axis.YP.rotationDegrees(Mth.wrapDegrees(state.ageInTicks * 11.0F)));
		stack.translate(0.0F, -0.5F, 0.0F);
		nodeCollector.submitModel(this.model, state, stack, this.model.renderType(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null);
		stack.popPose();
		super.submit(state, stack, nodeCollector, cameraRenderState);
	}
}
