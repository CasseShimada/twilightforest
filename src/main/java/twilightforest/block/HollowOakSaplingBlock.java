package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import twilightforest.init.TFStructures;
import twilightforest.world.components.structures.TreeGrowerStartable;

import java.util.Optional;

public class HollowOakSaplingBlock extends SaplingBlock {
	private static final TreeGrower DUMMY_GROWER = new TreeGrower("hollow_oak", Optional.empty(), Optional.empty(), Optional.empty());

	public HollowOakSaplingBlock(BlockBehaviour.Properties properties) {
		super(DUMMY_GROWER, properties);
	}

	@Override
	public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
		if (state.getValue(STAGE) == 0) {
			level.setBlock(pos, state.cycle(STAGE), 260);
			return;
		}

		growHollowTree(level, pos, state);
	}

	private static boolean growHollowTree(ServerLevel level, BlockPos pos, BlockState state) {
		Structure structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValueOrThrow(TFStructures.HOLLOW_TREE);

		if (!(structure instanceof TreeGrowerStartable treeGrowerStartable) || !treeGrowerStartable.checkSaplingClearance(level, pos)) {
			return false;
		}

		ChunkGenerator generator = level.getChunkSource().getGenerator();
		StructureStart structurestart = treeGrowerStartable.generateFromSapling(level.registryAccess(), generator, generator.getBiomeSource(), level.getChunkSource().randomState(), level.getStructureManager(), level.getSeed(), pos, level);

		if (!structurestart.isValid()) {
			return false;
		}

		BoundingBox boundingbox = structurestart.getBoundingBox();
		ChunkPos start = ChunkPos.containing(new BlockPos(boundingbox.minX(), boundingbox.minY(), boundingbox.minZ()));
		ChunkPos end = ChunkPos.containing(new BlockPos(boundingbox.maxX(), boundingbox.maxY(), boundingbox.maxZ()));

		if (ChunkPos.rangeClosed(start, end).noneMatch(currentChunkPos -> level.isLoaded(currentChunkPos.getWorldPosition()))) {
			return false;
		}

		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

		ChunkPos.rangeClosed(start, end).forEach(chunkPos -> structurestart.placeInChunk(
			level,
			level.structureManager(),
			generator,
			level.getRandom(),
			new BoundingBox(
				chunkPos.getMinBlockX(),
				level.getMinY(),
				chunkPos.getMinBlockZ(),
				chunkPos.getMaxBlockX(),
				level.getMaxY(),
				chunkPos.getMaxBlockZ()
			),
			chunkPos
		));

		return true;
	}
}
