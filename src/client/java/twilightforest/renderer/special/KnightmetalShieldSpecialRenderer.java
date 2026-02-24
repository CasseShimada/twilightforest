package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.resources.model.MaterialSet;
import org.joml.Vector3fc;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.KnightmetalShieldModel;

import java.util.function.Consumer;

public record KnightmetalShieldSpecialRenderer(MaterialSet materials, KnightmetalShieldModel model) implements NoDataSpecialModelRenderer {

	private static final Material SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, TwilightForestMod.prefix("entity/knightmetal_shield"));

	@Override
	public void submit(ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);

		Identifier atlas = SHIELD_BASE.atlasLocation();
		var sprite = this.materials.get(SHIELD_BASE);
		nodeCollector.submitModelPart(this.model.handle(), poseStack, this.model.renderType(atlas), packedLight, packedOverlay, sprite, false, hasFoil, -1, null, outlineColor);
		nodeCollector.submitModelPart(this.model.plate(), poseStack, this.model.renderType(atlas), packedLight, packedOverlay, sprite, false, hasFoil, -1, null, outlineColor);
		poseStack.popPose();
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		PoseStack poseStack = new PoseStack();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		this.model.root().getExtentsForGui(poseStack, output);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<KnightmetalShieldSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(KnightmetalShieldSpecialRenderer.Unbaked::new);

        @Override
        public MapCodec<KnightmetalShieldSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return new KnightmetalShieldSpecialRenderer(context.materials(), new KnightmetalShieldModel(context.entityModelSet().bakeLayer(TFModelLayers.KNIGHTMETAL_SHIELD)));
        }
    }
}
