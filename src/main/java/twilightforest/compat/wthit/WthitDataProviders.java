package twilightforest.compat.wthit;

import mcp.mobius.waila.api.IDataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.SpawnData;
import twilightforest.block.ChiseledCanopyShelfBlock;
import twilightforest.block.entity.DryingRackBlockEntity;
import twilightforest.block.entity.bookshelf.ChiseledCanopyShelfBlockEntity;
import twilightforest.entity.passive.QuestRam;

final class WthitDataProviders {
	static final IDataProvider<DryingRackBlockEntity> DRYING_RACK = (data, accessor, config) -> {
		if (!config.getBoolean(WthitConstants.DRYING_RACK)) {
			return;
		}
		DryingRackBlockEntity rack = accessor.getTarget();
		data.raw().putBoolean(WthitConstants.DRYING_PRESENT, true);
		data.raw().putBoolean(WthitConstants.DRYING_ACTIVE, rack.isDrying());
		data.raw().putInt(WthitConstants.DRYING_PROGRESS, rack.getDryTime());
		data.raw().putInt(WthitConstants.DRYING_TOTAL, rack.getTotalDryTime());
	};

	static final IDataProvider<QuestRam> QUEST_RAM = (data, accessor, config) -> {
		if (config.getBoolean(WthitConstants.QUEST_RAM_WOOL)) {
			data.raw().putInt(WthitConstants.QUEST_RAM_FLAGS, accessor.getTarget().getColorFlags());
		}
	};

	static final IDataProvider<ChiseledCanopyShelfBlockEntity> CANOPY_SHELF = (data, accessor, config) -> {
		// Sensitive spawner identity is omitted from the packet itself for non-creative viewers.
		if (!config.getBoolean(WthitConstants.CANOPY_SHELF_SPAWNER)
			|| !accessor.getPlayer().isCreative()
			|| !accessor.getTarget().getBlockState().getValue(ChiseledCanopyShelfBlock.SPAWNER)) {
			return;
		}
		SpawnData spawnData = accessor.getTarget().getSpawner().getNextSpawnData();
		if (spawnData == null) {
			return;
		}
		Identifier id = Identifier.tryParse(spawnData.getEntityToSpawn().getStringOr("id", ""));
		if (id != null) {
			data.raw().putString(WthitConstants.SHELF_ENTITY, id.toString());
		}
	};

	private WthitDataProviders() {
	}
}
