package twilightforest.client.renderer.map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;
import twilightforest.client.renderer.map.TFMagicMapRenderKeys;

public final class MagicMapPlayerIconRenderer {

	private MagicMapPlayerIconRenderer() {
	}

	// [VanillaCopy] of MapRenderer.render, but with a set depth offset instead of relying on index.
	public static void render(MapRenderState.MapDecorationRenderState decoState, PoseStack stack, SubmitNodeCollector submitNodeCollector, MapRenderState state, int packedLight, int index) {
		if (!(state instanceof FabricRenderState fabricState)) return;
		if (!fabricState.getDataOrDefault(TFMagicMapRenderKeys.MAGIC_MAP, false)) return;

		stack.pushPose();
		stack.translate(0.0F + (float) decoState.x / 2.0F + 64.0F, 0.0F + (float) decoState.y / 2.0F + 64.0F, -0.02F);
		stack.mulPose(Axis.ZP.rotationDegrees((float) (decoState.rot * 360) / 16.0F));
		stack.scale(4.0F, 4.0F, 3.0F);
		stack.translate(-0.125F, 0.125F, 0.0F);

		TextureAtlasSprite sprite = decoState.atlasSprite;
		float f2 = sprite.getU0();
		float f3 = sprite.getV0();
		float f4 = sprite.getU1();
		float f5 = sprite.getV1();

		submitNodeCollector.order(index).submitCustomGeometry(stack, RenderTypes.text(sprite.atlasLocation()), (pose, consumer) -> {
			Matrix4f matrix4f1 = pose.pose();
			consumer.addVertex(matrix4f1, -1.0F, 1.0F, -0.3F).setColor(-1).setUv(f2, f3).setLight(packedLight);
			consumer.addVertex(matrix4f1, 1.0F, 1.0F, -0.3F).setColor(-1).setUv(f4, f3).setLight(packedLight);
			consumer.addVertex(matrix4f1, 1.0F, -1.0F, -0.3F).setColor(-1).setUv(f4, f5).setLight(packedLight);
			consumer.addVertex(matrix4f1, -1.0F, -1.0F, -0.3F).setColor(-1).setUv(f2, f5).setLight(packedLight);
		});
		stack.popPose();
	}
}
