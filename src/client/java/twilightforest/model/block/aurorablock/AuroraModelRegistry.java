package twilightforest.client.model.block.aurorablock;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import twilightforest.TwilightForestMod;

public final class AuroraModelRegistry {
	private static boolean registered = false;

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		ModelLoadingPlugin.register(context -> {
			for (int i = 0; i < 16; i++) {
				Identifier modelId = TwilightForestMod.prefix("block/aurora_block_" + i);
				ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(modelId::toString);
				context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(modelId));
			}
		});
	}

	private AuroraModelRegistry() {}
}
