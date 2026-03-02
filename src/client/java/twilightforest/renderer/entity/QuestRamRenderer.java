package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.QuestRamModel;
import twilightforest.client.state.QuestingRamRenderState;
import twilightforest.entity.passive.QuestRam;

public class QuestRamRenderer extends MobRenderer<QuestRam, QuestingRamRenderState, QuestRamModel> {

	public static final Identifier TEXTURE = TwilightForestMod.getModelTexture("questram.png");
	public static final Identifier LINE_TEXTURE = TwilightForestMod.getModelTexture("questram_lines.png");

	public QuestRamRenderer(EntityRendererProvider.Context context) {
		super(context, new QuestRamModel(context.bakeLayer(TFModelLayers.QUEST_RAM)), 1.0F);
		this.addLayer(new ColoredSegmentsLayer(this));
		this.addLayer(new GlowingLinesLayer(this));
	}

	@Override
	public QuestingRamRenderState createRenderState() {
		return new QuestingRamRenderState();
	}

	@Override
	public void extractRenderState(QuestRam entity, QuestingRamRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.colorFlags = entity.getColorFlags();
	}

	@Override
	public Identifier getTextureLocation(QuestingRamRenderState state) {
		return TEXTURE;
	}

	public class GlowingLinesLayer extends RenderLayer<QuestingRamRenderState, QuestRamModel> {

		public GlowingLinesLayer(RenderLayerParent<QuestingRamRenderState, QuestRamModel> renderer) {
			super(renderer);
		}

		@Override
		public void submit(PoseStack stack, net.minecraft.client.renderer.SubmitNodeCollector nodeCollector, int light, QuestingRamRenderState state, float netHeadYaw, float headPitch) {
			stack.scale(1.025F, 1.025F, 1.025F);
			int overlay = LivingEntityRenderer.getOverlayCoords(state, QuestRamRenderer.this.getWhiteOverlayProgress(state));
			nodeCollector.order(1).submitModel(this.getParentModel(), state, stack, RenderTypes.entityTranslucent(LINE_TEXTURE, false), 0xF000F0, overlay, -1, null);
		}
	}

	public class ColoredSegmentsLayer extends RenderLayer<QuestingRamRenderState, QuestRamModel> {
		public ColoredSegmentsLayer(RenderLayerParent<QuestingRamRenderState, QuestRamModel> renderer) {
			super(renderer);
		}

		@Override
		public void submit(PoseStack stack, net.minecraft.client.renderer.SubmitNodeCollector nodeCollector, int light, QuestingRamRenderState state, float netHeadYaw, float headPitch) {
			int overlay = LivingEntityRenderer.getOverlayCoords(state, QuestRamRenderer.this.getWhiteOverlayProgress(state));
			this.getParentModel().submitColoredSegments(stack, nodeCollector, light, overlay, null);
		}
	}
}
