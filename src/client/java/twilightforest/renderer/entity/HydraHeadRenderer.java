package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.entity.HydraHeadModel;
import twilightforest.client.state.HydraHeadRenderState;
import twilightforest.entity.boss.Hydra;
import twilightforest.entity.boss.HydraHead;
import twilightforest.entity.boss.HydraHeadContainer;

public class HydraHeadRenderer extends TFPartRenderer<HydraHead, HydraHeadRenderState, HydraHeadModel> {

	private static final Identifier TEXTURE = TwilightForestMod.getModelTexture("hydra4.png");

	public HydraHeadRenderer(EntityRendererProvider.Context context, HydraHeadModel model) {
		super(context, model);
	}

	@Override
	public void submit(HydraHeadRenderState state, PoseStack stack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		stack.pushPose();
		stack.mulPose(Axis.YP.rotationDegrees(-180));
		super.submit(state, stack, nodeCollector, cameraRenderState);
		stack.popPose();
	}

	@Override
	protected @Nullable RenderType getRenderType(HydraHeadRenderState state, boolean visible, boolean ghostly, boolean glowing) {
		// see whether we want to render these
		if (!state.active) return null;
		return super.getRenderType(state, visible, ghostly, glowing);
	}

	@Override
	protected boolean shouldShowName(HydraHead entity, double partialTick) {
		return entity.hasCustomName() && !entity.getCustomName().getString().isEmpty();
	}

	@Override
	protected void submitNameDisplay(HydraHeadRenderState state, PoseStack stack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		Vec3 vec3 = state.nameTagAttachment;
		if (vec3 != null && state.nameTag != null) {
			Vec3 adjusted = vec3.add(0.0D, 0.5D, 0.0D);
			nodeCollector.submitNameTag(stack, adjusted, 0, state.nameTag, !state.isDiscrete, state.lightCoords, cameraRenderState);
		}
	}

	@Override
	public HydraHeadRenderState createRenderState() {
		return new HydraHeadRenderState();
	}

	@Override
	public void extractRenderState(HydraHead entity, HydraHeadRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		var container = getHeadObject(entity);
		state.active = container == null || entity.isActive();
		state.mouthAngle = Mth.lerp(partialTick, entity.getMouthOpenLast(), entity.getMouthOpen());
	}

	@Nullable
	public static HydraHeadContainer getHeadObject(HydraHead entity) {
		Hydra hydra = entity.getParent();

		if (hydra != null) {
			for (int i = 0; i < Hydra.MAX_HEADS; i++) {
				if (hydra.hc[i].headEntity == entity) {
					return hydra.hc[i];
				}
			}
		}
		return null;
	}

	@Override
	public Identifier getTextureLocation(HydraHeadRenderState state) {
		return TEXTURE;
	}
}
