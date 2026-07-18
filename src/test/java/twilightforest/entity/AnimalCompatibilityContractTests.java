package twilightforest.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimalCompatibilityContractTests {
	private static final Path ENTITY_EVENTS = Path.of("src/main/java/twilightforest/events/EntityEvents.java");
	private static final Path EVENT_HANDLERS = Path.of("src/main/java/twilightforest/events/TFEventHandlers.java");
	private static final Path ENTITY_TAG_GENERATOR = Path.of(
		"src/datagen/java/twilightforest/datagen/data/tags/EntityTypeTagGenerator.java");
	private static final Path MIXIN_CONFIG = Path.of("src/main/resources/twilightforest.mixins.json");
	private static final Path AGEABLE_ACCESSOR = Path.of(
		"src/main/java/twilightforest/mixin/accessor/AgeableMobAccessor.java");
	private static final Path CANNOT_BE_AGE_LOCKED = Path.of(
		"src/generated/fabric/data/minecraft/tags/entity_type/cannot_be_age_locked.json");

	@Test
	void loyalZombieIsGeneratedIntoTheVanillaAgeLockExclusionTag() throws IOException {
		JsonObject tag = JsonParser.parseString(Files.readString(CANNOT_BE_AGE_LOCKED)).getAsJsonObject();
		JsonArray values = tag.getAsJsonArray("values");

		long loyalZombieEntries = values.asList().stream()
			.filter(value -> value.isJsonPrimitive()
				&& "twilightforest:loyal_zombie".equals(value.getAsString()))
			.count();
		assertEquals(1, loyalZombieEntries,
			"The tag may gain other intentional exclusions, but LoyalZombie must occur exactly once");
		assertFalse(tag.has("replace") && tag.get("replace").getAsBoolean());

		String generator = Files.readString(ENTITY_TAG_GENERATOR);
		assertTrue(generator.contains("this.builder(EntityTypeTags.CANNOT_BE_AGE_LOCKED)"
			+ ".add(key(TFEntities.LOYAL_ZOMBIE));"),
			"Datagen must continue producing the runtime tag instead of relying on a hand-written artifact");
	}

	@Test
	void vanillaAgeableInteractionHasNoDuplicateGlobalCallbackOrAccessor() throws IOException {
		String handlers = Files.readString(EVENT_HANDLERS);
		String entityEvents = Files.readString(ENTITY_EVENTS);
		String mixins = Files.readString(MIXIN_CONFIG);

		assertFalse(handlers.contains("UseEntityCallback"));
		assertFalse(handlers.contains("handleGoldenDandelionUse"));
		assertFalse(entityEvents.contains("handleGoldenDandelionUse"));
		assertFalse(Files.exists(AGEABLE_ACCESSOR));
		assertFalse(mixins.contains("AgeableMobAccessor"));
	}

	@Test
	void leashCompatibilityMixinsRemainRegisteredExactlyOnce() throws IOException {
		String mixins = Files.readString(MIXIN_CONFIG);

		assertEquals(1, occurrences(mixins, "\"LeashFenceKnotEntityMixin\""));
		assertEquals(1, occurrences(mixins, "\"MobMixin\""));
	}

	private static int occurrences(String text, String needle) {
		int count = 0;
		for (int index = 0; (index = text.indexOf(needle, index)) >= 0; index += needle.length()) {
			count++;
		}
		return count;
	}
}
