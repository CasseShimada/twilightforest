package twilightforest.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import twilightforest.TwilightForestMod;
import twilightforest.datagen.data.tags.BlockTagGenerator;

public final class TwilightForestDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		FabricDataGenerator.Pack pack = dataGenerator.createPack();
		pack.addProvider(BlockTagGenerator::new);
	}

	@Override
	public String getEffectiveModId() {
		return TwilightForestMod.ID;
	}
}
