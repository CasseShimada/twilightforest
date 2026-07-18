package twilightforest.config;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.network.SyncUncraftingTableConfigPacket;
import twilightforest.util.PlayerHelper;

import java.net.Proxy;
import java.nio.file.Path;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class TFConfig {

	private TFConfig() {}

	public static final String CONFIG_ID = "config." + TwilightForestMod.ID + ".";

	public static String portalAdvancementLock = "";
	public static List<String> auroraBiomes = new ArrayList<>(List.of("twilightforest:glacier"));
	public static List<String> giantSkinUUIDs = new ArrayList<>();

	@Nullable
	private static Identifier portalLockingAdvancement;
	private static final List<Holder<Biome>> VALID_AURORA_BIOMES = new ArrayList<>();
	public static final List<GameProfile> GAME_PROFILES = new ArrayList<>();

	/// --- CLIENT ---
	public static boolean silentCicadas = false;
	public static boolean silentCicadasOnHead = false;
	public static boolean firstPersonEffects = true;
	public static boolean rotateTrophyHeadsGui = true;
	public static boolean disableOptifineNagScreen = false;
	public static boolean disableLockedBiomeToasts = false;
	public static boolean showQuestRamCrosshairIndicator = true;
	public static boolean showFortificationShieldIndicator = true;
	public static boolean showFortificationShieldIndicatorInCreative = false;
	private static int clientCloudBlockPrecipitationDistance = 32;
	public static boolean prettifyOreMeterGui = true;
	public static boolean spawnCharmAnimationAsTotem = false;
	public static boolean manualTravellersWingsGradualGlideDefault = true;
	public static boolean firstPersonGloveOverlay = true;
	public static GiantBlockOutlineMode giantBlockOutlineMode = GiantBlockOutlineMode.AUTO;

	// --- Item Display ---
	public static int itemDisplayXOffs = 4;
	public static int itemDisplayYOffs = 4;
	public static double itemDisplayScale = 1.0D;
	public static boolean clock24HourFormat = use24HourTimeDefault();

	// --- COMMON ---
	public static boolean casketUUIDLocking = false;
	public static boolean disableSkullCandles = false;
	public static boolean defaultItemEnchants = true;
	public static boolean bossDropChests = true;
	public static MultiplayerFightAdjuster multiplayerFightAdjuster = MultiplayerFightAdjuster.NONE;
	public static int commonCloudBlockPrecipitationDistance = 32;

	// -- Dimension --
	public static boolean newPlayersSpawnInTF = false;
	public static boolean portalForNewPlayerSpawn = false;

	// -- Portal --
	public static String originDimension = Level.OVERWORLD.identifier().toString();
	public static boolean allowPortalsInOtherDimensions = false;
	public static int portalCreationPermission = 0;
	public static boolean disablePortalCreation = false;
	public static boolean checkPortalPlacement = true;
	public static boolean destructivePortalLightning = true;
	public static boolean shouldReturnPortalBeUsable = true;
	public static int maxPortalSize = 64;

	// -- Uncrafting Table --
	public static double uncraftingXpCostMultiplier = 1.0D;
	public static double repairingXpCostMultiplier = 1.0D;
	public static boolean allowShapelessUncrafting = false;
	public static boolean disableIngredientSwitching = false;
	public static List<? extends String> disableUncraftingRecipes = new ArrayList<>(List.of("twilightforest:giant_log_to_oak_planks"));
	public static boolean reverseRecipeBlacklist = false;
	public static List<? extends String> blacklistedUncraftingModIds = new ArrayList<>();
	public static boolean flipUncraftingModIdList = false;
	public static boolean disableUncraftingOnly = false;
	public static boolean disableEntireTable = false;

	// -- Magic Trees --
	public static boolean disableTimeCore = false;
	public static int timeCoreRange = 16;
	public static boolean disableTransformationCore = false;
	public static int transformationCoreRange = 16;
	public static boolean disableMiningCore = false;
	public static int miningCoreRange = 16;
	public static boolean disableSortingCore = false;
	public static int sortingCoreRange = 16;

	// -- Shield Parrying --
	public static boolean parryNonTwilightAttacks = false;
	public static int shieldParryTicks = 40;

	public static void loadCommon() {
		loadCommon(FabricLoader.getInstance().getConfigDir().resolve(TFConfigFile.COMMON_FILE_NAME));
	}

	public static void loadClient() {
		loadClient(FabricLoader.getInstance().getConfigDir().resolve(TFConfigFile.CLIENT_FILE_NAME));
	}

	static void loadCommon(Path path) {
		applyCommon(TFConfigFile.loadCommon(path));
	}

	static void loadClient(Path path) {
		applyClient(TFConfigFile.loadClient(path));
	}

	private static void applyCommon(TFConfigFile.Common config) {
		warnForNewerSchema(config.schemaVersion, TFConfigFile.COMMON_FILE_NAME);
		casketUUIDLocking = config.casketUUIDLocking;
		disableSkullCandles = config.disableSkullCandles;
		defaultItemEnchants = config.defaultItemEnchants;
		bossDropChests = config.bossDropChests;
		multiplayerFightAdjuster = config.multiplayerFightAdjuster != null ? config.multiplayerFightAdjuster : MultiplayerFightAdjuster.NONE;
		commonCloudBlockPrecipitationDistance = Math.max(0, config.cloudBlockPrecipitationDistance);

		newPlayersSpawnInTF = config.newPlayersSpawnInTF;
		portalForNewPlayerSpawn = config.portalForNewPlayerSpawn;

		originDimension = validIdentifierOrDefault(config.originDimension, Level.OVERWORLD.identifier().toString());
		allowPortalsInOtherDimensions = config.allowPortalsInOtherDimensions;
		portalCreationPermission = Math.clamp(config.portalCreationPermission, 0, 4);
		disablePortalCreation = config.disablePortalCreation;
		checkPortalPlacement = config.checkPortalPlacement;
		destructivePortalLightning = config.destructivePortalLightning;
		shouldReturnPortalBeUsable = config.shouldReturnPortalBeUsable;
		portalAdvancementLock = validIdentifierOrDefault(config.portalAdvancementLock, "");
		maxPortalSize = Math.max(4, config.maxPortalSize);

		uncraftingXpCostMultiplier = nonNegativeFinite(config.uncraftingXpCostMultiplier, 1.0D);
		repairingXpCostMultiplier = nonNegativeFinite(config.repairingXpCostMultiplier, 1.0D);
		allowShapelessUncrafting = config.allowShapelessUncrafting;
		disableIngredientSwitching = config.disableIngredientSwitching;
		disableUncraftingRecipes = validIdentifiers(config.disableUncraftingRecipes);
		reverseRecipeBlacklist = config.reverseRecipeBlacklist;
		blacklistedUncraftingModIds = validNamespaces(config.blacklistedUncraftingModIds);
		flipUncraftingModIdList = config.flipUncraftingModIdList;
		disableUncraftingOnly = config.disableUncraftingOnly;
		disableEntireTable = config.disableEntireTable;

		timeCoreRange = Math.clamp(config.timeCoreRange, 0, 128);
		disableTimeCore = timeCoreRange == 0;
		transformationCoreRange = Math.clamp(config.transformationCoreRange, 0, 128);
		disableTransformationCore = transformationCoreRange == 0;
		miningCoreRange = Math.clamp(config.miningCoreRange, 0, 128);
		disableMiningCore = miningCoreRange == 0;
		sortingCoreRange = Math.clamp(config.sortingCoreRange, 0, 128);
		disableSortingCore = sortingCoreRange == 0;

		parryNonTwilightAttacks = config.parryNonTwilightAttacks;
		shieldParryTicks = Math.max(0, config.shieldParryTicks);
		portalLockingAdvancement = null;
	}

	private static void applyClient(TFConfigFile.Client config) {
		warnForNewerSchema(config.schemaVersion, TFConfigFile.CLIENT_FILE_NAME);
		silentCicadas = config.silentCicadas;
		silentCicadasOnHead = config.silentCicadasOnHead;
		firstPersonEffects = config.firstPersonEffects;
		rotateTrophyHeadsGui = config.rotateTrophyHeadsGui;
		disableOptifineNagScreen = config.disableOptifineNagScreen;
		disableLockedBiomeToasts = config.disableLockedBiomeToasts;
		showQuestRamCrosshairIndicator = config.showQuestRamCrosshairIndicator;
		showFortificationShieldIndicator = config.showFortificationShieldIndicator;
		showFortificationShieldIndicatorInCreative = config.showFortificationShieldIndicatorInCreative;
		clientCloudBlockPrecipitationDistance = Math.max(-1, config.cloudBlockPrecipitationDistance);
		giantSkinUUIDs = validUuids(config.giantSkinUUIDs);
		auroraBiomes = validIdentifiers(config.auroraBiomes);
		prettifyOreMeterGui = config.prettifyOreMeterGui;
		spawnCharmAnimationAsTotem = config.spawnCharmAnimationAsTotem;
		manualTravellersWingsGradualGlideDefault = config.manualTravellersWingsGradualGlide;
		firstPersonGloveOverlay = config.firstPersonGloveOverlay;
		giantBlockOutlineMode = config.giantBlockOutlineMode != null ? config.giantBlockOutlineMode : GiantBlockOutlineMode.AUTO;
		itemDisplayXOffs = config.screenOffsetX;
		itemDisplayYOffs = config.screenOffsetY;
		itemDisplayScale = Double.isFinite(config.screenScale) ? Math.clamp(config.screenScale, 0.1D, 10.0D) : 1.0D;
		clock24HourFormat = config.twentyFourHourFormat;
		VALID_AURORA_BIOMES.clear();
		reloadGiantSkins();
	}

	public static void syncUncraftingConfig(ServerPlayer player) {
		ServerPlayNetworking.send(player, new SyncUncraftingTableConfigPacket(
			uncraftingXpCostMultiplier, repairingXpCostMultiplier,
			allowShapelessUncrafting, disableIngredientSwitching,
			disableUncraftingOnly, disableEntireTable,
			disableUncraftingRecipes, reverseRecipeBlacklist,
			blacklistedUncraftingModIds, flipUncraftingModIdList));
	}

	private static void warnForNewerSchema(int schemaVersion, String fileName) {
		if (schemaVersion > TFConfigFile.SCHEMA_VERSION) {
			TwilightForestMod.LOGGER.warn("Configuration {} uses schema version {}, but this build only knows version {}", fileName, schemaVersion, TFConfigFile.SCHEMA_VERSION);
		}
	}

	private static double nonNegativeFinite(double value, double fallback) {
		return Double.isFinite(value) && value >= 0.0D ? value : fallback;
	}

	private static String validIdentifierOrDefault(@Nullable String value, String fallback) {
		return value != null && (value.isEmpty() || Identifier.tryParse(value) != null) ? value : fallback;
	}

	private static List<String> validIdentifiers(@Nullable List<String> values) {
		if (values == null) {
			return List.of();
		}
		return values.stream()
			.filter(value -> value != null && Identifier.tryParse(value) != null)
			.distinct()
			.toList();
	}

	private static List<String> validNamespaces(@Nullable List<String> values) {
		if (values == null) {
			return List.of();
		}
		return values.stream()
			.filter(value -> value != null && !value.contains(":") && Identifier.tryParse(value + ":validation") != null)
			.distinct()
			.toList();
	}

	private static List<String> validUuids(@Nullable List<String> values) {
		if (values == null) {
			return List.of();
		}
		return values.stream()
			.filter(value -> {
				try {
					UUID.fromString(value);
					return true;
				} catch (IllegalArgumentException | NullPointerException exception) {
					return false;
				}
			})
			.distinct()
			.toList();
	}

	public static int getClientCloudBlockPrecipitationDistance() {
		return clientCloudBlockPrecipitationDistance == -1 ? commonCloudBlockPrecipitationDistance : clientCloudBlockPrecipitationDistance;
	}

	@Nullable
	public static Identifier getPortalLockingAdvancement(Player player) {
		// Only run assigning logic if the config has an advancement set and the cached Identifier is null.
		if (portalLockingAdvancement == null && !portalAdvancementLock.isEmpty()) {
			Identifier lock = Identifier.tryParse(portalAdvancementLock);
			if (lock == null || PlayerHelper.getAdvancement(player, lock) == null) {
				TwilightForestMod.LOGGER.fatal("The portal locking advancement is not a valid advancement! Setting to null!");
				portalAdvancementLock = "";
			} else {
				portalLockingAdvancement = lock;
				TwilightForestMod.LOGGER.debug("Portal locking advancement reloaded. Current advancement to check for is: {}", portalLockingAdvancement);
			}
		}
		// Always return the cached identifier (can be null).
		return portalLockingAdvancement;
	}

	public static List<Holder<Biome>> getValidAuroraBiomes(RegistryAccess access) {
		if (VALID_AURORA_BIOMES.isEmpty() && !auroraBiomes.isEmpty()) {
			auroraBiomes.forEach(s -> {
				Optional<Holder<Biome>> holder = Optional.ofNullable(Identifier.tryParse(s)).flatMap(key -> access.lookupOrThrow(Registries.BIOME).get(key));
				if (holder.isEmpty()) {
					TwilightForestMod.LOGGER.warn("Biome {} in Twilight Forest's aurora biomes list is not a valid biome. Skipping!", s);
				} else {
					VALID_AURORA_BIOMES.add(holder.get());
				}
			});
		}
		return VALID_AURORA_BIOMES;
	}

	public static void reloadGiantSkins() {
		GAME_PROFILES.clear();
		if (!giantSkinUUIDs.isEmpty()) {
			Thread loader = new Thread(() -> {
				YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
				MinecraftSessionService session = service.createMinecraftSessionService();
				for (String stringUUID : giantSkinUUIDs) {
					try {
						ProfileResult result = session.fetchProfile(UUID.fromString(stringUUID), false);
						if (result != null) {
							GAME_PROFILES.add(result.profile());
						}
					} catch (IllegalArgumentException e) {
						TwilightForestMod.LOGGER.error("\"{}\" is not a valid UUID!", stringUUID);
					}
				}
			}, "TF Giant Skin Loader");
			loader.setDaemon(true);
			loader.start();
		}
	}

	public static boolean use24HourTimeDefault() {
		try {
			String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
				FormatStyle.SHORT, FormatStyle.SHORT, IsoChronology.INSTANCE, Locale.getDefault());
			return !pattern.contains("a");
		} catch (RuntimeException exception) {
			return true;
		}
	}

	public enum MultiplayerFightAdjuster {
		NONE(false, false),
		MORE_LOOT(true, false),
		MORE_HEALTH(false, true),
		MORE_LOOT_AND_HEALTH(true, true);

		private final boolean moreLoot;
		private final boolean moreHealth;

		MultiplayerFightAdjuster(boolean loot, boolean health) {
			this.moreLoot = loot;
			this.moreHealth = health;
		}

		public boolean adjustsLootRolls() {
			return this.moreLoot;
		}

		public boolean adjustsHealth() {
			return this.moreHealth;
		}

		public Component getTranslatedName() {
			return Component.translatable(CONFIG_ID + "multiplayer_fight_adjuster." + this.name().toLowerCase(Locale.ROOT));
		}
	}

	public enum GiantBlockOutlineMode {
		AUTO,
		SECONDARY,
		SAFE_LINES,
		VANILLA
	}
}
