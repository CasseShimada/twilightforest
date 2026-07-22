package twilightforest.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientModelPerformanceContractTests {
	@Test
	void dynamicModelsBuildOneQuadCollectionPerCollectedPart() throws IOException {
		for (String relative : List.of(
			"patch/PatchModel.java",
			"connected/ConnectedTextureBlockStateModel.java",
			"forcefield/ForceFieldBlockStateModel.java",
			"giantblock/GiantBlockStateModel.java")) {
			String source = Files.readString(Path.of("src/client/java/twilightforest/model/block").resolve(relative));
			int part = source.indexOf("implements BlockStateModelPart");
			int getQuads = source.indexOf("getQuads", part);
			int nextOverride = source.indexOf("@Override", getQuads + 1);
			String body = source.substring(getQuads, nextOverride < 0 ? source.length() : nextOverride);
			assertFalse(body.contains("buildQuads("), relative + " rebuilt its entire geometry for each face query");
			assertTrue(body.contains("this.quads"), relative + " did not retain the per-part quad collection");
		}
	}

	@Test
	void renderContextIsAlwaysClearedWhenTessellationThrows() throws IOException {
		String source = Files.readString(Path.of("src/client/java/twilightforest/mixin/client/ModelBlockRendererMixin.java"));
		assertTrue(source.contains("try {"));
		assertTrue(source.contains("finally {"));
		assertTrue(source.indexOf("BlockModelContext.clear()") > source.indexOf("finally {"));
	}

	@Test
	void knightmetalShieldUsesTheSpriteIdStitchedByDatagen() throws IOException {
		String renderer = Files.readString(Path.of("src/client/java/twilightforest/renderer/special/KnightmetalShieldSpecialRenderer.java"));
		String atlas = Files.readString(Path.of("src/generated/fabric/assets/minecraft/atlases/shield_patterns.json"));
		assertTrue(atlas.contains("twilightforest:entity/knightmetal_shield"));
		assertTrue(renderer.contains("new SpriteId(Sheets.SHIELD_SHEET, TwilightForestMod.prefix(\"entity/knightmetal_shield\"))"));
		assertFalse(renderer.contains("SHIELD_MAPPER.apply"), "the mapper prepends entity/shield and resolves to the missing sprite");
	}

	@Test
	void skullCandleUsesVanillaSkullGuiExtents() throws IOException {
		String renderer = Files.readString(Path.of("src/client/java/twilightforest/renderer/special/SkullCandleSpecialRenderer.java"));
		assertTrue(renderer.contains("this.model.setupAnim(modelState)"));
		assertTrue(renderer.contains("poseStack.translate(0.0F, -0.5F, 0.0F)"),
			"the vanilla skull wrapper flips positive Y, so the candle must use a negative local offset");
		assertFalse(renderer.contains("poseStack.scale(-1.0F, -1.0F, 1.0F)"),
			"world-space skull transforms make the GUI extent calculation shrink the item");
		for (String skull : List.of("skeleton", "wither_skeleton", "zombie", "creeper", "player", "piglin")) {
			String definition = Files.readString(Path.of("src/generated/resources/assets/twilightforest/items/" + skull + "_skull_candle.json"));
			assertTrue(definition.contains("\"translation\""), skull + " skull candle omitted vanilla's centering transform");
			assertTrue(definition.contains("0.5"), skull + " skull candle has no half-block centering offset");
		}
	}

	@Test
	void runtimeProbeAcceptsExperimentalWorldWarningWithoutManualInput() throws IOException {
		String probe = Files.readString(Path.of("src/runtimeTestMod/java/twilightforest/client/RuntimeCompatClientProbe.java"));
		assertTrue(probe.contains("ScreenEvents.AFTER_INIT.register"));
		assertTrue(probe.contains("ScreenEvents.afterExtract(screen).register"));
		assertTrue(probe.contains("screen instanceof AccessibilityOnboardingScreen"));
		assertTrue(probe.contains("CommonComponents.GUI_CONTINUE, \"accessibility_onboarding\""));
		assertTrue(probe.contains("screen instanceof BackupConfirmScreen && screen.getTitle().equals(EXPERIMENTAL_BACKUP_TITLE)"));
		assertTrue(probe.contains("Component.translatable(\"selectWorld.backupJoinSkipButton\")"));
		assertTrue(probe.contains("button.getMessage().equals(proceedButton)"));
		assertTrue(probe.contains("screen.mouseClicked(click, false)"));
		assertTrue(probe.contains("screen.mouseReleased(click)"));
	}
}
