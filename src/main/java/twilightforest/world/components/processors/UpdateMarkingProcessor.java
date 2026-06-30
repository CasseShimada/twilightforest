package twilightforest.world.components.processors;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFStructureProcessors;

import java.util.Arrays;
import java.util.List;

public class UpdateMarkingProcessor implements StructureProcessor {
	public static final MapCodec<UpdateMarkingProcessor> CODEC = Block.CODEC.codec().listOf().xmap(UpdateMarkingProcessor::new, p -> p.blocksToMarkUpdate).fieldOf("mark_updates");

	private final List<Block> blocksToMarkUpdate;

	public static UpdateMarkingProcessor forBlocks(Block... blocks) {
		return new UpdateMarkingProcessor(Arrays.asList(blocks));
	}

	public UpdateMarkingProcessor(List<Block> blocksToMarkUpdate) {
		this.blocksToMarkUpdate = blocksToMarkUpdate;
	}

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos piecePos, BlockPos originalPos, StructureTemplate.StructureBlockInfo modifiedInfo, StructurePlaceSettings placeSettings) {
		if (this.blocksToMarkUpdate.contains(modifiedInfo.state().getBlock())) {
			level.getChunk(modifiedInfo.pos()).markPosForPostProcessing(modifiedInfo.pos());
		}

		return modifiedInfo;
	}

	@Override
	public MapCodec<UpdateMarkingProcessor> codec() {
		return TFStructureProcessors.UPDATE_MARKING_PROCESSOR.value();
	}
}
