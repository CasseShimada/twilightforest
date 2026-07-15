package twilightforest.init;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TFDataMapsTests {
	private static final Identifier DATA_MAP_ID = Identifier.parse("twilightforest:data_maps/test.json");
	private static final Identifier STONE_ID = Identifier.parse("minecraft:stone");
	private static final Identifier DIRT_ID = Identifier.parse("minecraft:dirt");

	@Test
	void higherPriorityResourcesOverrideIndividualEntries() throws Exception {
		TFDataMaps.ReloadedDataMap<String> dataMap = load(
			"{\"values\":{\"minecraft:stone\":\"base\",\"minecraft:dirt\":\"base\"}}",
			"{\"values\":{\"minecraft:stone\":\"override\"}}"
		);

		assertEquals("override", dataMap.get(STONE_ID));
		assertEquals("base", dataMap.get(DIRT_ID));
	}

	@Test
	void replaceDiscardsLowerPriorityEntries() throws Exception {
		TFDataMaps.ReloadedDataMap<String> dataMap = load(
			"{\"values\":{\"minecraft:stone\":\"base\",\"minecraft:dirt\":\"base\"}}",
			"{\"replace\":true,\"values\":{\"minecraft:stone\":\"override\"}}"
		);

		assertEquals("override", dataMap.get(STONE_ID));
		assertNull(dataMap.get(DIRT_ID));
	}

	private static TFDataMaps.ReloadedDataMap<String> load(String... resources) throws Exception {
		ResourceManager manager = mock(ResourceManager.class);
		List<Resource> stack = java.util.Arrays.stream(resources).map(TFDataMapsTests::resource).toList();
		when(manager.getResourceStack(DATA_MAP_ID)).thenReturn(stack);

		TFDataMaps.ReloadedDataMap<String> dataMap = new TFDataMaps.ReloadedDataMap<>(DATA_MAP_ID, Codec.STRING);
		dataMap.load(manager);
		return dataMap;
	}

	private static Resource resource(String json) {
		Resource resource = mock(Resource.class);
		try {
			when(resource.openAsReader()).thenReturn(new BufferedReader(new StringReader(json)));
		} catch (Exception e) {
			throw new AssertionError(e);
		}
		when(resource.sourcePackId()).thenReturn("test");
		return resource;
	}
}
