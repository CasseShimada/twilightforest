package twilightforest.compat.wthit;

import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IWailaClientPlugin;
import twilightforest.block.ChiseledCanopyShelfBlock;
import twilightforest.block.DryingRackBlock;
import twilightforest.entity.passive.QuestRam;

/** WTHIT 20 client entrypoint, isolated in the Loom client source set. */
public final class WthitClientCompat implements IWailaClientPlugin {
	@Override
	public void register(IClientRegistrar registrar) {
		registrar.body(WthitTooltipProviders.DRYING_RACK, DryingRackBlock.class);
		registrar.body(WthitTooltipProviders.QUEST_RAM, QuestRam.class);
		registrar.body(WthitTooltipProviders.CANOPY_SHELF, ChiseledCanopyShelfBlock.class);
	}
}
