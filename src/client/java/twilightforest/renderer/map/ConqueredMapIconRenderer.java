package twilightforest.client.renderer.map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import org.joml.Matrix4f;
import twilightforest.client.renderer.map.TFMagicMapRenderKeys;

public final class ConqueredMapIconRenderer {

	private ConqueredMapIconRenderer() {
	}

	private static boolean isConquered(MapRenderState state, MapRenderState.MapDecorationRenderState decoration) {
		if (decoration.name == null) return false;
		if (!(state instanceof FabricRenderState fabricState)) return false;
		var conquered = fabricState.getData(TFMagicMapRenderKeys.CONQUERED_STRUCTURES);
		return conquered != null && conquered.contains(decoration.name.getString());
	}

	public static void render(MapRenderState.MapDecorationRenderState decoState, PoseStack stack, SubmitNodeCollector submitNodeCollector, MapRenderState state, int light, int index) {
		if (!isConquered(state, decoState)) return;

		TextureAtlas decorationSprites = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.MAP_DECORATIONS);
		TextureAtlasSprite xSprite = decorationSprites.getSprite(MapDecorationTypes.RED_X.value().assetId());
		float depth = -0.095F;

		stack.pushPose();
		stack.translate(0.0F + decoState.x / 2.0F + 64.0F, 0.0F + decoState.y / 2.0F + 64.0F, 0.0F);
		stack.mulPose(Axis.ZP.rotationDegrees((decoState.rot * 360) / 16.0F));
		stack.scale(2.0F, 2.0F, 2.0F);
		stack.translate(-1.0F, -1.0F, -0.005F);
		float f2 = xSprite.getU0();
		float f3 = xSprite.getV0();
		float f4 = xSprite.getU1();
		float f5 = xSprite.getV1();

		submitNodeCollector.order(index).submitCustomGeometry(stack, RenderTypes.text(xSprite.atlasLocation()), (pose, consumer) -> {
			Matrix4f matrix4f = pose.pose();
			consumer.addVertex(matrix4f, -1.0F, 1.0F, depth).setColor(-1).setUv(f2, f3).setLight(light);
			consumer.addVertex(matrix4f, 1.0F, 1.0F, depth).setColor(-1).setUv(f4, f3).setLight(light);
			consumer.addVertex(matrix4f, 1.0F, -1.0F, depth).setColor(-1).setUv(f4, f5).setLight(light);
			consumer.addVertex(matrix4f, -1.0F, -1.0F, depth).setColor(-1).setUv(f2, f5).setLight(light);
		});
		stack.popPose();
	}
}
