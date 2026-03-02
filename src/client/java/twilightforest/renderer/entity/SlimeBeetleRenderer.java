package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.SlimeBeetleModel;
import twilightforest.entity.monster.SlimeBeetle;

public class SlimeBeetleRenderer extends MobRenderer<SlimeBeetle, LivingEntityRenderState, SlimeBeetleModel> {

	private static final Identifier TEXTURE = TwilightForestMod.getModelTexture("slimebeetle.png");

	public SlimeBeetleRenderer(EntityRendererProvider.Context context) {
		super(context, new SlimeBeetleModel(context.bakeLayer(TFModelLayers.SLIME_BEETLE)), 0.6F);
		this.addLayer(new OuterTailLayer(this));
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return TEXTURE;
	}

	public static class OuterTailLayer extends RenderLayer<LivingEntityRenderState, SlimeBeetleModel> {
		public OuterTailLayer(RenderLayerParent<LivingEntityRenderState, SlimeBeetleModel> renderer) {
			super(renderer);
		}

		@Override
		public void submit(PoseStack ms, net.minecraft.client.renderer.SubmitNodeCollector nodeCollector, int light, LivingEntityRenderState state, float netHeadYaw, float headPitch) {
			if (!state.isInvisible) {
				this.getParentModel().submitTail(ms, nodeCollector.order(1), light, LivingEntityRenderer.getOverlayCoords(state, 0), RenderTypes.entityTranslucent(TEXTURE, false), state.outlineColor);
			}
		}
	}
}
