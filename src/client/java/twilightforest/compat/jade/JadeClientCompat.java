package twilightforest.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import twilightforest.block.ChiseledCanopyShelfBlock;
import twilightforest.block.DryingRackBlock;
import twilightforest.entity.passive.QuestRam;

public final class JadeClientCompat {
	private JadeClientCompat() {
	}

	public static void register(IWailaClientRegistration registration) {
		registration.registerBlockComponent(ChiseledBookshelfSpawnProvider.INSTANCE, ChiseledCanopyShelfBlock.class);
		registration.registerBlockComponent(DryingRackProvider.INSTANCE, DryingRackBlock.class);
		registration.registerEntityComponent(QuestRamWoolProvider.INSTANCE, QuestRam.class);
	}
}
