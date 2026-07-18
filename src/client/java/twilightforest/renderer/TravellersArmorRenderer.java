package twilightforest.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.armor.TravellersWingsModel;
import twilightforest.config.TFConfig;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFEquipmentAssets;

public final class TravellersArmorRenderer implements ArmorRenderer {
	public static final TravellersArmorRenderer INSTANCE = new TravellersArmorRenderer();
	public static final RenderStateDataKey<Boolean> GOGGLES_DOWN_KEY = RenderStateDataKey.create(
		() -> TwilightForestMod.prefix("travellers_goggles_down").toString());
	private static final Identifier FIRST_PERSON_TEXTURE = TwilightForestMod.prefix("textures/models/armor/travellers_layer_1.png");

	private HumanoidModel<HumanoidRenderState> helmetModel;
	private HumanoidModel<HumanoidRenderState> chestModel;
	private HumanoidModel<HumanoidRenderState> slimChestModel;
	private TravellersWingsModel leggingsModel;
	private HumanoidModel<HumanoidRenderState> bootsModel;
	private EquipmentLayerRenderer equipmentRenderer;

	private TravellersArmorRenderer() {
	}

	public static void extractRenderState(LivingEntity entity, HumanoidRenderState state, FabricRenderState fabricState) {
		fabricState.setData(GOGGLES_DOWN_KEY, TFDataAttachments.get(entity, TFDataAttachments.IS_USING_GOGGLES_ZOOM_MODIFIER));
		if (entity.getItemBySlot(EquipmentSlot.LEGS).has(TFDataComponents.TRAVELLERS_HAS_WINGS)) {
			TravellersWingsModel.extractAnimationState(entity, state, fabricState);
		}
	}

	@Override
	public void render(PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState state,
		EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		if (equippable == null || equippable.slot() != slot || equippable.assetId().isEmpty()) {
			return;
		}

		HumanoidModel<HumanoidRenderState> model = this.modelFor(slot, state);
		if (model == null) {
			return;
		}
		model.resetPose();
		model.allParts().forEach(part -> {
			part.visible = true;
			part.skipDraw = false;
		});
		model.setupAnim(state);
		this.configureParts(model, stack, slot);

		EquipmentClientInfo.LayerType layerType = slot == EquipmentSlot.LEGS
			? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
			: EquipmentClientInfo.LayerType.HUMANOID;
		boolean gogglesDown = slot == EquipmentSlot.HEAD
			&& ((FabricRenderState) state).getDataOrDefault(GOGGLES_DOWN_KEY, false);
		this.equipmentRenderer().renderLayers(
			layerType,
			gogglesDown ? TFEquipmentAssets.TRAVELLERS_DOWN : equippable.assetId().orElseThrow(),
			model,
			state,
			stack,
			matrices,
			collector,
			light,
			state.outlineColor);
	}

	private HumanoidModel<HumanoidRenderState> modelFor(EquipmentSlot slot, HumanoidRenderState state) {
		return switch (slot) {
			case HEAD -> this.helmetModel();
			case CHEST -> isSlim(state) ? this.slimChestModel() : this.chestModel();
			case LEGS -> this.leggingsModel();
			case FEET -> this.bootsModel();
			default -> null;
		};
	}

	private void configureParts(HumanoidModel<HumanoidRenderState> model, ItemStack stack, EquipmentSlot slot) {
		model.head.visible = false;
		model.hat.visible = false;
		model.body.visible = false;
		model.rightArm.visible = false;
		model.leftArm.visible = false;
		model.rightLeg.visible = false;
		model.leftLeg.visible = false;
		switch (slot) {
			case HEAD -> model.head.visible = true;
			case CHEST -> {
				model.body.visible = stack.has(TFDataComponents.TRAVELLERS_HAS_CHESTPLATE);
				boolean hasGloves = stack.has(TFDataComponents.TRAVELLERS_HAS_GLOVES);
				model.rightArm.visible = hasGloves;
				model.leftArm.visible = hasGloves;
			}
			case LEGS -> {
				model.body.visible = true;
				model.body.skipDraw = true;
				TravellersWingsModel wingsModel = (TravellersWingsModel) model;
				wingsModel.setWingsVisible(stack.has(TFDataComponents.TRAVELLERS_HAS_WINGS));
				wingsModel.setBeltVisible(stack.has(TFDataComponents.TRAVELLERS_HAS_BELT)
					|| stack.has(TFDataComponents.SWAP_HOTBAR_MODIFIER));
			}
			case FEET -> {
				model.rightLeg.visible = true;
				model.leftLeg.visible = true;
			}
		}
	}

	public void renderFirstPersonGlove(PoseStack matrices, SubmitNodeCollector collector, int light, HumanoidArm arm) {
		Minecraft minecraft = Minecraft.getInstance();
		AbstractClientPlayer player = minecraft.player;
		if (!TFConfig.firstPersonGloveOverlay || player == null) {
			return;
		}
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!chest.has(TFDataComponents.TRAVELLERS_HAS_GLOVES) || chest.has(TFDataComponents.EMPERORS_CLOTH)) {
			return;
		}

		HumanoidModel<HumanoidRenderState> model = player.getSkin().model() == PlayerModelType.SLIM
			? this.slimChestModel()
			: this.chestModel();
		model.resetPose();
		ModelPart armPart = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
		armPart.visible = true;
		armPart.skipDraw = false;
		armPart.xRot = 0.0F;
		armPart.yRot = 0.0F;
		armPart.zRot = arm == HumanoidArm.RIGHT ? 0.1F : -0.1F;
		collector.submitModelPart(
			armPart,
			matrices,
			RenderTypes.armorCutoutNoCull(FIRST_PERSON_TEXTURE),
			light,
			OverlayTexture.NO_OVERLAY,
			null);
	}

	public void resetModelCache() {
		this.helmetModel = null;
		this.chestModel = null;
		this.slimChestModel = null;
		this.leggingsModel = null;
		this.bootsModel = null;
		this.equipmentRenderer = null;
	}

	private HumanoidModel<HumanoidRenderState> helmetModel() {
		if (this.helmetModel == null) {
			this.helmetModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(TFModelLayers.TRAVELLERS_ARMOR_HELMET));
		}
		return this.helmetModel;
	}

	private HumanoidModel<HumanoidRenderState> chestModel() {
		if (this.chestModel == null) {
			this.chestModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(TFModelLayers.TRAVELLERS_ARMOR_CHEST_GLOVES));
		}
		return this.chestModel;
	}

	private HumanoidModel<HumanoidRenderState> slimChestModel() {
		if (this.slimChestModel == null) {
			this.slimChestModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(TFModelLayers.TRAVELLERS_ARMOR_CHEST_GLOVES_SLIM));
		}
		return this.slimChestModel;
	}

	private TravellersWingsModel leggingsModel() {
		if (this.leggingsModel == null) {
			this.leggingsModel = new TravellersWingsModel(Minecraft.getInstance().getEntityModels().bakeLayer(TFModelLayers.TRAVELLERS_ARMOR_LEGGINGS));
		}
		return this.leggingsModel;
	}

	private HumanoidModel<HumanoidRenderState> bootsModel() {
		if (this.bootsModel == null) {
			this.bootsModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(TFModelLayers.TRAVELLERS_ARMOR_BOOTS));
		}
		return this.bootsModel;
	}

	private EquipmentLayerRenderer equipmentRenderer() {
		if (this.equipmentRenderer == null) {
			Minecraft minecraft = Minecraft.getInstance();
			this.equipmentRenderer = new EquipmentLayerRenderer(
				minecraft.getEntityRenderDispatcher().equipmentAssets,
				minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.ARMOR_TRIMS));
		}
		return this.equipmentRenderer;
	}

	private static boolean isSlim(HumanoidRenderState state) {
		return state instanceof AvatarRenderState avatarState
			&& avatarState.skin != null
			&& avatarState.skin.model() == PlayerModelType.SLIM;
	}
}
