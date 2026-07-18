package twilightforest.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.maps.MapId;
import twilightforest.TwilightForestMod;

import java.util.Map;

public final class LegacyMapItemStackFix {
	private static final Map<Identifier, Identifier> LEGACY_FILLED_MAP_IDS = Map.of(
		TwilightForestMod.prefix("magic_map"), TwilightForestMod.prefix("filled_magic_map"),
		TwilightForestMod.prefix("maze_map"), TwilightForestMod.prefix("filled_maze_map"),
		TwilightForestMod.prefix("ore_map"), TwilightForestMod.prefix("filled_ore_map")
	);

	private LegacyMapItemStackFix() {
	}

	public static FixedStack fix(Holder<Item> item, DataComponentPatch components) {
		Identifier itemId = BuiltInRegistries.ITEM.getKey(item.value());
		Identifier targetId = targetItemId(itemId, item.components(), components);
		Holder<Item> targetItem = item;
		if (!targetId.equals(itemId)) {
			if (!BuiltInRegistries.ITEM.containsKey(targetId)) {
				TwilightForestMod.LOGGER.error("Cannot migrate legacy map item {} because target {} is not registered", itemId, targetId);
				return new FixedStack(item, components);
			}
			targetItem = BuiltInRegistries.ITEM.wrapAsHolder(BuiltInRegistries.ITEM.getValue(targetId));
		}

		return new FixedStack(targetItem, fixComponents(itemId, item.components(), components));
	}

	static Identifier targetItemId(Identifier itemId, DataComponentMap prototype, DataComponentPatch components) {
		Identifier targetId = LEGACY_FILLED_MAP_IDS.get(itemId);
		return targetId != null && hasLegacyMapReference(prototype, components) ? targetId : itemId;
	}

	static DataComponentPatch fixComponents(Identifier itemId, DataComponentMap prototype, DataComponentPatch components) {
		if (!LEGACY_FILLED_MAP_IDS.containsKey(itemId) && !LEGACY_FILLED_MAP_IDS.containsValue(itemId)) {
			return components;
		}

		CustomData customData = components.get(prototype, DataComponents.CUSTOM_DATA);
		if (customData == null) {
			return components;
		}

		CompoundTag customTag = customData.copyTag();
		Integer legacyMapId = customTag.getInt("map").orElse(null);
		if (legacyMapId == null || legacyMapId < 0) {
			return components;
		}

		customTag.remove("map");
		DataComponentPatch.SplitResult split = components.split();
		DataComponentPatch.Builder builder = DataComponentPatch.builder().set(split.added());
		split.removed().forEach(builder::remove);
		if (customTag.isEmpty()) {
			builder.remove(DataComponents.CUSTOM_DATA);
		} else {
			builder.set(DataComponents.CUSTOM_DATA, CustomData.of(customTag));
		}
		if (components.get(prototype, DataComponents.MAP_ID) == null) {
			builder.set(DataComponents.MAP_ID, new MapId(legacyMapId));
		}
		return builder.build();
	}

	private static boolean hasLegacyMapReference(DataComponentMap prototype, DataComponentPatch components) {
		if (components.get(prototype, DataComponents.MAP_ID) != null) {
			return true;
		}
		CustomData customData = components.get(prototype, DataComponents.CUSTOM_DATA);
		return customData != null && customData.copyTag().getInt("map").filter(id -> id >= 0).isPresent();
	}

	public record FixedStack(Holder<Item> item, DataComponentPatch components) {
	}
}
