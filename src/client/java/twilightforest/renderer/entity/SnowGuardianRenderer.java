package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import twilightforest.client.model.TFModelLayers;
import twilightforest.entity.monster.SnowGuardian;

public class SnowGuardianRenderer extends HumanoidMobRenderer<SnowGuardian, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");

	public SnowGuardianRenderer(EntityRendererProvider.Context context) {
		super(context, new HumanoidModel<>(context.bakeLayer(TFModelLayers.NOOP)), 0.25F);
		ArmorModelSet<HumanoidModel<HumanoidRenderState>> armorModels = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new);
		this.addLayer(new HumanoidArmorLayer<>(this, armorModels, context.getEquipmentRenderer()));
	}

	@Override
	protected int getModelTint(HumanoidRenderState state) {
		return ARGB.colorFromFloat(0.0F, 0.0F, 0.0F, 0.0F);
	}

	@Override
	protected void scale(HumanoidRenderState state, PoseStack stack) {
		stack.translate(0.0F, Mth.sin(state.ageInTicks * 0.2F) * 0.15F, 0.0F);
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState state) {
		return TEXTURE;
	}
}
