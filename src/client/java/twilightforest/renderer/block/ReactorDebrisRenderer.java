package twilightforest.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import twilightforest.block.entity.ReactorDebrisBlockEntity;

import java.util.HashMap;
import java.util.Map;

public class ReactorDebrisRenderer implements BlockEntityRenderer<ReactorDebrisBlockEntity, ReactorDebrisRenderer.RenderState> {

	public ReactorDebrisRenderer(BlockEntityRendererProvider.Context context) {}

	private static final Map<Identifier, TextureAtlasSprite> spriteCache = new HashMap<>();

	private enum Axis {
		X, Y, Z
	}

	private record QuadRenderInfo(VertexConsumer builder, Matrix4f matrix, int light, int overlay) {
		private void vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
			this.builder.addVertex(this.matrix, x, y, z)
				.setUv(u, v).setLight(this.light)
				.setOverlay(this.overlay)
				.setNormal(nx, ny, nz)
				.setColor(1f, 1f, 1f, 1f);
		}
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void extractRenderState(ReactorDebrisBlockEntity blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		renderState.textures = blockEntity.textures.clone();
		renderState.minX = blockEntity.minPos.x;
		renderState.minY = blockEntity.minPos.y;
		renderState.minZ = blockEntity.minPos.z;
		renderState.maxX = blockEntity.maxPos.x;
		renderState.maxY = blockEntity.maxPos.y;
		renderState.maxZ = blockEntity.maxPos.z;
	}

	@Override
	public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		nodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentCullItemTarget(TextureAtlas.LOCATION_BLOCKS), (pose, consumer) -> {
			QuadRenderInfo info = new QuadRenderInfo(consumer, pose.pose(), renderState.lightCoords, 0);
			renderBlock(info, renderState);
		});
	}

	private void renderBlock(QuadRenderInfo info, RenderState entity) {
		float minX = entity.minX;
		float minY = entity.minY;
		float minZ = entity.minZ;
		float maxX = entity.maxX;
		float maxY = entity.maxY;
		float maxZ = entity.maxZ;

		renderSide(info, getSprite(entity.textures[0]), Axis.X, minX, minY, minZ, maxY, maxZ, -1);
		renderSide(info, getSprite(entity.textures[1]), Axis.X, maxX, minY, maxZ, maxY, minZ, 1);
		renderSide(info, getSprite(entity.textures[2]), Axis.Y, minY, minX, maxZ, maxX, minZ, -1);
		renderSide(info, getSprite(entity.textures[3]), Axis.Y, maxY, minX, minZ, maxX, maxZ, 1);
		renderSide(info, getSprite(entity.textures[4]), Axis.Z, minZ, minX, minY, maxX, maxY, -1);
		renderSide(info, getSprite(entity.textures[5]), Axis.Z, maxZ, maxX, minY, minX, maxY, 1);

		// Duplication for inner side because portal is transparent
		renderSide(info, getSprite(entity.textures[0]), Axis.X, minX, minY, maxZ, maxY, minZ, 1);
		renderSide(info, getSprite(entity.textures[1]), Axis.X, maxX, minY, minZ, maxY, maxZ, -1);
		renderSide(info, getSprite(entity.textures[2]), Axis.Y, minY, minX, minZ, maxX, maxZ, 1);
		renderSide(info, getSprite(entity.textures[3]), Axis.Y, maxY, minX, maxZ, maxX, minZ, -1);
		renderSide(info, getSprite(entity.textures[4]), Axis.Z, minZ, maxX, minY, minX, maxY, 1);
		renderSide(info, getSprite(entity.textures[5]), Axis.Z, maxZ, minX, minY, maxX, maxY, -1);
	}

	private void renderSide(QuadRenderInfo info, TextureAtlasSprite sprite, Axis axis, float c1, float min1, float min2, float max1, float max2, float n) {
		float u0 = Mth.lerp(min1, sprite.getU0(), sprite.getU1());
		float v0 = Mth.lerp(min2, sprite.getV0(), sprite.getV1());
		float u1 = Mth.lerp(max1, sprite.getU0(), sprite.getU1());
		float v1 = Mth.lerp(max2, sprite.getV0(), sprite.getV1());

		switch (axis) {
			case X -> {
				info.vertex(c1, min1, min2, u0, v0, n, 0, 0);
				info.vertex(c1, min1, max2, u0, v1, n, 0, 0);
				info.vertex(c1, max1, max2, u1, v1, n, 0, 0);
				info.vertex(c1, max1, min2, u1, v0, n, 0, 0);
			}
			case Y -> {
				info.vertex(min1, c1, min2, u0, v0, 0, n, 0);
				info.vertex(min1, c1, max2, u0, v1, 0, n, 0);
				info.vertex(max1, c1, max2, u1, v1, 0, n, 0);
				info.vertex(max1, c1, min2, u1, v0, 0, n, 0);
			}
			case Z -> {
				info.vertex(min1, min2, c1, u0, v0, 0, 0, n);
				info.vertex(min1, max2, c1, u0, v1, 0, 0, n);
				info.vertex(max1, max2, c1, u1, v1, 0, 0, n);
				info.vertex(max1, min2, c1, u1, v0, 0, 0, n);
			}
		}
	}

	public static TextureAtlasSprite getSprite(Identifier location) {
		if (location == null)
			return getSprite(ReactorDebrisBlockEntity.DEFAULT_TEXTURE);
		return spriteCache.computeIfAbsent(location, loc ->
			Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(loc)
		);
	}

	public static class RenderState extends BlockEntityRenderState {
		public Identifier[] textures = new Identifier[6];
		public float minX;
		public float minY;
		public float minZ;
		public float maxX;
		public float maxY;
		public float maxZ;
	}
}
