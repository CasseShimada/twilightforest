package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.ProtectionBoxModel;
import twilightforest.client.state.ProtectionBoxRenderState;
import twilightforest.entity.ProtectionBox;
import twilightforest.TwilightForestMod;

public class ProtectionBoxRenderer extends EntityRenderer<ProtectionBox, ProtectionBoxRenderState> {

	private final ProtectionBoxModel boxModel;

	public ProtectionBoxRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.0F;
		this.boxModel = new ProtectionBoxModel(context.bakeLayer(TFModelLayers.PROTECTION_BOX));
	}

	@Override
	public boolean shouldRender(ProtectionBox entity, Frustum frustum, double x, double y, double z) {
		return true;
	}

	@Override
	public void submit(ProtectionBoxRenderState state, PoseStack stack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {

		float alpha = 1.0F;
		if (state.life < 20) alpha = state.life / 20.0F;

		float tick = Minecraft.getInstance().level != null
			? (float) Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)
			: 0.0F;
		nodeCollector.submitModel(
			this.boxModel,
			state,
			stack,
			RenderTypes.energySwirl(TwilightForestMod.getModelTexture("protectionbox.png"), (tick * 0.06F) % 1.0F, (tick * 0.035F) % 1.0F),
			state.lightCoords,
			OverlayTexture.NO_OVERLAY,
			ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F),
			null
		);
		super.submit(state, stack, nodeCollector, cameraRenderState);
	}

	@Override
	public ProtectionBoxRenderState createRenderState() {
		return new ProtectionBoxRenderState();
	}

	@Override
	public void extractRenderState(ProtectionBox entity, ProtectionBoxRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.life = entity.lifeTime;
		state.sizeX = entity.sizeX;
		state.sizeY = entity.sizeY;
		state.sizeZ = entity.sizeZ;
	}
}
