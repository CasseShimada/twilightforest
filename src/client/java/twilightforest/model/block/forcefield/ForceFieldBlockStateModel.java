package twilightforest.client.model.block.forcefield;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import twilightforest.client.model.block.BlockModelContext;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ForceFieldBlockStateModel implements BlockStateModel {
	private final List<Part> parts;
	private final TextureAtlasSprite particle;
	private final boolean useAmbientOcclusion;

	public ForceFieldBlockStateModel(List<Part> parts, TextureAtlasSprite particle, boolean useAmbientOcclusion) {
		this.parts = parts;
		this.particle = particle;
		this.useAmbientOcclusion = useAmbientOcclusion;
	}

	@Override
	public void collectParts(RandomSource random, List<BlockModelPart> output) {
		output.add(new ForceFieldPart());
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return particle;
	}

	private QuadCollection buildQuads(Map<ForceFieldModel.ExtraDirection, List<Direction>> directions) {
		QuadCollection.Builder builder = new QuadCollection.Builder();

		for (Part part : parts) {
			ForceFieldModelLoader.Condition condition = part.condition();
			if (ForceFieldModel.skipRender(directions, condition.direction(), condition.b(), condition.parents(), part.face())) {
				continue;
			}

			if (part.cullFace() == null) {
				builder.addUnculledFace(part.quad());
			} else {
				builder.addCulledFace(part.cullFace(), part.quad());
			}
		}

		return builder.build();
	}

	private Map<ForceFieldModel.ExtraDirection, List<Direction>> collectDirections(@Nullable BlockModelContext.Context ctx) {
		Map<ForceFieldModel.ExtraDirection, List<Direction>> directions = new EnumMap<>(ForceFieldModel.ExtraDirection.class);
		for (ForceFieldModel.ExtraDirection dir : ForceFieldModel.ExtraDirection.values()) {
			directions.put(dir, List.of());
		}
		if (ctx == null) {
			return directions;
		}

		BlockState state = ctx.state();
		var level = ctx.level();
		BlockPos pos = ctx.pos();
		for (ForceFieldModel.ExtraDirection extraDirection : ForceFieldModel.getExtraDirections(state, level, pos)) {
			List<Direction> directionList = new java.util.ArrayList<>();
			for (Direction dir : Direction.values()) {
				ForceFieldModel.ExtraDirection mirrored = extraDirection.mirrored(dir.getAxis());
				if (mirrored != extraDirection) {
					BlockState other = level.getBlockState(pos.relative(dir));
					if (other.getBlock() instanceof twilightforest.block.ForceFieldBlock) {
						if (ForceFieldModel.getExtraDirections(other, level, pos.relative(dir)).contains(mirrored)) {
							directionList.add(dir);
						}
					}
				}
			}
			directions.put(extraDirection, directionList);
		}
		return directions;
	}

	private final class ForceFieldPart implements BlockModelPart {
		@Override
		public List<BakedQuad> getQuads(@Nullable Direction direction) {
			Map<ForceFieldModel.ExtraDirection, List<Direction>> directions = collectDirections(BlockModelContext.get());
			return buildQuads(directions).getQuads(direction);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return useAmbientOcclusion;
		}

		@Override
		public TextureAtlasSprite particleIcon() {
			return particle;
		}
	}

	public record Part(Direction face, @Nullable Direction cullFace, BakedQuad quad, ForceFieldModelLoader.Condition condition) {}

	public record Unbaked(Variant variant) implements CustomUnbakedBlockStateModel {
		public static final MapCodec<Unbaked> MAP_CODEC = Variant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

		@Override
		public BlockStateModel bake(ModelBaker baker) {
			ResolvedModel resolved = baker.getModel(variant.modelLocation());
			if (resolved.wrapped() instanceof UnbakedForceFieldModel unbaked) {
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
