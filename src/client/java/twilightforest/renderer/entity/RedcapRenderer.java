package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.RedcapModel;
import twilightforest.entity.monster.Redcap;

public class RedcapRenderer extends HumanoidMobRenderer<Redcap, HumanoidRenderState, RedcapModel> {

	private static final Identifier TEXTURE = TwilightForestMod.getModelTexture("redcap.png");

	public RedcapRenderer(EntityRendererProvider.Context context) {
		super(context, new RedcapModel(context.bakeLayer(TFModelLayers.REDCAP)), 0.4F);
		ArmorModelSet<RedcapModel> armorModels = ArmorModelSet.bake(
			new ArmorModelSet<>(TFModelLayers.REDCAP_ARMOR_OUTER, TFModelLayers.REDCAP_ARMOR_OUTER, TFModelLayers.REDCAP_ARMOR_INNER, TFModelLayers.REDCAP_ARMOR_OUTER),
			context.getModelSet(),
			RedcapModel::new
		);
		this.addLayer(new RedcapArmorLayer<>(this, armorModels, context.getEquipmentRenderer()));
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState state) {
		return TEXTURE;
	}

	public static class RedcapArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends net.minecraft.client.renderer.entity.layers.RenderLayer<S, M> {
		private final ArmorModelSet<A> modelSet;
		private final ArmorModelSet<A> babyModelSet;
		private final EquipmentLayerRenderer equipmentRenderer;

		public RedcapArmorLayer(RenderLayerParent<S, M> renderer, ArmorModelSet<A> modelSet, EquipmentLayerRenderer equipmentRenderer) {
			this(renderer, modelSet, modelSet, equipmentRenderer);
		}

		public RedcapArmorLayer(RenderLayerParent<S, M> renderer, ArmorModelSet<A> modelSet, ArmorModelSet<A> babyModelSet, EquipmentLayerRenderer equipmentRenderer) {
			super(renderer);
			this.modelSet = modelSet;
			this.babyModelSet = babyModelSet;
			this.equipmentRenderer = equipmentRenderer;
		}

		@Override
		public void submit(PoseStack stack, SubmitNodeCollector nodeCollector, int light, S state, float yRot, float xRot) {
			this.renderArmorPiece(stack, nodeCollector, state.chestEquipment, EquipmentSlot.CHEST, light, state);
			this.renderArmorPiece(stack, nodeCollector, state.legsEquipment, EquipmentSlot.LEGS, light, state);
			//TF: raise boots
			stack.pushPose();
			stack.translate(0.0D, -0.2D, 0.0D);
			this.renderArmorPiece(stack, nodeCollector, state.feetEquipment, EquipmentSlot.FEET, light, state);
			stack.popPose();
			this.renderArmorPiece(stack, nodeCollector, state.headEquipment, EquipmentSlot.HEAD, light, state);
		}

		private void renderArmorPiece(PoseStack stack, SubmitNodeCollector nodeCollector, ItemStack itemStack, EquipmentSlot slot, int light, S state) {
			if (!HumanoidArmorLayer.shouldRender(itemStack, slot)) {
				return;
			}

			Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
			if (equippable == null || equippable.assetId().isEmpty()) {
				return;
			}

			A model = this.getArmorModel(state, slot);
			EquipmentClientInfo.LayerType layerType = usesInnerModel(slot)
				? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
				: EquipmentClientInfo.LayerType.HUMANOID;

			this.equipmentRenderer.renderLayers(layerType, equippable.assetId().orElseThrow(), model, state, itemStack, stack, nodeCollector, light, state.outlineColor);
		}

		private A getArmorModel(S state, EquipmentSlot slot) {
			return (state.isBaby ? this.babyModelSet : this.modelSet).get(slot);
		}

		private static boolean usesInnerModel(EquipmentSlot slot) {
			return slot == EquipmentSlot.LEGS;
		}
	}
}
