package twilightforest.compat.carryon;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import tschipp.carryon.api.CarryDecision;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarryOnCompatTests {
	private static final Pattern REGISTERED_ENTITY = Pattern.compile("register(?:WithEgg|NoEgg|Misc)?\\(\\\"([^\\\"]+)\\\"");

	@Test
	void harmlessAnimalsAreExplicitlyAllowedAndWorldBoundEntitiesAreDenied() {
		assertEquals(CarryDecision.ALLOW, CarryOnCompat.classifyEntity(Identifier.parse("twilightforest:boar")));
		assertEquals(CarryDecision.ALLOW, CarryOnCompat.classifyEntity(Identifier.parse("twilightforest:tiny_bird")));
		assertEquals(CarryDecision.DENY, CarryOnCompat.classifyEntity(Identifier.parse("twilightforest:quest_ram")));
		assertEquals(CarryDecision.DENY, CarryOnCompat.classifyEntity(Identifier.parse("twilightforest:naga")));
		assertEquals(CarryDecision.PASS, CarryOnCompat.classifyEntity(Identifier.parse("minecraft:cow")));
	}

	@Test
	void everyRegisteredTwilightForestEntityHasAnExplicitNonPassDefault() throws IOException {
		String source = Files.readString(Path.of("src/main/java/twilightforest/init/TFEntities.java"));
		Matcher matcher = REGISTERED_ENTITY.matcher(source);
		int registrations = 0;
		while (matcher.find()) {
			registrations++;
			Identifier id = Identifier.fromNamespaceAndPath("twilightforest", matcher.group(1));
			assertNotEquals(CarryDecision.PASS, CarryOnCompat.classifyEntity(id), id.toString());
		}
		assertTrue(registrations >= 80, "entity registry scan unexpectedly found only " + registrations);
	}
}
