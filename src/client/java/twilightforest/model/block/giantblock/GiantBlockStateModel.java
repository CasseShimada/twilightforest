package twilightforest.client.model.block.giantblock;

import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;
import twilightforest.block.GiantBlock;
import twilightforest.client.model.block.BlockModelContext;
import twilightforest.util.Vec2i;

import java.util.List;

public final class GiantBlockStateModel implements BlockStateModel {
	private final Material.Baked[] textures;
	private final Material.Baked particle;
	private final boolean useAmbientOcclusion;
	private final ModelBaker baker;
	private final int materialFlags;

	public GiantBlockStateModel(Material.Baked[] textures, Material.Baked particle, boolean useAmbientOcclusion, ModelBaker baker) {
		this.textures = textures;
		this.particle = particle;
		this.useAmbientOcclusion = useAmbientOcclusion;
		this.baker = baker;
		this.materialFlags = this.buildQuads(null, BlockPos.ZERO).materialFlags();
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
		output.add(new GiantBlockPart());
	}

	@Override
	public Material.Baked particleMaterial() {
		return particle;
	}

	@Override
	public int materialFlags() {
		return this.materialFlags;
	}

	private QuadCollection buildQuads(net.minecraft.client.renderer.block.BlockAndTintGetter level, BlockPos pos) {
		QuadCollection.Builder builder = new QuadCollection.Builder();

		for (Direction side : Direction.values()) {
			if (level == null || shouldRenderSide(level, pos, side)) {
				Vec2i coords = calculateOffset(side, pos.offset(magicOffsetFromDir(side)));
				Material.Baked sprite = this.textures[this.textures.length > 1 ? side.ordinal() : 0];
				CuboidFace face = new CuboidFace(side, side.ordinal(), side.name(), new CuboidFace.UVs(
					0.0F + coords.x,
					0.0F + coords.z,
					4.0F + coords.x,
					4.0F + coords.z
				), Quadrant.R0);

				builder.addCulledFace(side, FaceBakery.bakeQuad(
					baker,
					new Vector3f(0.0F, 0.0F, 0.0F),
					new Vector3f(16.0F, 16.0F, 16.0F),
					face,
					sprite,
					side,
					BlockModelRotation.IDENTITY,
					null,
					true,
					0
				));
			}
		}

		return builder.build();
	}

	private boolean shouldRenderSide(net.minecraft.client.renderer.block.BlockAndTintGetter level, BlockPos pos, Direction side) {
		return !com.google.common.collect.Iterables.contains(GiantBlock.getVolume(pos), pos.offset(side.getUnitVec3i()));
	}

	private final class GiantBlockPart implements BlockStateModelPart {
		@Override
		public List<net.minecraft.client.resources.model.geometry.BakedQuad> getQuads(Direction direction) {
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
			return GiantBlockStateModel.this.materialFlags;
		}
	}

	private static BlockPos magicOffsetFromDir(Direction side) {
		return switch (side) {
			case DOWN -> new BlockPos(0, 0, 2);
			case NORTH, SOUTH -> new BlockPos(0, 1, 0);
			case WEST, EAST -> new BlockPos(0, 1, -1);
			default -> new BlockPos(0, 0, -1);
		};
	}

	private static Vec2i calculateOffset(Direction side, BlockPos pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		int offsetX;
		int offsetY;

		if (side.getAxis().isVertical()) {
			offsetX = x % 4;
			offsetY = (side.getStepY() * z + 1) % 4;
		} else if (side.getAxis() == Direction.Axis.Z) {
			offsetX = x % 4;
			offsetY = -y % 4;
		} else {
			offsetX = (z + 1) % 4;
			offsetY = -y % 4;
		}

		if (side == Direction.NORTH || side == Direction.EAST) {
			offsetX = (4 - offsetX - 1) % 4;
		}

		if (offsetX < 0) {
			offsetX += 16;
		}
		if (offsetY < 0) {
			offsetY += 16;
		}

		return new Vec2i((offsetX % 4) * 4, (offsetY % 4) * 4);
	}

	public record Unbaked(Variant variant) implements CustomUnbakedBlockStateModel {
		public static final MapCodec<Unbaked> MAP_CODEC = Variant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

		@Override
		public BlockStateModel bake(ModelBaker baker) {
			ResolvedModel resolved = baker.getModel(variant.modelLocation());
			if (resolved.wrapped() instanceof UnbakedGiantBlockModel unbaked) {
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
