package twilightforest.world.components.structures.finalcastle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.mixin.accessor.StructurePieceFieldsAccessor;
import twilightforest.world.components.structures.TFStructureComponentOld;


public class FinalCastleMuralComponent extends TFStructureComponentOld {

	private volatile @Nullable FinalCastleMural mural;

	public FinalCastleMuralComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		super(TFStructurePieceTypes.TFFCMur, nbt);

		if (nbt.contains("muralBytes")) {
			this.mural = new FinalCastleMural(nbt);
		} else {
			this.mural = null;
		}
	}

	public FinalCastleMuralComponent(int i, int x, int y, int z, int width, int height, Direction direction, long worldSeed) {
		super(TFStructurePieceTypes.TFFCMur, i, x, y, z);
		this.setOrientation(direction);
		this.boundingBox = TFStructureComponentOld.getComponentToAddBoundingBox2(x, y, z, 0, -height / 2, -width / 2, 1, height - 1, width - 1, direction);

		this.mural = new FinalCastleMural(this.boundingBox, ((StructurePieceFieldsAccessor) this).twilightforest$getOrientation(), worldSeed + (this.boundingBox.minX() * 321534781L) ^ (this.boundingBox.minZ() * 756839L));
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tagCompound) {
		super.addAdditionalSaveData(ctx, tagCompound);

		FinalCastleMural mural = this.mural;
		if (mural != null) {
			mural.writeIntoTag(tagCompound);
		}
	}

	@Override
	public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
		FinalCastleMural mural = this.getOrCreateMural(world.getSeed());

		final BlockState castleMagic = TFBlocks.YELLOW_CASTLE_RUNE_BRICK.defaultBlockState();

		// copy mural to world
		for (int x = 0; x < mural.width; x++) {
			for (int y = 0; y < mural.height; y++) {
				if (mural.get(x, y) > 0) {
					this.placeBlock(world, castleMagic, 0, y, x, sbb);
				} else {
					//this.setBlockState(world, TFBlocks.forceField, 0, 0, y, x, sbb);
				}
			}
		}
	}

	private synchronized FinalCastleMural getOrCreateMural(long worldSeed) {
		if (this.mural == null) {
			this.mural = new FinalCastleMural(this.boundingBox, ((StructurePieceFieldsAccessor) this).twilightforest$getOrientation(), worldSeed + (this.boundingBox.minX() * 321534781L) ^ (this.boundingBox.minZ() * 756839L));
		}
		return this.mural;
	}
}
