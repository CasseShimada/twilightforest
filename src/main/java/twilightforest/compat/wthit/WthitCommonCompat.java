package twilightforest.compat.wthit;

import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import twilightforest.block.entity.DryingRackBlockEntity;
import twilightforest.block.entity.bookshelf.ChiseledCanopyShelfBlockEntity;
import twilightforest.entity.passive.QuestRam;

/** WTHIT 20 common entrypoint. This class has no client-only imports. */
public final class WthitCommonCompat implements IWailaCommonPlugin {
	@Override
	public void register(ICommonRegistrar registrar) {
		registrar.featureConfig(WthitConstants.DRYING_RACK, false);
		registrar.featureConfig(WthitConstants.QUEST_RAM_WOOL, false);
		registrar.featureConfig(WthitConstants.CANOPY_SHELF_SPAWNER, false);

		registrar.blockData(WthitDataProviders.DRYING_RACK, DryingRackBlockEntity.class);
		registrar.entityData(WthitDataProviders.QUEST_RAM, QuestRam.class);
		registrar.blockData(WthitDataProviders.CANOPY_SHELF, ChiseledCanopyShelfBlockEntity.class);
	}
}
