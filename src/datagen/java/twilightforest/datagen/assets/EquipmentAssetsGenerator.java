package twilightforest.datagen.assets;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import twilightforest.init.TFEquipmentAssetsClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class EquipmentAssetsGenerator implements DataProvider {
	private final PackOutput.PathProvider pathProvider;

	public EquipmentAssetsGenerator(FabricPackOutput output) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "equipment");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		Map<Identifier, EquipmentClientInfo> equipmentAssets = new LinkedHashMap<>();
		TFEquipmentAssetsClient.bootstrap((key, info) -> {
			EquipmentClientInfo previous = equipmentAssets.put(key.identifier(), info);
			if (previous != null) {
				throw new IllegalStateException("Duplicate equipment asset " + key.identifier());
			}
		});
		return DataProvider.saveAll(output, EquipmentClientInfo.CODEC, this.pathProvider, equipmentAssets);
	}

	@Override
	public String getName() {
		return "Twilight Forest Equipment Assets";
	}
}
