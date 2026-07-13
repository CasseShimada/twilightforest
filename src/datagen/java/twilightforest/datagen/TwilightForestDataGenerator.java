package twilightforest.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import twilightforest.TwilightForestMod;
import twilightforest.datagen.data.tags.BlockTagGenerator;
import twilightforest.datagen.data.tags.ItemTagGenerator;

public final class TwilightForestDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		FabricDataGenerator.Pack pack = dataGenerator.createPack();
		BlockTagGenerator blockTags = pack.addProvider(BlockTagGenerator::new);
		pack.addProvider((output, registries) -> new ItemTagGenerator(output, registries, blockTags));
	}

	@Override
	public String getEffectiveModId() {
		return TwilightForestMod.ID;
	}
}
