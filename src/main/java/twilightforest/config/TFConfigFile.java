package twilightforest.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;
import twilightforest.TwilightForestMod;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class TFConfigFile {
	static final int SCHEMA_VERSION = 1;
	static final String COMMON_FILE_NAME = "twilightforest-common.json";
	static final String CLIENT_FILE_NAME = "twilightforest-client.json";
	static final String LEGACY_COMMON_FILE_NAME = "twilightforest-common.toml";
	static final String LEGACY_CLIENT_FILE_NAME = "twilightforest-client.toml";

	private static final Gson GSON = new GsonBuilder()
		.disableHtmlEscaping()
		.setPrettyPrinting()
		.create();

	private TFConfigFile() {
	}

	static Common loadCommon(Path path) {
		return readOrCreate(path, Common::new, Common.class, LEGACY_COMMON_FILE_NAME, TFConfigFile::importCommon);
	}

	static Client loadClient(Path path) {
		return readOrCreate(path, Client::new, Client.class, LEGACY_CLIENT_FILE_NAME, TFConfigFile::importClient);
	}

	private static <T> T readOrCreate(Path path, Supplier<T> defaults, Class<T> type, String legacyFileName, LegacyImporter<T> importer) {
		try {
			Path parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}

			if (Files.notExists(path)) {
				Path legacyPath = path.resolveSibling(legacyFileName);
				T value;
				if (Files.isRegularFile(legacyPath)) {
					value = importer.read(legacyPath);
					TwilightForestMod.LOGGER.info("Imported legacy Twilight Forest configuration {} into {}", legacyPath.toAbsolutePath(), path.toAbsolutePath());
				} else {
					value = defaults.get();
				}
				writeAtomically(path, value);
				return value;
			}

			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				T value = GSON.fromJson(reader, type);
				if (value == null) {
					throw new JsonParseException("Configuration root must be a JSON object");
				}
				return value;
			}
		} catch (IOException | JsonParseException exception) {
			throw new IllegalStateException("Unable to load Twilight Forest configuration " + path.toAbsolutePath(), exception);
		}
	}

	private static Common importCommon(Path path) throws IOException {
		try {
			TomlParseResult root = parseLegacy(path);
			Common config = new Common();

			config.casketUUIDLocking = booleanValue(root, "casketUUIDLocking", config.casketUUIDLocking);
			config.disableSkullCandles = booleanValue(root, "disableSkullCandleCreation", config.disableSkullCandles);
			config.defaultItemEnchants = booleanValue(root, "showEnchantmentsOnItems", config.defaultItemEnchants);
			config.bossDropChests = booleanValue(root, "bossesSpawnDropChests", config.bossDropChests);
			config.cloudBlockPrecipitationDistance = intValue(root, "cloudBlockPrecipitationDistance", config.cloudBlockPrecipitationDistance);
			config.multiplayerFightAdjuster = enumValue(root, "multiplayerFightAdjuster", TFConfig.MultiplayerFightAdjuster.class, config.multiplayerFightAdjuster);

			TomlTable dimension = root.getTable("Dimension Settings");
			config.newPlayersSpawnInTF = booleanValue(dimension, "newPlayersSpawnInTF", config.newPlayersSpawnInTF);
			config.portalForNewPlayerSpawn = booleanValue(dimension, "portalForNewPlayer", config.portalForNewPlayerSpawn);

			TomlTable portal = root.getTable("Portal Settings");
			config.originDimension = stringValue(portal, "originDimension", config.originDimension);
			config.allowPortalsInOtherDimensions = booleanValue(portal, "allowPortalsInOtherDimensions", config.allowPortalsInOtherDimensions);
			config.portalCreationPermission = permissionValue(portal, "portalCreationPermission", config.portalCreationPermission);
			config.disablePortalCreation = booleanValue(portal, "disablePortalCreation", config.disablePortalCreation);
			config.checkPortalPlacement = booleanValue(portal, "checkPortalPlacement", config.checkPortalPlacement);
			config.destructivePortalLightning = booleanValue(portal, "destructivePortalLightning", config.destructivePortalLightning);
			config.shouldReturnPortalBeUsable = booleanValue(portal, "shouldReturnPortalBeUsable", config.shouldReturnPortalBeUsable);
			config.portalAdvancementLock = stringValue(portal, "portalUnlockedByAdvancement", config.portalAdvancementLock);
			config.maxPortalSize = intValue(portal, "maxPortalSize", config.maxPortalSize);

			TomlTable uncrafting = root.getTable("Uncrafting Table");
			config.uncraftingXpCostMultiplier = doubleValue(uncrafting, "uncraftingXpCostMultiplier", config.uncraftingXpCostMultiplier);
			config.repairingXpCostMultiplier = doubleValue(uncrafting, "repairingXpCostMultiplier", config.repairingXpCostMultiplier);
			config.disableUncraftingRecipes = stringList(uncrafting, "disableUncraftingRecipes", config.disableUncraftingRecipes);
			config.reverseRecipeBlacklist = booleanValue(uncrafting, "flipRecipeList", config.reverseRecipeBlacklist);
			config.blacklistedUncraftingModIds = stringList(uncrafting, "blacklistedUncraftingModIds", config.blacklistedUncraftingModIds);
			config.flipUncraftingModIdList = booleanValue(uncrafting, "flipIdList", config.flipUncraftingModIdList);
			config.allowShapelessUncrafting = booleanValue(uncrafting, "enableShapelessCrafting", config.allowShapelessUncrafting);
			config.disableIngredientSwitching = booleanValue(uncrafting, "disableIngredientSwitching", config.disableIngredientSwitching);
			config.disableUncraftingOnly = booleanValue(uncrafting, "disableUncrafting", config.disableUncraftingOnly);
			config.disableEntireTable = booleanValue(uncrafting, "disableUncraftingTable", config.disableEntireTable);

			TomlTable magicTrees = root.getTable("Magic Trees");
			config.timeCoreRange = intValue(magicTrees, "timeCoreRange", config.timeCoreRange);
			config.transformationCoreRange = intValue(magicTrees, "transformationCoreRange", config.transformationCoreRange);
			config.miningCoreRange = intValue(magicTrees, "miningCoreRange", config.miningCoreRange);
			config.sortingCoreRange = intValue(magicTrees, "sortingCoreRange", config.sortingCoreRange);

			TomlTable shieldParrying = root.getTable("Shield Parrying");
			config.parryNonTwilightAttacks = booleanValue(shieldParrying, "parryNonTwilightAttacks", config.parryNonTwilightAttacks);
			config.shieldParryTicks = intValue(shieldParrying, "shieldParryTicksArrow", config.shieldParryTicks);
			return config;
		} catch (RuntimeException exception) {
			throw new IOException("Invalid legacy Twilight Forest common configuration " + path.toAbsolutePath(), exception);
		}
	}

	private static Client importClient(Path path) throws IOException {
		try {
			TomlParseResult root = parseLegacy(path);
			Client config = new Client();
			config.silentCicadas = booleanValue(root, "silentCicadas", config.silentCicadas);
			config.silentCicadasOnHead = booleanValue(root, "silentCicadasOnHead", config.silentCicadasOnHead);
			config.firstPersonEffects = booleanValue(root, "screenShakingEffect", config.firstPersonEffects);
			config.rotateTrophyHeadsGui = booleanValue(root, "rotateTrophyHeadsGui", config.rotateTrophyHeadsGui);
			config.disableOptifineNagScreen = booleanValue(root, "disableOptifineNagScreen", config.disableOptifineNagScreen);
			config.disableLockedBiomeToasts = booleanValue(root, "disableLockedBiomeToasts", config.disableLockedBiomeToasts);
			config.showQuestRamCrosshairIndicator = booleanValue(root, "questRamWoolIndicator", config.showQuestRamCrosshairIndicator);
			config.showFortificationShieldIndicator = booleanValue(root, "fortificationShieldIndicator", config.showFortificationShieldIndicator);
			config.showFortificationShieldIndicatorInCreative = booleanValue(root, "fortificationShieldIndicatorInCreative", config.showFortificationShieldIndicatorInCreative);
			config.cloudBlockPrecipitationDistance = intValue(root, "cloudBlockPrecipitationDistance", config.cloudBlockPrecipitationDistance);
			config.giantSkinUUIDs = stringList(root, "giantSkinUUIDs", config.giantSkinUUIDs);
			config.auroraBiomes = stringList(root, "auroraBiomes", config.auroraBiomes);
			config.prettifyOreMeterGui = booleanValue(root, "prettifyOreMeterGui", config.prettifyOreMeterGui);
			config.spawnCharmAnimationAsTotem = booleanValue(root, "totemCharmAnimation", config.spawnCharmAnimationAsTotem);
			config.manualTravellersWingsGradualGlide = booleanValue(root, "travellersWingsGradualGlide", config.manualTravellersWingsGradualGlide);
			config.firstPersonGloveOverlay = booleanValue(root, "firstPersonGloveOverlay", config.firstPersonGloveOverlay);

			TomlTable itemDisplay = root.getTable("Item Display Modifier Settings");
			config.screenOffsetX = intValue(itemDisplay, "screenOffsetX", config.screenOffsetX);
			config.screenOffsetY = intValue(itemDisplay, "screenOffsetY", config.screenOffsetY);
			config.screenScale = doubleValue(itemDisplay, "screenScale", config.screenScale);
			config.twentyFourHourFormat = booleanValue(itemDisplay, "twentyFourHourFormat", config.twentyFourHourFormat);
			return config;
		} catch (RuntimeException exception) {
			throw new IOException("Invalid legacy Twilight Forest client configuration " + path.toAbsolutePath(), exception);
		}
	}

	private static TomlParseResult parseLegacy(Path path) throws IOException {
		TomlParseResult result = Toml.parse(path);
		if (result.hasErrors()) {
			String errors = result.errors().stream().map(Object::toString).collect(Collectors.joining("; "));
			throw new IOException("Unable to parse legacy TOML " + path.toAbsolutePath() + ": " + errors);
		}
		return result;
	}

	private static boolean booleanValue(TomlTable table, String key, boolean fallback) {
		Object value = value(table, key);
		return value == null ? fallback : requireType(value, Boolean.class, key);
	}

	private static int intValue(TomlTable table, String key, int fallback) {
		Object value = value(table, key);
		if (value == null) {
			return fallback;
		}
		if (value instanceof Long number && number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE) {
			return number.intValue();
		}
		throw invalidType(key, "32-bit integer", value);
	}

	private static double doubleValue(TomlTable table, String key, double fallback) {
		Object value = value(table, key);
		if (value == null) {
			return fallback;
		}
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		throw invalidType(key, "number", value);
	}

	private static String stringValue(TomlTable table, String key, String fallback) {
		Object value = value(table, key);
		return value == null ? fallback : requireType(value, String.class, key);
	}

	private static <E extends Enum<E>> E enumValue(TomlTable table, String key, Class<E> type, E fallback) {
		Object value = value(table, key);
		if (value == null) {
			return fallback;
		}
		String name = requireType(value, String.class, key);
		try {
			return Enum.valueOf(type, name.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid enum value for legacy TOML key " + key + ": " + name, exception);
		}
	}

	private static int permissionValue(TomlTable table, String key, int fallback) {
		Object value = value(table, key);
		if (value == null) {
			return fallback;
		}
		if (value instanceof Long number && number >= 0 && number <= 4) {
			return number.intValue();
		}
		String name = requireType(value, String.class, key).toUpperCase(Locale.ROOT);
		return switch (name) {
			case "ALL" -> 0;
			case "MODERATORS" -> 1;
			case "GAMEMASTERS" -> 2;
			case "ADMINS" -> 3;
			case "OWNERS" -> 4;
			default -> throw new IllegalArgumentException("Invalid permission level for legacy TOML key " + key + ": " + name);
		};
	}

	private static List<String> stringList(TomlTable table, String key, List<String> fallback) {
		Object value = value(table, key);
		if (value == null) {
			return fallback;
		}
		TomlArray array = requireType(value, TomlArray.class, key);
		List<String> values = new ArrayList<>(array.size());
		for (int index = 0; index < array.size(); index++) {
			values.add(requireType(array.get(index), String.class, key + "[" + index + "]"));
		}
		return values;
	}

	private static Object value(TomlTable table, String key) {
		return table == null ? null : table.get(key);
	}

	private static <T> T requireType(Object value, Class<T> type, String key) {
		if (type.isInstance(value)) {
			return type.cast(value);
		}
		throw invalidType(key, type.getSimpleName(), value);
	}

	private static IllegalArgumentException invalidType(String key, String expected, Object value) {
		return new IllegalArgumentException("Expected " + expected + " for legacy TOML key " + key + ", got " + value.getClass().getSimpleName());
	}

	@FunctionalInterface
	private interface LegacyImporter<T> {
		T read(Path path) throws IOException;
	}

	private static void writeAtomically(Path path, Object value) throws IOException {
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		Files.writeString(temporary, GSON.toJson(value) + System.lineSeparator(), StandardCharsets.UTF_8,
			StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		try {
			Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException exception) {
			Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	static final class Common {
		int schemaVersion = SCHEMA_VERSION;

		boolean casketUUIDLocking;
		@SerializedName(value = "disableSkullCandles", alternate = "disableSkullCandleCreation")
		boolean disableSkullCandles;
		@SerializedName(value = "defaultItemEnchants", alternate = "showEnchantmentsOnItems")
		boolean defaultItemEnchants = true;
		@SerializedName(value = "bossDropChests", alternate = "bossesSpawnDropChests")
		boolean bossDropChests = true;
		TFConfig.MultiplayerFightAdjuster multiplayerFightAdjuster = TFConfig.MultiplayerFightAdjuster.NONE;
		int cloudBlockPrecipitationDistance = 32;

		boolean newPlayersSpawnInTF;
		@SerializedName(value = "portalForNewPlayerSpawn", alternate = "portalForNewPlayer")
		boolean portalForNewPlayerSpawn;

		String originDimension = "minecraft:overworld";
		boolean allowPortalsInOtherDimensions;
		int portalCreationPermission;
		boolean disablePortalCreation;
		boolean checkPortalPlacement = true;
		boolean destructivePortalLightning = true;
		boolean shouldReturnPortalBeUsable = true;
		@SerializedName(value = "portalAdvancementLock", alternate = "portalUnlockedByAdvancement")
		String portalAdvancementLock = "";
		int maxPortalSize = 64;

		double uncraftingXpCostMultiplier = 1.0D;
		double repairingXpCostMultiplier = 1.0D;
		@SerializedName(value = "allowShapelessUncrafting", alternate = "enableShapelessCrafting")
		boolean allowShapelessUncrafting;
		boolean disableIngredientSwitching;
		List<String> disableUncraftingRecipes = new ArrayList<>(List.of("twilightforest:giant_log_to_oak_planks"));
		@SerializedName(value = "reverseRecipeBlacklist", alternate = "flipRecipeList")
		boolean reverseRecipeBlacklist;
		List<String> blacklistedUncraftingModIds = new ArrayList<>();
		@SerializedName(value = "flipUncraftingModIdList", alternate = "flipIdList")
		boolean flipUncraftingModIdList;
		@SerializedName(value = "disableUncraftingOnly", alternate = "disableUncrafting")
		boolean disableUncraftingOnly;
		@SerializedName(value = "disableEntireTable", alternate = "disableUncraftingTable")
		boolean disableEntireTable;

		int timeCoreRange = 16;
		int transformationCoreRange = 16;
		int miningCoreRange = 16;
		int sortingCoreRange = 16;

		boolean parryNonTwilightAttacks;
		@SerializedName(value = "shieldParryTicks", alternate = "shieldParryTicksArrow")
		int shieldParryTicks = 40;
	}

	static final class Client {
		int schemaVersion = SCHEMA_VERSION;

		boolean silentCicadas;
		boolean silentCicadasOnHead;
		@SerializedName(value = "firstPersonEffects", alternate = "screenShakingEffect")
		boolean firstPersonEffects = true;
		boolean rotateTrophyHeadsGui = true;
		boolean disableOptifineNagScreen;
		boolean disableLockedBiomeToasts;
		@SerializedName(value = "showQuestRamCrosshairIndicator", alternate = "questRamWoolIndicator")
		boolean showQuestRamCrosshairIndicator = true;
		@SerializedName(value = "showFortificationShieldIndicator", alternate = "fortificationShieldIndicator")
		boolean showFortificationShieldIndicator = true;
		@SerializedName(value = "showFortificationShieldIndicatorInCreative", alternate = "fortificationShieldIndicatorInCreative")
		boolean showFortificationShieldIndicatorInCreative;
		int cloudBlockPrecipitationDistance = -1;
		List<String> giantSkinUUIDs = new ArrayList<>();
		List<String> auroraBiomes = new ArrayList<>(List.of("twilightforest:glacier"));
		boolean prettifyOreMeterGui = true;
		@SerializedName(value = "spawnCharmAnimationAsTotem", alternate = "totemCharmAnimation")
		boolean spawnCharmAnimationAsTotem;
		@SerializedName(value = "manualTravellersWingsGradualGlide", alternate = "travellersWingsGradualGlide")
		boolean manualTravellersWingsGradualGlide = true;
		boolean firstPersonGloveOverlay = true;
		TFConfig.GiantBlockOutlineMode giantBlockOutlineMode = TFConfig.GiantBlockOutlineMode.AUTO;

		int screenOffsetX = 4;
		int screenOffsetY = 4;
		double screenScale = 1.0D;
		boolean twentyFourHourFormat = TFConfig.use24HourTimeDefault();
	}
}
