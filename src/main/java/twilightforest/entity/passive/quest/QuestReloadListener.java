package twilightforest.entity.passive.quest;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.quest.ram.QuestingRamContext;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;

import java.util.HashMap;
import java.util.Map;

public final class QuestReloadListener extends SimplePreparableReloadListener<Map<Identifier, QuestingRamContext>> implements IdentifiableResourceReloadListener {
	public static final String DIRECTORY = "twilight/quests";
	static final Identifier QUESTING_RAM = TwilightForestMod.prefix("questing_ram");

	@Override
	public Identifier getFabricId() {
		return TwilightForestMod.prefix("quests");
	}

	@Override
	protected Map<Identifier, QuestingRamContext> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<Identifier, QuestingRamContext> quests = new HashMap<>();
		SimpleJsonResourceReloadListener.scanDirectory(
			resourceManager,
			FileToIdConverter.json(DIRECTORY),
			registryOps(),
			QuestingRamContext.CODEC,
			quests
		);
		return quests;
	}

	static DynamicOps<JsonElement> registryOps() {
		RegistryAccess registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		return registryAccess.createSerializationContext(JsonOps.INSTANCE);
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
