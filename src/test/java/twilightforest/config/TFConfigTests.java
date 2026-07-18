package twilightforest.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TFConfigTests {
	@TempDir
	Path directory;

	@AfterEach
	void restoreDefaults() throws IOException {
		Files.deleteIfExists(this.directory.resolve(TFConfigFile.LEGACY_COMMON_FILE_NAME));
		Files.deleteIfExists(this.directory.resolve(TFConfigFile.LEGACY_CLIENT_FILE_NAME));
		TFConfig.loadCommon(this.directory.resolve("reset-common.json"));
		TFConfig.loadClient(this.directory.resolve("reset-client.json"));
	}

	@Test
	void createsDefaultFiles() {
		Path common = this.directory.resolve(TFConfigFile.COMMON_FILE_NAME);
		Path client = this.directory.resolve(TFConfigFile.CLIENT_FILE_NAME);

		TFConfig.loadCommon(common);
		TFConfig.loadClient(client);

		assertTrue(Files.isRegularFile(common));
		assertTrue(Files.isRegularFile(client));
		assertEquals(64, TFConfig.maxPortalSize);
		assertFalse(TFConfig.portalForNewPlayerSpawn);
		assertEquals(List.of("twilightforest:giant_log_to_oak_planks"), TFConfig.disableUncraftingRecipes);
		assertEquals(32, TFConfig.getClientCloudBlockPrecipitationDistance());
		assertEquals(List.of("twilightforest:glacier"), TFConfig.auroraBiomes);
		assertTrue(TFConfig.manualTravellersWingsGradualGlideDefault);
		assertTrue(TFConfig.firstPersonGloveOverlay);
	}

	@Test
	void importsLegacyTomlFilesOnceAndKeepsTheSources() throws IOException {
		Path commonToml = this.directory.resolve(TFConfigFile.LEGACY_COMMON_FILE_NAME);
		Path clientToml = this.directory.resolve(TFConfigFile.LEGACY_CLIENT_FILE_NAME);
		Path commonJson = this.directory.resolve(TFConfigFile.COMMON_FILE_NAME);
		Path clientJson = this.directory.resolve(TFConfigFile.CLIENT_FILE_NAME);
		copyFixture("legacy-common.toml", commonToml);
		copyFixture("legacy-client.toml", clientToml);

		TFConfig.loadCommon(commonJson);
		TFConfig.loadClient(clientJson);

		assertTrue(Files.isRegularFile(commonToml));
		assertTrue(Files.isRegularFile(clientToml));
		assertTrue(Files.isRegularFile(commonJson));
		assertTrue(Files.isRegularFile(clientJson));
		assertTrue(Files.readString(commonJson).contains("\"schemaVersion\": 1"));
		assertTrue(Files.readString(clientJson).contains("legacy-invalid-uuid"));

		assertTrue(TFConfig.casketUUIDLocking);
		assertTrue(TFConfig.disableSkullCandles);
		assertFalse(TFConfig.defaultItemEnchants);
		assertFalse(TFConfig.bossDropChests);
		assertEquals(TFConfig.MultiplayerFightAdjuster.MORE_LOOT_AND_HEALTH, TFConfig.multiplayerFightAdjuster);
		assertEquals(73, TFConfig.commonCloudBlockPrecipitationDistance);
		assertTrue(TFConfig.newPlayersSpawnInTF);
		assertTrue(TFConfig.portalForNewPlayerSpawn);
		assertEquals("minecraft:the_nether", TFConfig.originDimension);
		assertTrue(TFConfig.allowPortalsInOtherDimensions);
		assertEquals(3, TFConfig.portalCreationPermission);
		assertTrue(TFConfig.disablePortalCreation);
		assertFalse(TFConfig.checkPortalPlacement);
		assertFalse(TFConfig.destructivePortalLightning);
		assertFalse(TFConfig.shouldReturnPortalBeUsable);
		assertEquals("minecraft:story/root", TFConfig.portalAdvancementLock);
		assertEquals(81, TFConfig.maxPortalSize);
		assertEquals(2.5D, TFConfig.uncraftingXpCostMultiplier);
		assertEquals(3.5D, TFConfig.repairingXpCostMultiplier);
		assertEquals(List.of("twilightforest:uncrafting_table", "minecraft:crafting_table"), TFConfig.disableUncraftingRecipes);
		assertTrue(TFConfig.reverseRecipeBlacklist);
		assertEquals(List.of("minecraft", "example"), TFConfig.blacklistedUncraftingModIds);
		assertTrue(TFConfig.flipUncraftingModIdList);
		assertTrue(TFConfig.allowShapelessUncrafting);
		assertTrue(TFConfig.disableIngredientSwitching);
		assertTrue(TFConfig.disableUncraftingOnly);
		assertTrue(TFConfig.disableEntireTable);
		assertEquals(11, TFConfig.timeCoreRange);
		assertEquals(22, TFConfig.transformationCoreRange);
		assertEquals(33, TFConfig.miningCoreRange);
		assertEquals(44, TFConfig.sortingCoreRange);
		assertTrue(TFConfig.parryNonTwilightAttacks);
		assertEquals(17, TFConfig.shieldParryTicks);

		assertTrue(TFConfig.silentCicadas);
		assertTrue(TFConfig.silentCicadasOnHead);
		assertFalse(TFConfig.firstPersonEffects);
		assertFalse(TFConfig.rotateTrophyHeadsGui);
		assertTrue(TFConfig.disableOptifineNagScreen);
		assertTrue(TFConfig.disableLockedBiomeToasts);
		assertFalse(TFConfig.showQuestRamCrosshairIndicator);
		assertFalse(TFConfig.showFortificationShieldIndicator);
		assertTrue(TFConfig.showFortificationShieldIndicatorInCreative);
		assertEquals(91, TFConfig.getClientCloudBlockPrecipitationDistance());
		assertEquals(List.of(), TFConfig.giantSkinUUIDs);
		assertEquals(List.of("minecraft:the_end", "twilightforest:glacier"), TFConfig.auroraBiomes);
		assertFalse(TFConfig.prettifyOreMeterGui);
		assertTrue(TFConfig.spawnCharmAnimationAsTotem);
		assertFalse(TFConfig.manualTravellersWingsGradualGlideDefault);
		assertFalse(TFConfig.firstPersonGloveOverlay);
		assertEquals(-19, TFConfig.itemDisplayXOffs);
		assertEquals(27, TFConfig.itemDisplayYOffs);
		assertEquals(1.75D, TFConfig.itemDisplayScale);
		assertFalse(TFConfig.clock24HourFormat);

		Files.writeString(commonToml, "this is no longer valid TOML");
		Files.writeString(clientToml, "this is no longer valid TOML");
		TFConfig.loadCommon(commonJson);
		TFConfig.loadClient(clientJson);
		assertEquals(3, TFConfig.portalCreationPermission);
		assertEquals(-19, TFConfig.itemDisplayXOffs);
	}

	@Test
	void rejectsMalformedLegacyTomlWithoutCreatingJson() throws IOException {
		Path commonToml = this.directory.resolve(TFConfigFile.LEGACY_COMMON_FILE_NAME);
		Path commonJson = this.directory.resolve(TFConfigFile.COMMON_FILE_NAME);
		Files.writeString(commonToml, "[\"Portal Settings\"\nmaxPortalSize = nope");

		assertThrows(IllegalStateException.class, () -> TFConfig.loadCommon(commonJson));
		assertTrue(Files.isRegularFile(commonToml));
		assertFalse(Files.exists(commonJson));
	}

	@Test
	void appliesTravellersGearClientOptions() throws IOException {
		Path client = this.directory.resolve("travellers-client.json");
		Files.writeString(client, """
			{
			  "travellersWingsGradualGlide": false,
			  "firstPersonGloveOverlay": false,
			  "screenOffsetX": -24,
			  "screenOffsetY": 36,
			  "screenScale": 25.0,
			  "twentyFourHourFormat": false
			}
			""");

		TFConfig.loadClient(client);

		assertFalse(TFConfig.manualTravellersWingsGradualGlideDefault);
		assertFalse(TFConfig.firstPersonGloveOverlay);
		assertEquals(-24, TFConfig.itemDisplayXOffs);
		assertEquals(36, TFConfig.itemDisplayYOffs);
		assertEquals(10.0D, TFConfig.itemDisplayScale);
		assertFalse(TFConfig.clock24HourFormat);
	}

	@Test
	void acceptsLegacyLeafKeysAndAppliesBounds() throws IOException {
		Path common = this.directory.resolve("legacy-common.json");
		Files.writeString(common, """
			{
			  "portalForNewPlayer": false,
			  "portalUnlockedByAdvancement": "minecraft:story/root",
			  "portalCreationPermission": 99,
			  "maxPortalSize": 2,
			  "enableShapelessCrafting": true,
			  "flipRecipeList": true,
			  "flipIdList": true,
			  "disableUncrafting": true,
			  "disableUncraftingTable": true,
			  "timeCoreRange": 0,
			  "transformationCoreRange": 999,
			  "uncraftingXpCostMultiplier": -4,
			  "blacklistedUncraftingModIds": ["minecraft", "bad:namespace"],
			  "disableUncraftingRecipes": ["twilightforest:uncrafting_table", "not an id"]
			}
			""");

		TFConfig.loadCommon(common);

		assertFalse(TFConfig.portalForNewPlayerSpawn);
		assertEquals("minecraft:story/root", TFConfig.portalAdvancementLock);
		assertEquals(4, TFConfig.portalCreationPermission);
		assertEquals(4, TFConfig.maxPortalSize);
		assertTrue(TFConfig.allowShapelessUncrafting);
		assertTrue(TFConfig.reverseRecipeBlacklist);
		assertTrue(TFConfig.flipUncraftingModIdList);
		assertTrue(TFConfig.disableUncraftingOnly);
		assertTrue(TFConfig.disableEntireTable);
		assertTrue(TFConfig.disableTimeCore);
		assertEquals(128, TFConfig.transformationCoreRange);
		assertEquals(1.0D, TFConfig.uncraftingXpCostMultiplier);
		assertEquals(List.of("minecraft"), TFConfig.blacklistedUncraftingModIds);
		assertEquals(List.of("twilightforest:uncrafting_table"), TFConfig.disableUncraftingRecipes);
	}

	@Test
	void rejectsMalformedJson() throws IOException {
		Path common = this.directory.resolve("broken.json");
		Files.writeString(common, "{not-json");

		assertThrows(IllegalStateException.class, () -> TFConfig.loadCommon(common));
	}

	private void copyFixture(String name, Path target) throws IOException {
		try (InputStream input = Objects.requireNonNull(TFConfigTests.class.getResourceAsStream("/twilightforest/config/" + name))) {
			Files.copy(input, target);
		}
	}
}
