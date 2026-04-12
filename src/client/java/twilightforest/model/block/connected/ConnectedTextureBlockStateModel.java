package twilightforest.client.model.block.connected;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import twilightforest.client.model.block.BlockModelContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ConnectedTextureBlockStateModel implements BlockStateModel {
	private final Set<Direction> connectedFaces;
	private final Set<Direction> unculledFaces;
	private final boolean renderOverlayOnAllFaces;
	private final List<Block> validConnectors;
	private final Map<Direction, BakedQuad[]> baseQuads;
	private final Map<Direction, BakedQuad[][]> connectedQuads;
	private final Material.Baked particle;
	private final boolean useAmbientOcclusion;
	private final int materialFlags;

	public ConnectedTextureBlockStateModel(
		Set<Direction> connectedFaces,
		Set<Direction> unculledFaces,
		boolean renderOverlayOnAllFaces,
		List<Block> connectableBlocks,
		Map<Direction, BakedQuad[]> baseQuads,
		Map<Direction, BakedQuad[][]> connectedQuads,
		Material.Baked particle,
		boolean useAmbientOcclusion,
		int materialFlags
	) {
		this.connectedFaces = connectedFaces;
		this.unculledFaces = unculledFaces;
		this.renderOverlayOnAllFaces = renderOverlayOnAllFaces;
		this.validConnectors = connectableBlocks;
		this.baseQuads = baseQuads;
		this.connectedQuads = connectedQuads;
		this.particle = particle;
		this.useAmbientOcclusion = useAmbientOcclusion;
		this.materialFlags = materialFlags;
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
		parts.add(new ConnectedTexturePart());
	}

	@Override
	public Material.Baked particleMaterial() {
		return particle;
	}

	@Override
	public int materialFlags() {
		return this.materialFlags;
	}

	private QuadCollection buildQuads(@Nullable BlockAndTintGetter level, BlockPos pos) {
		ConnectionLogic[][] logic = level != null ? computeLogic(level, pos) : null;

		QuadCollection.Builder builder = new QuadCollection.Builder();

		for (Direction side : Direction.values()) {
			BakedQuad[] base = baseQuads.get(side);
			if (base != null) {
				for (BakedQuad quad : base) {
					builder.addCulledFace(side, quad);
				}
			}

			if (connectedFaces.contains(side) || renderOverlayOnAllFaces) {
				BakedQuad[][] perSide = connectedQuads.get(side);
				if (perSide == null) continue;

				for (int quad = 0; quad < 4; ++quad) {
					ConnectionLogic type = ConnectionLogic.NONE;
					if (logic != null && connectedFaces.contains(side)) {
						type = logic[side.get3DDataValue()][quad];
					}

					BakedQuad baked = perSide[quad][type.ordinal()];
					if (unculledFaces.contains(side)) {
						builder.addUnculledFace(baked);
					} else {
						builder.addCulledFace(side, baked);
					}
				}
			}
		}

		return builder.build();
	}

	private ConnectionLogic[][] computeLogic(BlockAndTintGetter level, BlockPos pos) {
		ConnectionLogic[][] logic = new ConnectionLogic[6][4];

		for (Direction face : Direction.values()) {
			Direction[] directions = ConnectionLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];
			boolean[] sideStates = new boolean[4];

			for (int faceIndex = 0; faceIndex < directions.length; faceIndex++) {
				sideStates[faceIndex] = shouldConnectSide(level, pos, face, directions[faceIndex]);
			}

			int faceIndex = face.get3DDataValue();
			for (int dir = 0; dir < directions.length; dir++) {
				int cornerOffset = (dir + 1) % directions.length;
				boolean side1 = sideStates[dir];
				boolean side2 = sideStates[cornerOffset];
				boolean corner = side1 && side2 && isCornerBlockPresent(level, pos, face, directions[dir], directions[cornerOffset]);
				logic[faceIndex][dir] = dir % 2 == 0 ? ConnectionLogic.of(side1, side2, corner) : ConnectionLogic.of(side2, side1, corner);
			}
		}

		return logic;
	}

	private boolean shouldConnectSide(BlockAndTintGetter getter, BlockPos pos, Direction face, Direction side) {
		var neighborState = getter.getBlockState(pos.relative(side));
		boolean matches = validConnectors.stream().anyMatch(neighborState::is);
		if (!matches) return false;

		if (unculledFaces.contains(face)) return true;
		return Block.shouldRenderFace(getter.getBlockState(pos), getter.getBlockState(pos.relative(face)), face);
	}

	private boolean isCornerBlockPresent(BlockAndTintGetter getter, BlockPos pos, Direction face, Direction side1, Direction side2) {
		var neighborState = getter.getBlockState(pos.relative(side1).relative(side2));
		boolean matches = validConnectors.stream().anyMatch(neighborState::is);
		if (!matches) return false;

		if (unculledFaces.contains(face)) return true;
		return Block.shouldRenderFace(getter.getBlockState(pos), getter.getBlockState(pos.relative(face)), face);
	}

	private final class ConnectedTexturePart implements BlockStateModelPart {
		@Override
		public List<BakedQuad> getQuads(@Nullable Direction direction) {
			BlockModelContext.Context ctx = BlockModelContext.get();
			if (ctx == null) {
				return buildQuads(null, BlockPos.ZERO).getQuads(direction);
			}
			return buildQuads(ctx.level(), ctx.pos()).getQuads(direction);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return useAmbientOcclusion;
		}

		@Override
		public Material.Baked particleMaterial() {
			return particle;
		}

		@Override
		public int materialFlags() {
			return ConnectedTextureBlockStateModel.this.materialFlags;
		}
	}

	public record Unbaked(Variant variant) implements CustomUnbakedBlockStateModel {
		public static final MapCodec<Unbaked> MAP_CODEC = Variant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

		@Override
		public BlockStateModel bake(ModelBaker baker) {
			ResolvedModel resolved = baker.getModel(variant.modelLocation());
			if (resolved.wrapped() instanceof UnbakedConnectedTextureModel unbaked) {
				return unbaked.bakeToBlockStateModel(baker, resolved, variant.modelState().asModelState());
			}

			return new SingleVariant(variant.bake(baker));
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			variant.resolveDependencies(resolver);
		}

		@Override
		public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
			return MAP_CODEC;
		}
	}
}
