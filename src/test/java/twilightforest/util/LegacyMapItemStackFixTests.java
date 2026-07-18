package twilightforest.util;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class LegacyMapItemStackFixTests {
	private static final int MINECRAFT_1_16_5_DATA_VERSION = 2586;
	private static final Map<Identifier, Identifier> RENAMES = Map.of(
		Identifier.parse("twilightforest:magic_map"), Identifier.parse("twilightforest:filled_magic_map"),
		Identifier.parse("twilightforest:maze_map"), Identifier.parse("twilightforest:filled_maze_map"),
		Identifier.parse("twilightforest:ore_map"), Identifier.parse("twilightforest:filled_ore_map")
	);

	@Test
	void recognizesLegacyMapComponentsWithoutChangingBlankMaps() {
		DataComponentPatch legacyCustomData = legacyMapPatch(42);
		DataComponentPatch modernMapId = DataComponentPatch.builder().set(DataComponents.MAP_ID, new MapId(7)).build();

		RENAMES.forEach((oldId, newId) -> {
			assertEquals(newId, LegacyMapItemStackFix.targetItemId(oldId, DataComponentMap.EMPTY, legacyCustomData));
			assertEquals(newId, LegacyMapItemStackFix.targetItemId(oldId, DataComponentMap.EMPTY, modernMapId));
			assertEquals(oldId, LegacyMapItemStackFix.targetItemId(oldId, DataComponentMap.EMPTY, DataComponentPatch.EMPTY));
		});
	}

	@Test
	void convertsLegacyMapTagAndPreservesOtherCustomData() {
		Identifier oldId = Identifier.parse("twilightforest:magic_map");
		DataComponentPatch fixed = LegacyMapItemStackFix.fixComponents(oldId, DataComponentMap.EMPTY, legacyMapPatch(42));

		assertEquals(42, Objects.requireNonNull(fixed.get(DataComponentMap.EMPTY, DataComponents.MAP_ID)).id());
		CustomData remaining = Objects.requireNonNull(fixed.get(DataComponentMap.EMPTY, DataComponents.CUSTOM_DATA));
		assertFalse(remaining.copyTag().contains("map"));
		assertEquals("preserved", remaining.copyTag().getStringOr("LegacyMarker", ""));
		assertSame(DataComponentPatch.EMPTY, LegacyMapItemStackFix.fixComponents(
			Identifier.parse("twilightforest:magic_map"), DataComponentMap.EMPTY, DataComponentPatch.EMPTY));
	}

	@Test
	void vanillaDfuLeavesCustomMapDataAvailableForTheModFix() throws Exception {
		SharedConstants.tryDetectVersion();
		CompoundTag legacyPlayer = TagParser.parseCompoundFully(readFixture("legacy-filled-maps-player.snbt"));
		CompoundTag updatedPlayer = DataFixTypes.PLAYER.updateToCurrentVersion(
			DataFixers.getDataFixer(), legacyPlayer, MINECRAFT_1_16_5_DATA_VERSION);
		ListTag inventory = updatedPlayer.getListOrEmpty("Inventory");

		assertEquals(3, inventory.size());
		List<Integer> expectedMapIds = List.of(42, 43, 44);
		for (int index = 0; index < inventory.size(); index++) {
			CompoundTag stack = inventory.getCompoundOrEmpty(index);
			assertEquals(RENAMES.keySet().stream().map(Identifier::toString).sorted().toList().get(index),
				stack.getStringOr("id", ""));
			CompoundTag components = stack.getCompoundOrEmpty("components");
			CompoundTag customData = components.getCompoundOrEmpty("minecraft:custom_data");
			assertEquals(expectedMapIds.get(index), customData.getInt("map").orElseThrow());
			assertFalse(components.contains("minecraft:map_id"));
		}
	}

	private static DataComponentPatch legacyMapPatch(int mapId) {
		CompoundTag customData = new CompoundTag();
		customData.putInt("map", mapId);
		customData.putString("LegacyMarker", "preserved");
		return DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(customData)).build();
	}

	private static String readFixture(String name) throws IOException {
		try (InputStream input = Objects.requireNonNull(LegacyMapItemStackFixTests.class.getResourceAsStream("/twilightforest/item/" + name))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
