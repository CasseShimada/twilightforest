package twilightforest.client.model.block.patch;

import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.joml.Vector3f;
import twilightforest.block.PatchBlock;
import twilightforest.client.model.block.BlockModelContext;

import java.util.ArrayList;
import java.util.List;

public class PatchModel implements BlockStateModel {
	private final TextureAtlasSprite texture;
	private final boolean shaggify;
	private final TextureAtlasSprite particle;
	private final boolean usesAmbientOcclusion;
	private final ModelBaker.PartCache partCache;

	public PatchModel(TextureAtlasSprite texture, boolean shaggify, TextureAtlasSprite particle, boolean usesAmbientOcclusion, ModelBaker.PartCache partCache) {
		this.texture = texture;
		this.shaggify = shaggify;
		this.particle = particle;
		this.usesAmbientOcclusion = usesAmbientOcclusion;
		this.partCache = partCache;
	}

	@Override
	public void collectParts(RandomSource random, List<BlockModelPart> output) {
		output.add(new PatchPart(random.nextLong()));
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return this.particle;
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
		BlockElementFace face = new BlockElementFace(null, 0, this.texture.atlasLocation().toString(), switch (direction) {
			case NORTH -> new BlockElementFace.UVs(maxX, minZ + 1f, minX, minZ);
			case EAST -> new BlockElementFace.UVs(maxX, minZ, maxX - 1f, maxZ);
			case SOUTH -> new BlockElementFace.UVs(minX, maxZ, maxX, maxZ - 1f);
			case WEST -> new BlockElementFace.UVs(minX, maxZ, minX + 1f, minZ);
			default -> new BlockElementFace.UVs(minX, minZ, maxX, maxZ);
		}, rotation);

		return FaceBakery.bakeQuad(
			partCache,
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

	private final class PatchPart implements BlockModelPart {
		private final long seed;

		private PatchPart(long seed) {
			this.seed = seed;
		}

		@Override
		public List<BakedQuad> getQuads(Direction direction) {
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
			RandomSource seeded = RandomSource.create(seed);
			return buildQuads(north, east, south, west, seeded).getQuads(direction);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return usesAmbientOcclusion;
		}

		@Override
		public TextureAtlasSprite particleIcon() {
			return particle;
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
