package twilightforest.client.renderer.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.component.ResolvableProfile;
import twilightforest.client.model.entity.GiantModel;
import twilightforest.client.state.GiantRenderState;
import twilightforest.config.TFConfig;
import twilightforest.entity.monster.GiantMiner;

public class TFGiantRenderer<T extends GiantMiner> extends HumanoidMobRenderer<T, GiantRenderState, GiantModel> {
	private final GiantModel normalModel;
	private final GiantModel slimModel;

	public TFGiantRenderer(EntityRendererProvider.Context context) {
		super(context, new GiantModel(context.bakeLayer(ModelLayers.PLAYER), false), 1.8F);
		this.normalModel = this.getModel();
		this.slimModel = new GiantModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

		this.layers.removeIf(layer -> layer instanceof ItemInHandLayer<GiantRenderState, GiantModel>);
		this.addLayer(new GiantItemInHandLayer<>(this));
		@SuppressWarnings("unchecked")
		ArmorModelSet<HumanoidModel<GiantRenderState>> armorModels = (ArmorModelSet<HumanoidModel<GiantRenderState>>)(ArmorModelSet<?>)
			ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new);
		this.addLayer(new HumanoidArmorLayer<>(this, armorModels, context.getEquipmentRenderer()));
	}

	@Override
	public Identifier getTextureLocation(GiantRenderState state) {
		Minecraft mc = Minecraft.getInstance();
		Identifier texture = DefaultPlayerSkin.getDefaultTexture();
		this.model = this.normalModel;

		GameProfile profile = TFConfig.GAME_PROFILES.isEmpty() ? null : TFConfig.GAME_PROFILES.get(Math.abs((int) state.uuid.getMostSignificantBits()) % TFConfig.GAME_PROFILES.size());

		if (profile != null) {
			PlayerSkinRenderCache.RenderInfo info = mc.playerSkinRenderCache().getOrDefault(ResolvableProfile.createResolved(profile));
			PlayerSkin skin = info.playerSkin();
			texture = skin.body().texturePath();
			if (skin.model() == PlayerModelType.SLIM) this.model = this.slimModel;
		} else if (mc.getCameraEntity() instanceof AbstractClientPlayer client) {
			PlayerSkin skin = client.getSkin();
			texture = skin.body().texturePath();
			if (skin.model() == PlayerModelType.SLIM) this.model = this.slimModel;
		}

		if (this.model == this.slimModel) this.slimModel.setupAnim(state);

		return texture;
	}

	@Override
	public void scale(GiantRenderState state, PoseStack stack) {
		stack.scale(4.0F, 4.0F, 4.0F);
	}

	@Override
	public GiantRenderState createRenderState() {
		return new GiantRenderState();
	}

	@Override
	public void extractRenderState(T entity, GiantRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.uuid = entity.getUUID();
		if (Minecraft.getInstance().player != null) {
			state.showHat = Minecraft.getInstance().player.isModelPartShown(PlayerModelPart.HAT);
			state.showJacket = Minecraft.getInstance().player.isModelPartShown(PlayerModelPart.JACKET);
			state.showLeftPants = Minecraft.getInstance().player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
			state.showRightPants = Minecraft.getInstance().player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
			state.showLeftSleeve = Minecraft.getInstance().player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
			state.showRightSleeve = Minecraft.getInstance().player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
		}
	}

	public static class GiantItemInHandLayer<S extends HumanoidRenderState, M extends EntityModel<S> & ArmedModel<S>> extends ItemInHandLayer<S, M> {

		public GiantItemInHandLayer(RenderLayerParent<S, M> renderer) {
			super(renderer);
		}

		@Override
		protected void submitArmWithItem(S renderState, ItemStackRenderState itemStackRenderState, net.minecraft.world.item.ItemStack itemStack, HumanoidArm arm, PoseStack stack, SubmitNodeCollector nodeCollector, int light) {
			if (itemStackRenderState.isEmpty()) {
				return;
			}

			stack.pushPose();
			this.getParentModel().translateToHand(renderState, arm, stack);
			stack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			stack.mulPose(Axis.YP.rotationDegrees(180.0F));
			boolean flag = arm == HumanoidArm.LEFT;
			// TF - move item a bit to actually fit in the giant's hand (y and z changes)
			stack.translate((float) (flag ? -1 : 1) / 16.0F, 0.0D, -0.5D);
			// TF - scale items down to accurately match the actual size it would be in a giant's hand
			stack.scale(0.25F, 0.25F, 0.25F);

			if (renderState.attackTime > 0.0F && renderState.mainArm == arm && renderState.swingAnimationType == SwingAnimationType.STAB) {
				net.minecraft.client.model.effects.SpearAnimations.thirdPersonAttackItem(renderState, stack);
			}

			float useTicks = renderState.ticksUsingItem(arm);
			if (useTicks != 0.0F) {
				HumanoidModel.ArmPose pose = arm == HumanoidArm.RIGHT ? renderState.rightArmPose : renderState.leftArmPose;
				pose.animateUseItem(renderState, stack, useTicks, arm, itemStack);
			}

			itemStackRenderState.submit(stack, nodeCollector, light, OverlayTexture.NO_OVERLAY, renderState.outlineColor);
			stack.popPose();
		}
	}
}
