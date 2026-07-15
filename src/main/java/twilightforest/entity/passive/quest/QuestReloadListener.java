package twilightforest.entity.passive.quest;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.quest.ram.QuestingRamContext;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;

import java.util.Map;

public final class QuestReloadListener extends SimpleJsonResourceReloadListener<QuestingRamContext> implements IdentifiableResourceReloadListener {
	public static final String DIRECTORY = "twilight/quests";
	static final Identifier QUESTING_RAM = TwilightForestMod.prefix("questing_ram");

	public QuestReloadListener() {
		super(QuestingRamContext.CODEC, FileToIdConverter.json(DIRECTORY));
	}

	@Override
	public Identifier getFabricId() {
		return TwilightForestMod.prefix("quests");
	}

	@Override
	protected void apply(Map<Identifier, QuestingRamContext> quests, ResourceManager resourceManager, ProfilerFiller profiler) {
		QuestingRamContext context = quests.get(QUESTING_RAM);
		if (context == null) {
			TwilightForestMod.LOGGER.error("Questing Ram quest file not found. Defaulting to fallback");
			context = QuestingRamContext.FALLBACK;
		} else {
			TwilightForestMod.LOGGER.debug("Questing Ram quest loaded");
		}

		QuestingRamCurrentContext.INSTANCE.setContext(context);
	}
}
