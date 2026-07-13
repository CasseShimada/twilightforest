package twilightforest.datagen.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import twilightforest.init.TFStructures;
import twilightforest.tags.TFStructureTags;

import java.util.concurrent.CompletableFuture;

public final class StructureTagGenerator extends FabricTagsProvider<Structure> {
	public StructureTagGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, Registries.STRUCTURE, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		this.builder(TFStructureTags.LANDMARK)
			.add(TFStructures.HEDGE_MAZE)
			.add(TFStructures.QUEST_GROVE)
			.add(TFStructures.MUSHROOM_TOWER)
			.add(TFStructures.HOLLOW_HILL_SMALL)
			.add(TFStructures.HOLLOW_HILL_MEDIUM)
			.add(TFStructures.HOLLOW_HILL_LARGE)
			.add(TFStructures.NAGA_COURTYARD)
			.add(TFStructures.LICH_TOWER)
			.add(TFStructures.LABYRINTH)
			.add(TFStructures.HYDRA_LAIR)
			.add(TFStructures.KNIGHT_STRONGHOLD)
			.add(TFStructures.DARK_TOWER)
			.add(TFStructures.YETI_CAVE)
			.add(TFStructures.AURORA_PALACE)
			.add(TFStructures.TROLL_CAVE)
			.add(TFStructures.FINAL_CASTLE);
	}
}
