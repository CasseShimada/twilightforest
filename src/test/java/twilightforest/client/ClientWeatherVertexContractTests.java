package twilightforest.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientWeatherVertexContractTests {
	private static final Path PROGRESSION_WEATHER = Path.of("src/client/java/twilightforest/renderer/TFWeatherRenderer.java");
	private static final Path CLOUD_RAIN = Path.of("src/client/java/twilightforest/event/CloudEvents.java");
	private static final String COMPLETE_ENTITY_VERTEX =
		".setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0.0F,1.0F,0.0F);";

	@Test
	void customWeatherQuadsPopulateEveryEntityTranslucentVertexElement() throws IOException {
		assertCompleteWeatherQuad(PROGRESSION_WEATHER);
		assertCompleteWeatherQuad(CLOUD_RAIN);
	}

	private static void assertCompleteWeatherQuad(Path source) throws IOException {
		String compact = Files.readString(source).replaceAll("\\s+", "");
		assertTrue(compact.contains("RenderTypes.entityTranslucent("), source + " must retain its translucent render type");
		assertEquals(4, occurrences(compact, COMPLETE_ENTITY_VERTEX),
			source + " must write overlay and normal attributes for every quad vertex");
	}

	private static int occurrences(String source, String marker) {
		return (source.length() - source.replace(marker, "").length()) / marker.length();
	}
}
