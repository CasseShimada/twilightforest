package twilightforest.block;

import org.junit.jupiter.api.Test;
import twilightforest.TwilightForestMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalRulesFallbackTests {
	private static final Path MOD = Path.of("src/main/java/twilightforest/TwilightForestMod.java");
	private static final Path PORTAL = Path.of("src/main/java/twilightforest/block/TFPortalBlock.java");
	private static final Path TELEPORTER = Path.of("src/main/java/twilightforest/world/TFTeleporter.java");
	private static final Path LANDMARK_UTIL = Path.of("src/main/java/twilightforest/util/landmarks/LandmarkUtil.java");

	@Test
	void progressionCompatibilityPreservesADisabledValueFromEitherPublishedId() {
		assertTrue(TwilightForestMod.resolveEnforcedProgression(true, true));
		assertFalse(TwilightForestMod.resolveEnforcedProgression(false, true));
		assertFalse(TwilightForestMod.resolveEnforcedProgression(true, false));
		assertFalse(TwilightForestMod.resolveEnforcedProgression(false, false));
	}

	@Test
	void portalTransitionTimeDistinguishesNonPlayersSurvivalAndCreativePlayers() {
		assertEquals(0, TFPortalBlock.resolvePortalTransitionTime(false, false, 60, 0));
		assertEquals(73, TFPortalBlock.resolvePortalTransitionTime(true, false, 73, 4));
		assertEquals(4, TFPortalBlock.resolvePortalTransitionTime(true, true, 73, 4));
	}

	@Test
	void registersAndMirrorsBothProgressionIdsAndBothPortalDelayRules() throws IOException {
		String mod = compactSource(MOD);
		String landmarkUtil = compactSource(LANDMARK_UTIL);
		String portal = compactSource(PORTAL);

		assertTrue(mod.contains("buildAndRegister(prefix(\"tf_enforced_progression\"))"));
		assertTrue(mod.contains("buildAndRegister(prefix(\"twilightforest_enforced_progression\"))"));
		assertTrue(mod.contains("GameRuleBuilder.forInteger(60).minValue(0).category(GameRuleCategory.PLAYER).buildAndRegister(prefix(\"players_twilight_portal_default_delay\"))"));
		assertTrue(mod.contains("GameRuleBuilder.forInteger(0).minValue(0).category(GameRuleCategory.PLAYER).buildAndRegister(prefix(\"players_twilight_portal_creative_delay\"))"));
		assertEquals(2, occurrences(mod, "GameRuleEvents.changeCallback("));
		assertTrue(mod.contains("ServerLifecycleEvents.SERVER_STARTED.register("));
		assertTrue(mod.contains("server.getGameRules().set(publishedFabricRule,enforced,server);"));
		assertTrue(mod.contains("server.getGameRules().set(upstreamRule,enforced,server);"));
		assertTrue(landmarkUtil.contains("TwilightForestMod.ENFORCED_PROGRESSION_RULE.get()"));
		assertTrue(landmarkUtil.contains("TwilightForestMod.UPSTREAM_ENFORCED_PROGRESSION_RULE.get()"));
		assertTrue(portal.contains("TwilightForestMod.TF_PORTAL_DEFAULT_DELAY.get()"));
		assertTrue(portal.contains("TwilightForestMod.TF_PORTAL_CREATIVE_DELAY.get()"));
	}

	@Test
	void finalPortalFallbackUsesDestinationSurfaceHeightmap() throws IOException {
		String teleporter = compactSource(TELEPORTER);
		int methodStart = teleporter.indexOf("protectedstaticvoidmakePortal(");
		int methodEnd = teleporter.indexOf("protectedstaticvoidloadSurroundingArea(", methodStart);
		assertTrue(methodStart >= 0 && methodEnd > methodStart);
		String makePortal = teleporter.substring(methodStart, methodEnd);

		assertTrue(makePortal.contains("BlockPoshorizontallyScaled=BlockPos.containing(entity.getX()*getHorizontalScale(world),entity.getY(),entity.getZ()*getHorizontalScale(world));"));
		assertTrue(makePortal.contains("spot=world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,horizontallyScaled);"));
		assertTrue(makePortal.contains("makePortalAt(world,spot,locked)"));
		assertFalse(makePortal.contains("entity.getY()*yFactor"));
	}

	private static String compactSource(Path source) throws IOException {
		return Files.readString(source).replaceAll("\\s+", "");
	}

	private static int occurrences(String source, String marker) {
		return (source.length() - source.replace(marker, "").length()) / marker.length();
	}
}
