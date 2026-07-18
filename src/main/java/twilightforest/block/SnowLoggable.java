package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import static net.minecraft.world.level.block.Block.dropResources;

public interface SnowLoggable {
	float SNOW_Z_FIGHTING = 0.008F;
	int MIN_SNOW_LAYERS = 0;
	int MAX_SNOW_LAYERS = SnowLayerBlock.MAX_HEIGHT;
	IntegerProperty SNOW_LAYERS = IntegerProperty.create("layers", MIN_SNOW_LAYERS, MAX_SNOW_LAYERS);
	VoxelShape[] SNOW_SHAPE_BY_LAYER = Util.make(new VoxelShape[9], shapes -> {
		shapes[0] = Shapes.empty();
		for (int i = 1; i <= 8; i++) {
			shapes[i] = Block.box(0.0, 0.0, 0.0, 16.0, i * 2.0, 16.0);
		}
	});

	default void meltSnow(BlockState state, Level level, BlockPos pos) {
		int snowLayers = state.getValue(SNOW_LAYERS);
		if (level.getBrightness(LightLayer.BLOCK, pos) > 11 && snowLayers > 0 && snowLayers < MAX_SNOW_LAYERS) {
			dropResources(Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, snowLayers), level, pos);
			level.setBlock(pos, state.setValue(SNOW_LAYERS, 0), Block.UPDATE_CLIENTS);
		}
	}

	default void handleBreakingLogic(Level level, BlockPos pos, BlockState state, Player player, @Nullable BlockState blockToConvertTo) {
		BlockHitResult result = this.clip(player);
		Vec3 hit = result.getType() == BlockHitResult.Type.BLOCK ? result.getLocation() : null;
		if (hit != null) {
			hit = hit.add(-pos.getX(), -pos.getY(), -pos.getZ());
		}
		double snowHeight = SNOW_SHAPE_BY_LAYER[state.getValue(SNOW_LAYERS)].bounds().maxY;
		if (hit != null && hit.y() <= snowHeight) {
			level.levelEvent(player, 2001, pos, Block.getId(Blocks.SNOW.defaultBlockState()));
			level.setBlockAndUpdate(pos, blockToConvertTo != null ? blockToConvertTo : state.setValue(SNOW_LAYERS, 0));
		} else {
			level.levelEvent(player, 2001, pos, Block.getId(state.setValue(SNOW_LAYERS, 0)));
			if (!player.isCreative()) {
				dropResources(state, level, pos, null, player, player.getMainHandItem());
			}
			level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, state.getValue(SNOW_LAYERS)));
		}
	}

	default BlockHitResult clip(Player player) {
		Vec3 start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
		Vec3 end = start.add(player.getLookAngle().x() * 6.0D, player.getLookAngle().y() * 6.0D, player.getLookAngle().z() * 6.0D);
		return player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
	}

	default boolean isColliding(Player player, BlockPos pos, BlockState state) {
		return !player.level().isUnobstructed(state, pos, CollisionContext.of(player));
	}
}
