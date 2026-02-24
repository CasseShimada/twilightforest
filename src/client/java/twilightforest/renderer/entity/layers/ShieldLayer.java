package twilightforest.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import twilightforest.TwilightForestMod;
import twilightforest.entity.boss.Lich;
import twilightforest.init.TFDataAttachments;

public class ShieldLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {

	public static final Identifier LOC = TwilightForestMod.prefix("shield");
	public static final RenderStateDataKey<Integer> SHIELD_COUNT_KEY = RenderStateDataKey.create(() -> TwilightForestMod.prefix("shield_count").toString());

	private final ItemStackRenderState renderState = new ItemStackRenderState();

	public ShieldLayer(RenderLayerParent<S, M> renderer) {
		super(renderer);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S state, float netHeadYaw, float headPitch) {
		if (!(state instanceof FabricRenderState fabricState)) return;
		int count = fabricState.getDataOrDefault(SHIELD_COUNT_KEY, 0);
		if (count > 0) renderShields(poseStack, nodeCollector, state, count, packedLight);
	}

	public static int getShieldCount(LivingEntity entity) {
		return entity instanceof Lich lich
			? (lich.getTeleportInvisibility() > 0 ? 0 : lich.getShieldStrength())
			: TFDataAttachments.get(entity, TFDataAttachments.FORTIFICATION_SHIELDS).shieldsLeft();
	}

	private void renderShields(PoseStack poseStack, SubmitNodeCollector nodeCollector, S state, int count, int packedLight) {
		ItemModel model = Minecraft.getInstance().getModelManager().getItemModel(LOC);
		ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
		this.renderState.clear();
		model.update(this.renderState, ItemStack.EMPTY, resolver, ItemDisplayContext.NONE, Minecraft.getInstance().level, null, 0);

		float age = state.ageInTicks;
		float rotateAngleY = age / -5.0F;
		float rotateAngleX = Mth.sin(age / 5.0F) / 4.0F;
		float rotateAngleZ = Mth.cos(age / 5.0F) / 4.0F;

		for (int c = 0; c < count; c++) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F + rotateAngleZ * (180.0F / Mth.PI)));
			poseStack.mulPose(Axis.YP.rotationDegrees(rotateAngleY * (180.0F / Mth.PI) + (c * (360.0F / count))));
			poseStack.mulPose(Axis.XP.rotationDegrees(rotateAngleX * (180.0F / Mth.PI)));
			poseStack.translate(-0.5F, -0.65F, -0.5F);
			poseStack.translate(0.0F, 0.0F, -0.7F);

			this.renderState.submit(poseStack, nodeCollector, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
			poseStack.popPose();
		}
	}
}
