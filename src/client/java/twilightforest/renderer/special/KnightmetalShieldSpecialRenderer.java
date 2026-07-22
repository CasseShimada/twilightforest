package twilightforest.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.KnightmetalShieldModel;

import java.util.function.Consumer;

public record KnightmetalShieldSpecialRenderer(SpriteGetter sprites, KnightmetalShieldModel model) implements NoDataSpecialModelRenderer {

	// Datagen stitches this legacy entity texture into the shield-pattern atlas under its
	// original sprite id. SHIELD_MAPPER would prepend entity/shield/ and silently resolve
	// to the missing-texture sprite instead.
	private static final SpriteId SHIELD_BASE = new SpriteId(Sheets.SHIELD_SHEET, TwilightForestMod.prefix("entity/knightmetal_shield"));

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);

		Identifier atlas = SHIELD_BASE.atlasLocation();
		TextureAtlasSprite sprite = this.sprites.get(SHIELD_BASE);
		nodeCollector.submitModelPart(this.model.handle(), poseStack, this.model.renderType(atlas), packedLight, packedOverlay, sprite, -1, null, outlineColor);
		nodeCollector.submitModelPart(this.model.plate(), poseStack, this.model.renderType(atlas), packedLight, packedOverlay, sprite, -1, null, outlineColor);
		poseStack.popPose();
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		PoseStack poseStack = new PoseStack();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		this.model.root().getExtentsForGui(poseStack, output);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<KnightmetalShieldSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(KnightmetalShieldSpecialRenderer.Unbaked::new);

        @Override
        public MapCodec<KnightmetalShieldSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new KnightmetalShieldSpecialRenderer(context.sprites(), new KnightmetalShieldModel(context.entityModelSet().bakeLayer(TFModelLayers.KNIGHTMETAL_SHIELD)));
        }
    }
}
