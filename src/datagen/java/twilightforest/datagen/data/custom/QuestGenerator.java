package twilightforest.datagen.data.custom;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.quest.QuestReloadListener;
import twilightforest.entity.passive.quest.ram.QuestingRamContext;

import java.util.concurrent.CompletableFuture;

public final class QuestGenerator implements DataProvider {
	private static final Identifier QUESTING_RAM = TwilightForestMod.prefix("questing_ram");

	private final PackOutput.PathProvider pathProvider;

	public QuestGenerator(FabricPackOutput output) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, QuestReloadListener.DIRECTORY);
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return DataProvider.saveStable(
			output,
			QuestingRamContext.CODEC,
			QuestingRamContext.FALLBACK,
			this.pathProvider.json(QUESTING_RAM)
		);
	}

	@Override
	public String getName() {
		return "Twilight Forest Quests";
	}
}
