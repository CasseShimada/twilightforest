package twilightforest.client.model.block.connected;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import twilightforest.client.model.block.ModelBakingUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnbakedConnectedTextureModel implements UnbakedModel {
	private final UnbakedModel baseModel;
	private final boolean renderOverlayOnAllFaces;
	private final Set<Direction> connectedFaces;
	private final List<Block> connectableBlocks;
	private final CuboidModelElement[][] baseElements;
	private final CuboidModelElement[][][] connectedElements;

	public UnbakedConnectedTextureModel(
		UnbakedModel baseModel,
		Pair<Vector3f, Vector3f> element,
		Set<Direction> connectedFaces,
		boolean renderOnDisabledFaces,
		List<Block> connectableBlocks,
		int baseTintIndex,
		int baseEmissivity,
		int tintIndex,
		int emissivity
	) {
		this.baseModel = baseModel;
		this.connectedFaces = connectedFaces;
		this.renderOverlayOnAllFaces = renderOnDisabledFaces;
		this.connectableBlocks = connectableBlocks;

		this.baseElements = new CuboidModelElement[6][4];
		this.connectedElements = new CuboidModelElement[6][4][5];

		int center = 8;

		for (Direction face : Direction.values()) {
			Direction cull = getCullface(face, element.getFirst(), element.getSecond());
			Direction[] planeDirections = ConnectionLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];

			for (int i = 0; i < 4; ++i) {
				Vec3i corner = face.getUnitVec3i().offset(planeDirections[i].getUnitVec3i()).offset(planeDirections[(i + 1) % 4].getUnitVec3i()).offset(1, 1, 1).multiply(8);
				Vector3f from = new Vector3f(
					Math.clamp(Math.min(center - (16 - element.getSecond().x()), corner.getX() + element.getFirst().x()), 0, 16),
					Math.clamp(Math.min(center - (16 - element.getSecond().y()), corner.getY() + element.getFirst().y()), 0, 16),
					Math.clamp(Math.min(center - (16 - element.getSecond().z()), corner.getZ() + element.getFirst().z()), 0, 16)
				);
				Vector3f to = new Vector3f(
					element.getSecond().x() < center ? element.getSecond().x() : Math.max(center, corner.getX() - (16 - element.getSecond().x())),
					element.getSecond().y() < center ? element.getSecond().y() : Math.max(center, corner.getY() - (16 - element.getSecond().y())),
					element.getSecond().z() < center ? element.getSecond().z() : Math.max(center, corner.getZ() - (16 - element.getSecond().z()))
				);

				this.baseElements[face.get3DDataValue()][i] = new CuboidModelElement(
					from,
					to,
					Map.of(face, new CuboidFace(cull, baseTintIndex, "", remapUVs(from, to, face, ConnectionLogic.NONE), Quadrant.R0)),
					null,
					true,
					baseEmissivity
				);

				for (ConnectionLogic logic : ConnectionLogic.values()) {
					this.connectedElements[face.get3DDataValue()][i][logic.ordinal()] = new CuboidModelElement(
						from,
						to,
						Map.of(face, new CuboidFace(cull, tintIndex, "", remapUVs(from, to, face, logic), Quadrant.R0)),
						null,
						true,
						emissivity
					);
				}
			}
		}
	}

	@Override
	public Boolean ambientOcclusion() {
		return baseModel.ambientOcclusion();
	}

	@Override
	public GuiLight guiLight() {
		return baseModel.guiLight();
	}

	@Override
	public ItemTransforms transforms() {
		return baseModel.transforms();
	}

	@Override
	public TextureSlots.Data textureSlots() {
		return baseModel.textureSlots();
	}

	@Override
	public UnbakedGeometry geometry() {
		return (textureSlots, baker, modelState, name) -> {
			QuadData data = bakeQuads(baker, textureSlots, modelState, name);
			QuadCollection.Builder builder = new QuadCollection.Builder();

			for (Direction side : Direction.values()) {
				BakedQuad[] base = data.baseQuads.get(side);
				if (base != null) {
					for (BakedQuad quad : base) {
						builder.addCulledFace(side, quad);
					}
				}

				if (connectedFaces.contains(side) || renderOverlayOnAllFaces) {
					BakedQuad[][] perSide = data.connectedQuads.get(side);
					if (perSide == null) continue;
					for (int quad = 0; quad < 4; ++quad) {
						BakedQuad overlay = perSide[quad][ConnectionLogic.NONE.ordinal()];
						if (data.unculledFaces.contains(side)) {
							builder.addUnculledFace(overlay);
						} else {
							builder.addCulledFace(side, overlay);
						}
					}
				}
			}

			return builder.build();
		};
	}

	@Override
	public Identifier parent() {
		return baseModel.parent();
	}

	public ConnectedTextureBlockStateModel bakeToBlockStateModel(ModelBaker baker, ResolvedModel resolvedModel, ModelState state) {
		TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
		QuadData data = bakeQuads(baker, textureSlots, state, resolvedModel);

		return new ConnectedTextureBlockStateModel(
			this.connectedFaces,
			data.unculledFaces,
			this.renderOverlayOnAllFaces,
			this.connectableBlocks,
			data.baseQuads,
			data.connectedQuads,
			data.particle,
			resolvedModel.getTopAmbientOcclusion(),
			data.materialFlags
		);
	}

	private QuadData bakeQuads(ModelBaker baker, TextureSlots textureSlots, ModelState state, ModelDebugName name) {
		Map<Direction, BakedQuad[]> baseQuads = new HashMap<>();
		Set<Direction> unculledFaces = new HashSet<>();

		if (textureSlots.getMaterial("base_texture") != null) {
			Material.Baked baseTexture = ModelBakingUtil.resolveMaterial(baker, textureSlots, "base_texture", name);
			for (Direction dir : Direction.values()) {
				BakedQuad[] quads = new BakedQuad[this.baseElements[dir.get3DDataValue()].length];
				int idx = 0;
				for (CuboidModelElement element : this.baseElements[dir.get3DDataValue()]) {
					quads[idx++] = FaceBakery.bakeQuad(
						baker,
						element.from(),
						element.to(),
						element.faces().get(dir),
						baseTexture,
						dir,
						state,
						element.rotation(),
						element.shade(),
						element.lightEmission()
					);
				}
				baseQuads.put(dir, quads);
			}
		}

		Material.Baked overlayTexture = ModelBakingUtil.resolveMaterial(baker, textureSlots, "overlay_texture", name);
		Material.Baked overlayConnectedTexture = ModelBakingUtil.resolveMaterial(baker, textureSlots, "overlay_connected", name);
		Material.Baked particle = textureSlots.getMaterial("particle") != null
			? ModelBakingUtil.resolveMaterial(baker, textureSlots, "particle", name)
			: overlayTexture;

		Material.Baked[] materials = new Material.Baked[]{overlayTexture, overlayConnectedTexture, particle};
		Map<Direction, BakedQuad[][]> connectedQuads = new HashMap<>();
		int materialFlags = 0;

		for (Direction dir : Direction.values()) {
			BakedQuad[][] dirQuads = new BakedQuad[4][5];
			for (int quad = 0; quad < 4; quad++) {
				for (int type = 0; type < 5; type++) {
					CuboidModelElement element = this.connectedElements[dir.get3DDataValue()][quad][type];
					CuboidFace face = element.faces().get(dir);
					if (face.cullForDirection() == null) unculledFaces.add(dir);
					dirQuads[quad][type] = FaceBakery.bakeQuad(
						baker,
						element.from(),
						element.to(),
						face,
						ConnectionLogic.values()[type].chooseMaterial(materials),
						dir,
						state,
						element.rotation(),
						element.shade(),
						element.lightEmission()
					);
					materialFlags |= dirQuads[quad][type].materialInfo().flags();
				}
			}
			connectedQuads.put(dir, dirQuads);
		}

		for (BakedQuad[] quads : baseQuads.values()) {
			for (BakedQuad quad : quads) {
				materialFlags |= quad.materialInfo().flags();
			}
		}

		return new QuadData(baseQuads, connectedQuads, unculledFaces, particle, materialFlags);
	}

	private static CuboidFace.UVs remapUVs(Vector3f from, Vector3f to, Direction face, ConnectionLogic logic) {
		CuboidFace.UVs base = defaultFaceUV(from, to, face);
		float[] uvs = new float[]{base.minU(), base.minV(), base.maxU(), base.maxV()};
		float[] remapped = logic.remapUVs(uvs);
		return new CuboidFace.UVs(remapped[0], remapped[1], remapped[2], remapped[3]);
	}

	private static CuboidFace.UVs defaultFaceUV(Vector3f from, Vector3f to, Direction face) {
		return switch (face) {
			case DOWN -> new CuboidFace.UVs(from.x(), 16.0F - to.z(), to.x(), 16.0F - from.z());
			case UP -> new CuboidFace.UVs(from.x(), from.z(), to.x(), to.z());
			case NORTH -> new CuboidFace.UVs(16.0F - to.x(), 16.0F - to.y(), 16.0F - from.x(), 16.0F - from.y());
			case SOUTH -> new CuboidFace.UVs(from.x(), 16.0F - to.y(), to.x(), 16.0F - from.y());
			case WEST -> new CuboidFace.UVs(from.z(), 16.0F - to.y(), to.z(), 16.0F - from.y());
			case EAST -> new CuboidFace.UVs(16.0F - to.z(), 16.0F - to.y(), 16.0F - from.z(), 16.0F - from.y());
		};
	}

	@Nullable
	private static Direction getCullface(Direction direction, Vector3f from, Vector3f to) {
		boolean cull = switch (direction) {
			case DOWN -> from.y() == 0.0F;
			case UP -> to.y() == 16.0F;
			case NORTH -> from.x() == 0.0F;
			case SOUTH -> to.x() == 16.0F;
			case WEST -> from.z() == 0.0F;
			case EAST -> to.z() == 16.0F;
		};

		return cull ? direction : null;
	}

	private record QuadData(
		Map<Direction, BakedQuad[]> baseQuads,
		Map<Direction, BakedQuad[][]> connectedQuads,
		Set<Direction> unculledFaces,
		Material.Baked particle,
		int materialFlags
	) {
	}
}
