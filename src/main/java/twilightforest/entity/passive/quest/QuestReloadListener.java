package twilightforest.entity.passive.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootTable;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.quest.ram.QuestingRamContext;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;
import twilightforest.loot.TFLootTables;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class QuestReloadListener extends SimpleJsonResourceReloadListener<JsonElement> implements IdentifiableResourceReloadListener {

	public QuestReloadListener() {
		super(ExtraCodecs.JSON, FileToIdConverter.json("twilight/quests"));
	}

	@Override
	public Identifier getFabricId() {
		return TwilightForestMod.prefix("quests");
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
		boolean found = false;
		for (var entry : object.entrySet()) {
			if (entry.getKey().getPath().equals("questing_ram")) {
				QuestingRamContext context = parseQuestingRam(entry.getValue(), entry.getKey());
				if (context != null) {
					QuestingRamCurrentContext.INSTANCE.setContext(context);
					TwilightForestMod.LOGGER.debug("Questing Ram quest set by mod {}", entry.getKey().getNamespace());
					found = true;
				}
			}
		}

		if (!found) {
			TwilightForestMod.LOGGER.error("Questing Ram quest file not found. Defaulting to fallback");
			QuestingRamCurrentContext.INSTANCE.setContext(QuestingRamContext.FALLBACK);
		}
	}

	private static QuestingRamContext parseQuestingRam(JsonElement element, Identifier sourceId) {
		if (!element.isJsonObject()) {
			TwilightForestMod.LOGGER.error("Questing Ram quest file {} is not a JSON object", sourceId);
			return null;
		}

		JsonObject root = element.getAsJsonObject();
		if (!root.has("items") || !root.get("items").isJsonObject()) {
			TwilightForestMod.LOGGER.error("Questing Ram quest file {} missing items map", sourceId);
			return null;
		}

		EnumMap<DyeColor, Ingredient> items = new EnumMap<>(DyeColor.class);
		JsonObject itemsObject = root.getAsJsonObject("items");
		for (Map.Entry<String, JsonElement> entry : itemsObject.entrySet()) {
			DyeColor color = parseDyeColor(entry.getKey());
			if (color == null) {
				TwilightForestMod.LOGGER.warn("Questing Ram quest file {} has unknown color key {}", sourceId, entry.getKey());
				continue;
			}
			List<Item> parsedItems = parseItemList(entry.getValue(), sourceId, entry.getKey());
			if (!parsedItems.isEmpty()) {
				items.put(color, Ingredient.of(parsedItems.toArray(new Item[0])));
			}
		}

		if (!hasAllColors(items)) {
			TwilightForestMod.LOGGER.error("Questing Ram quest file {} is missing dye colors, defaulting to fallback", sourceId);
			return QuestingRamContext.FALLBACK;
		}

		ResourceKey<LootTable> lootTable = TFLootTables.QUESTING_RAM_REWARDS;
		if (root.has("reward") && root.get("reward").isJsonPrimitive()) {
			String rewardId = root.get("reward").getAsString();
			Identifier reward = Identifier.tryParse(rewardId);
			if (reward != null) {
				lootTable = ResourceKey.create(Registries.LOOT_TABLE, reward);
			}
		}

		return new QuestingRamContext(items, lootTable);
	}

	private static DyeColor parseDyeColor(String key) {
		String normalized = key.toLowerCase(Locale.ROOT);
		for (DyeColor color : DyeColor.values()) {
			if (color.getSerializedName().equals(normalized)) {
				return color;
			}
		}
		return null;
	}

	private static List<Item> parseItemList(JsonElement element, Identifier sourceId, String colorKey) {
		List<Item> items = new ArrayList<>();
		if (element.isJsonPrimitive()) {
			addItem(items, element.getAsString(), sourceId, colorKey);
		} else if (element.isJsonArray()) {
			JsonArray array = element.getAsJsonArray();
			for (JsonElement entry : array) {
				if (entry.isJsonPrimitive()) {
					addItem(items, entry.getAsString(), sourceId, colorKey);
				} else if (entry.isJsonObject()) {
					JsonObject obj = entry.getAsJsonObject();
					if (obj.has("item") && obj.get("item").isJsonPrimitive()) {
						addItem(items, obj.get("item").getAsString(), sourceId, colorKey);
					}
				}
			}
		} else if (element.isJsonObject()) {
			JsonObject obj = element.getAsJsonObject();
			if (obj.has("item") && obj.get("item").isJsonPrimitive()) {
				addItem(items, obj.get("item").getAsString(), sourceId, colorKey);
			} else if (obj.has("items") && obj.get("items").isJsonArray()) {
				for (JsonElement entry : obj.getAsJsonArray("items")) {
					if (entry.isJsonPrimitive()) {
						addItem(items, entry.getAsString(), sourceId, colorKey);
					}
				}
			}
		}
		return items;
	}

	private static void addItem(List<Item> items, String idString, Identifier sourceId, String colorKey) {
		Identifier id = Identifier.tryParse(idString);
		if (id == null) {
			TwilightForestMod.LOGGER.warn("Questing Ram quest file {} has invalid item id {} for {}", sourceId, idString, colorKey);
			return;
		}
		Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
		if (item.isPresent() && item.get() != Items.AIR) {
			items.add(item.get());
		} else {
			TwilightForestMod.LOGGER.warn("Questing Ram quest file {} has unknown item {} for {}", sourceId, idString, colorKey);
		}
	}

	private static boolean hasAllColors(EnumMap<DyeColor, Ingredient> items) {
		for (DyeColor color : DyeColor.values()) {
			if (!items.containsKey(color)) {
				return false;
			}
		}
		return true;
	}
}
