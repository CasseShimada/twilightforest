package twilightforest.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JadeCompatTests {
	private static final Path FABRIC_METADATA = Path.of("src/main/resources/fabric.mod.json");

	@Test
	void formatsDryingRackRemainingTimeWithoutDiscardingSubSecondProgress() {
		assertTranslation(JadeCompatLogic.formatDryingTime(19), "gui.twilightforest.drying_ticks", 19);
		assertTranslation(JadeCompatLogic.formatDryingTime(20), "gui.twilightforest.drying_second", 1);
		assertTranslation(JadeCompatLogic.formatDryingTime(1_200), "gui.twilightforest.drying_minute", 1);
		assertTranslation(JadeCompatLogic.formatDryingTime(1_220), "gui.twilightforest.drying_time", 1, 1);
		assertTranslation(JadeCompatLogic.formatDryingTime(2_400), "gui.twilightforest.drying_minutes", 2);
	}

	@Test
	void questRamProviderTreatsSetColorBitsAsAlreadyCollected() {
		assertTrue(JadeCompatLogic.isColorMissing(0, DyeColor.WHITE));
		assertFalse(JadeCompatLogic.isColorMissing(1 << DyeColor.WHITE.getId(), DyeColor.WHITE));
		assertFalse(JadeCompatLogic.isColorMissing(-1, DyeColor.BROWN));
		assertTrue(JadeCompatLogic.isColorMissing(1 << DyeColor.RED.getId(), DyeColor.BLUE));
	}

	@Test
	void fabricMetadataPublishesTheJadePluginEntrypoint() throws IOException {
		String metadata = Files.readString(FABRIC_METADATA).replaceAll("\\s+", "");
		assertTrue(metadata.contains("\"jade\":[\"twilightforest.compat.jade.JadeCompat\"]"));
	}

	private static void assertTranslation(Component component, String key, Object... args) {
		TranslatableContents contents = assertInstanceOf(TranslatableContents.class, component.getContents());
		assertEquals(key, contents.getKey());
		assertArrayEquals(args, contents.getArgs());
	}
}
