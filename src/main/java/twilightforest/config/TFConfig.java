package twilightforest.config;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.util.PlayerHelper;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class TFConfig {

	private TFConfig() {}

	public static final String CONFIG_ID = "config." + TwilightForestMod.ID + ".";

	// Backing values for the removed config system.
	// If/when a Fabric config system is added later, it should write into these fields.
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

	// --- COMMON ---
	public static boolean casketUUIDLocking = false;
	public static boolean disableSkullCandles = false;
	public static boolean defaultItemEnchants = true;
	public static boolean bossDropChests = true;
	public static MultiplayerFightAdjuster multiplayerFightAdjuster = MultiplayerFightAdjuster.NONE;
	public static int commonCloudBlockPrecipitationDistance = 32;

	// -- Dimension --
	public static boolean newPlayersSpawnInTF = false;
	public static boolean portalForNewPlayerSpawn = true;

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
	public static List<? extends String> disableUncraftingRecipes = new ArrayList<>();
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
		if (!giantSkinUUIDs.isEmpty()) {
			Thread loader = new Thread(() -> {
				GAME_PROFILES.clear();
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
}
