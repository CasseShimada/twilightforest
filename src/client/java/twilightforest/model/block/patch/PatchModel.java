package twilightforest.client.model.block.patch;

import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.joml.Vector3f;
import twilightforest.block.PatchBlock;
import twilightforest.client.model.block.BlockModelContext;

import java.util.ArrayList;
import java.util.List;

public class PatchModel implements BlockStateModel {
	private final Material.Baked texture;
	private final boolean shaggify;
	private final Material.Baked particle;
	private final boolean usesAmbientOcclusion;
	private final ModelBaker baker;
	private final int materialFlags;

	public PatchModel(Material.Baked texture, boolean shaggify, Material.Baked particle, boolean usesAmbientOcclusion, ModelBaker baker) {
		this.texture = texture;
		this.shaggify = shaggify;
		this.particle = particle;
		this.usesAmbientOcclusion = usesAmbientOcclusion;
		this.baker = baker;
		this.materialFlags = this.buildQuads(false, false, false, false, RandomSource.create(0L)).materialFlags();
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
		BlockModelContext.Context ctx = BlockModelContext.get();
		boolean north = false;
		boolean east = false;
		boolean south = false;
		boolean west = false;
		if (ctx != null) {
			var state = ctx.state();
			north = state.getValue(PatchBlock.NORTH);
			east = state.getValue(PatchBlock.EAST);
			south = state.getValue(PatchBlock.SOUTH);
			west = state.getValue(PatchBlock.WEST);
		}
		output.add(new PatchPart(this.buildQuads(north, east, south, west, RandomSource.create(random.nextLong()))));
	}

	@Override
	public Material.Baked particleMaterial() {
		return this.particle;
	}

	@Override
	public int materialFlags() {
		return this.materialFlags;
	}

	QuadCollection buildQuads(boolean north, boolean east, boolean south, boolean west, RandomSource posRandom) {
		List<BakedQuad> list = new ArrayList<>();

		BoundingBox bb = PatchBlock.AABBFromRandom(posRandom);

		this.quadsFromAABB(list, west ? 0 : bb.minX(), bb.minY(), north ? 0 : bb.minZ(), east ? 16 : bb.maxX(), bb.maxY(), south ? 16 : bb.maxZ());

		if (!this.shaggify) {
			return toCollection(list);
		}

		long westSeed = posRandom.nextLong();
		long eastSeed = posRandom.nextLong();
		long northSeed = posRandom.nextLong();
		long southSeed = posRandom.nextLong();

		int minY = bb.minY();
		int maxY = bb.maxY();

		if (!west) {
			long seed = westSeed;
			seed = seed * seed * 42317861L + seed * 7L;

			int num0 = (int) (seed >> 12 & 3L) + 1;
			int num1 = (int) (seed >> 15 & 3L) + 1;
			int num2 = (int) (seed >> 18 & 3L) + 1;
			int num3 = (int) (seed >> 21 & 3L) + 1;

			int minZ = bb.minZ() + num0;
			int maxZ = bb.maxZ();

			if (maxZ - ((num1 + num2 + num3)) > minZ) {
				int innerZ = bb.maxZ() - num2;
				this.quadsFromAABB(list, bb.minX() - 1, minY, minZ, bb.minX(), maxY, minZ + num1);
				this.quadsFromAABB(list, bb.minX() - 1, minY, innerZ - num3, bb.minX(), maxY, innerZ);
			} else {
				this.quadsFromAABB(list, bb.minX() - 1, minY, minZ, bb.minX(), maxY, maxZ - num2);
			}
		}

		if (!east) {
			long seed = eastSeed;
			seed = seed * seed * 42317861L + seed * 17L;

			int num0 = (int) (seed >> 12 & 3L) + 1;
			int num1 = (int) (seed >> 15 & 3L) + 1;
			int num2 = (int) (seed >> 18 & 3L) + 1;
			int num3 = (int) (seed >> 21 & 3L) + 1;

			int minZ = bb.minZ() + num0;
			int maxZ = bb.maxZ();

			if (maxZ - ((num1 + num2 + num3)) > minZ) {
				int innerZ = maxZ - num2;
				this.quadsFromAABB(list, bb.maxX(), minY, minZ, bb.maxX() + 1, maxY, minZ + num1);
				this.quadsFromAABB(list, bb.maxX(), minY, innerZ - num3, bb.maxX() + 1, maxY, innerZ);
			} else {
				this.quadsFromAABB(list, bb.maxX(), minY, minZ, bb.maxX() + 1, maxY, maxZ - num2);
			}
		}

		if (!north) {
			long seed = northSeed;
			seed = seed * seed * 42317861L + seed * 23L;

			int num0 = (int) (seed >> 12 & 3L) + 1;
			int num1 = (int) (seed >> 15 & 3L) + 1;
			int num2 = (int) (seed >> 18 & 3L) + 1;
			int num3 = (int) (seed >> 21 & 3L) + 1;

			int minX = bb.minX() + num0;
			int innerX = minX + num1;
			int maxX = bb.maxX() - num2;

			this.quadsFromAABB(list, minX, minY, bb.minZ() - 1, innerX, maxY, bb.minZ());
			this.quadsFromAABB(list, maxX - num3, minY, bb.minZ() - 1, maxX, maxY, bb.minZ());
		}

		if (!south) {
			long seed = southSeed;
			seed = seed * seed * 42317861L + seed * 11L;

			int num0 = (int) (seed >> 12 & 3L) + 1;
			int num1 = (int) (seed >> 15 & 3L) + 1;
			int num2 = (int) (seed >> 18 & 3L) + 1;
			int num3 = (int) (seed >> 21 & 3L) + 1;

			int minX = bb.minX() + num0;
			int maxX = bb.maxX() - num2;

			this.quadsFromAABB(list, minX, minY, bb.maxZ(), minX + num1, maxY, bb.maxZ() + 1);
			this.quadsFromAABB(list, maxX - num3, minY, bb.maxZ(), maxX, maxY, bb.maxZ() + 1);
		}

		return toCollection(list);
	}

	private static QuadCollection toCollection(List<BakedQuad> list) {
		QuadCollection.Builder builder = new QuadCollection.Builder();
		for (BakedQuad quad : list) {
			builder.addUnculledFace(quad);
		}
		return builder.build();
	}

	private void quadsFromAABB(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		quads.add(this.quadFromVectors(Direction.UP, minX, minY, minZ, maxX, maxY, maxZ));
		quads.add(this.quadFromVectors(Direction.NORTH, minX, minY, minZ, maxX, maxY, maxZ));
		quads.add(this.quadFromVectors(Direction.EAST, minX, minY, minZ, maxX, maxY, maxZ));
		quads.add(this.quadFromVectors(Direction.SOUTH, minX, minY, minZ, maxX, maxY, maxZ));
		quads.add(this.quadFromVectors(Direction.WEST, minX, minY, minZ, maxX, maxY, maxZ));
		quads.add(this.quadFromVectors(Direction.DOWN, minX, minY, minZ, maxX, maxY, maxZ));
	}

	private BakedQuad quadFromVectors(Direction direction, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		Quadrant rotation = (direction == Direction.EAST || direction == Direction.WEST) ? Quadrant.R90 : Quadrant.R0;
		CuboidFace face = new CuboidFace(null, 0, this.texture.sprite().atlasLocation().toString(), switch (direction) {
			case NORTH -> new CuboidFace.UVs(maxX, minZ + 1f, minX, minZ);
			case EAST -> new CuboidFace.UVs(maxX, minZ, maxX - 1f, maxZ);
			case SOUTH -> new CuboidFace.UVs(minX, maxZ, maxX, maxZ - 1f);
			case WEST -> new CuboidFace.UVs(minX, maxZ, minX + 1f, minZ);
			default -> new CuboidFace.UVs(minX, minZ, maxX, maxZ);
		}, rotation);

		return FaceBakery.bakeQuad(
			baker,
			new Vector3f(minX, minY, minZ),
			new Vector3f(maxX, maxY, maxZ),
			face,
			this.texture,
			direction,
			BlockModelRotation.IDENTITY,
			null,
			true,
			0
		);
	}

	private final class PatchPart implements BlockStateModelPart {
		private final QuadCollection quads;

		private PatchPart(QuadCollection quads) {
			this.quads = quads;
		}

		@Override
		public List<BakedQuad> getQuads(Direction direction) {
			return this.quads.getQuads(direction);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return usesAmbientOcclusion;
		}

		@Override
		public Material.Baked particleMaterial() {
			return particle;
		}

		@Override
		public int materialFlags() {
			return PatchModel.this.materialFlags;
		}
	}

	public record Unbaked(Variant variant) implements CustomUnbakedBlockStateModel {
		public static final MapCodec<Unbaked> MAP_CODEC = Variant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

		@Override
		public BlockStateModel bake(ModelBaker baker) {
			ResolvedModel resolved = baker.getModel(variant.modelLocation());
			if (resolved.wrapped() instanceof UnbakedPatchModel unbaked) {
				return unbaked.bakeToBlockStateModel(baker, resolved);
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
