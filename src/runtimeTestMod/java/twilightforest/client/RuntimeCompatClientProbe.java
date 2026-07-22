package twilightforest.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.slf4j.Logger;
import twilightforest.block.ChiseledCanopyShelfBlock;
import twilightforest.block.AbstractSkullCandleBlock;
import twilightforest.block.entity.DryingRackBlockEntity;
import twilightforest.block.entity.bookshelf.ChiseledCanopyShelfBlockEntity;
import twilightforest.components.item.SkullCandles;
import twilightforest.init.TFDataComponents;
import twilightforest.init.TFDimension;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Automated client rendering/reload/chunk/dimension probe for isolated development runs. */
public final class RuntimeCompatClientProbe implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String MARKER = "[TF-RUNTIME-PROBE]";
	private static final Component EXPERIMENTAL_BACKUP_TITLE = Component.translatable("selectWorld.backupQuestion.experimental");
	private static final Component SKIP_BACKUP_AND_JOIN = Component.translatable("selectWorld.backupJoinSkipButton");
	private static final int MATRIX_X = 80;
	private static final int MATRIX_Y = 120;
	private static final int MATRIX_Z = 80;
	// The dedicated-server probe pre-generates this cross-chunk region in both dimensions.
	// Reusing it keeps dimension round trips from measuring synchronous world generation.
	private static final int TRIP_X = 32;
	private static final int TRIP_Z = 32;
	private static final BlockPos WTHIT_DRYING_RACK = new BlockPos(MATRIX_X - 5, MATRIX_Y + 1, MATRIX_Z + 2);
	private static final BlockPos WTHIT_CANOPY_SHELF = new BlockPos(MATRIX_X + 5, MATRIX_Y + 1, MATRIX_Z + 2);
	private static final List<String> SPECIAL_ITEMS = List.of(
		"twilightforest:firefly",
		"twilightforest:cicada",
		"twilightforest:moonworm",
		"twilightforest:mason_jar",
		"twilightforest:knightmetal_shield",
		"twilightforest:keepsake_casket",
		"twilightforest:skull_chest",
		"twilightforest:candelabra",
		"twilightforest:brazier",
		"twilightforest:naga_trophy",
		"twilightforest:skeleton_skull_candle",
		"twilightforest:moonworm_queen"
	);
	private static final List<String> MATRIX_BLOCKS = List.of(
		"minecraft:stone",
		"minecraft:oak_leaves",
		"minecraft:dandelion",
		"twilightforest:blue_force_field",
		"twilightforest:castle_brick",
		"twilightforest:fallen_leaves",
		"twilightforest:giant_cobblestone",
		"twilightforest:candelabra",
		"twilightforest:skull_chest",
		"twilightforest:keepsake_casket",
		"twilightforest:oak_drying_rack",
		"twilightforest:chiseled_canopy_bookshelf"
	);

	private int phase;
	private int phaseTicks;
	private int totalTicks;
	private int dimensionTrips;
	private boolean screenshotInFlight;
	private final Set<Screen> automaticallyClickedScreens = Collections.newSetFromMap(new IdentityHashMap<>());
	private boolean wthitLoaded;
	private long startedNanos;
	private String scenario;

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("twilightforest.runtimeProbe")) {
			return;
		}
		this.scenario = System.getProperty("twilightforest.runtimeScenario", "manual");
		this.wthitLoaded = FabricLoader.getInstance().isModLoaded("wthit");
		verifyExpectedMods();
		this.startedNanos = System.nanoTime();
		ScreenEvents.AFTER_INIT.register((minecraft, screen, scaledWidth, scaledHeight) -> {
			LOGGER.info("{} SCREEN client scenario={} type={} title={}", MARKER, this.scenario,
				screen.getClass().getName(), screen.getTitle().getString());
			if (screen instanceof AccessibilityOnboardingScreen) {
				ScreenEvents.afterExtract(screen).register((renderedScreen, graphics, mouseX, mouseY, tickDelta) ->
					this.clickScreenButton(renderedScreen, CommonComponents.GUI_CONTINUE, "accessibility_onboarding"));
			}
			Component proceedButton = experimentalProceedButton(screen);
			if (proceedButton != null) {
				ScreenEvents.afterExtract(screen).register((renderedScreen, graphics, mouseX, mouseY, tickDelta) ->
					this.clickScreenButton(renderedScreen, proceedButton, "experimental_features"));
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
		LOGGER.info("{} START client scenario={}", MARKER, this.scenario);
	}

	private void tick(Minecraft minecraft) {
		try {
			this.totalTicks++;
			if (this.totalTicks > 3_600) {
				throw new IllegalStateException("client probe timed out after 180 seconds");
			}
			if (minecraft.player == null || minecraft.level == null || minecraft.getSingleplayerServer() == null) {
				return;
			}

			switch (this.phase) {
				case 0 -> {
					this.phase = 1;
					prepareMatrix(minecraft);
				}
				case 1 -> {
					// Server preparation advances the phase on the client executor.
				}
				case 2 -> {
					if (waited(100)) {
						minecraft.gui.setScreen(new CreativeModeInventoryScreen(minecraft.player,
							minecraft.player.connection.enabledFeatures(), minecraft.options.operatorItemsTab().get()));
						advance(3);
					}
				}
				case 3 -> {
					if (waited(40)) {
						capture(minecraft, "creative-inventory", 4);
					}
				}
				case 4 -> {
					minecraft.gui.setScreen(null);
					advance(5);
				}
				case 5 -> {
					if (waited(60)) {
						capture(minecraft, "render-matrix-before-reload", 6);
					}
				}
				case 6 -> {
					teleport(minecraft, Level.OVERWORLD, MATRIX_X + 64.5D, MATRIX_Y + 1.0D, MATRIX_Z + 12.5D, 180.0F, 4.0F);
					advance(7);
				}
				case 7 -> {
					if (waited(100)) {
						teleport(minecraft, Level.OVERWORLD, MATRIX_X + 0.5D, MATRIX_Y + 1.0D, MATRIX_Z + 12.5D, 180.0F, 4.0F);
						advance(8);
					}
				}
				case 8 -> {
					if (waited(100)) {
						prepareDimensionPlatformAndTeleport(minecraft, TFDimension.DIMENSION_KEY);
						advance(9);
					}
				}
				case 9 -> {
					if (minecraft.level.dimension().equals(TFDimension.DIMENSION_KEY) && waited(100)) {
						prepareDimensionPlatformAndTeleport(minecraft, Level.OVERWORLD);
						advance(10);
					}
				}
				case 10 -> {
					if (minecraft.level.dimension().equals(Level.OVERWORLD) && waited(100)) {
						this.dimensionTrips++;
						if (this.dimensionTrips < 3) {
							prepareDimensionPlatformAndTeleport(minecraft, TFDimension.DIMENSION_KEY);
							advance(9);
						} else {
							teleport(minecraft, Level.OVERWORLD, MATRIX_X + 0.5D, MATRIX_Y + 1.0D, MATRIX_Z + 12.5D, 180.0F, 4.0F);
							minecraft.getWindow().setWindowed(1100, 700);
							advance(11);
						}
					}
				}
				case 11 -> {
					if (waited(60)) {
						this.phase = 12;
						minecraft.reloadResourcePacks().whenComplete((ignored, failure) -> minecraft.execute(() -> {
							if (failure != null) {
								fail(minecraft, new IllegalStateException("resource reload failed", failure));
							} else {
								advance(13);
							}
						}));
					}
				}
				case 12 -> {
					// Resource reload completion advances the phase.
				}
				case 13 -> {
					if (waited(100)) {
						minecraft.gui.setScreen(new SpecialItemGridScreen(SPECIAL_ITEMS.stream()
							.map(RuntimeCompatClientProbe::runtimeItemStack)
							.toList()));
						advance(14);
					}
				}
				case 14 -> {
					if (waited(40)) {
						capture(minecraft, "special-items-after-reload", 15);
					}
				}
				case 15 -> {
					minecraft.gui.setScreen(null);
					advance(16);
				}
				case 16 -> {
					if (waited(60)) {
						capture(minecraft, "render-matrix-after-reload", this.wthitLoaded ? 17 : 24);
					}
				}
				case 17 -> {
					teleport(minecraft, Level.OVERWORLD, MATRIX_X - 4.5D, MATRIX_Y + 1.0D, MATRIX_Z + 6.5D, 180.0F, 10.0F);
					advance(18);
				}
				case 18 -> {
					if (waited(100)) {
						requireBlockTarget(minecraft, WTHIT_DRYING_RACK, "WTHIT drying rack");
						capture(minecraft, "wthit-drying-rack", 19);
					}
				}
				case 19 -> {
					teleport(minecraft, Level.OVERWORLD, MATRIX_X + 7.5D, MATRIX_Y + 1.0D, MATRIX_Z + 6.5D, 180.0F, 8.0F);
					advance(20);
				}
				case 20 -> {
					if (waited(100)) {
						requireEntityTarget(minecraft, "twilightforest:quest_ram", "WTHIT quest ram");
						capture(minecraft, "wthit-quest-ram", 21);
					}
				}
				case 21 -> {
					teleport(minecraft, Level.OVERWORLD, MATRIX_X + 5.5D, MATRIX_Y + 1.0D, MATRIX_Z + 6.5D, 180.0F, 15.0F);
					advance(22);
				}
				case 22 -> {
					if (waited(100)) {
						requireBlockTarget(minecraft, WTHIT_CANOPY_SHELF, "WTHIT canopy shelf");
						capture(minecraft, "wthit-canopy-shelf", 24);
					}
				}
				case 24 -> finish(minecraft);
				default -> {
				}
			}
		} catch (Throwable failure) {
			fail(minecraft, failure);
		}
	}

	private void prepareMatrix(Minecraft minecraft) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		UUID playerId = minecraft.player.getUUID();
		server.execute(() -> {
			try {
				ServerPlayer player = require(server.getPlayerList().getPlayer(playerId), "integrated-server player");
				ServerLevel level = player.level();
				player.setGameMode(GameType.CREATIVE);
				for (int chunkX = (MATRIX_X - 12) >> 4; chunkX <= (MATRIX_X + 12) >> 4; chunkX++) {
					for (int chunkZ = (MATRIX_Z - 4) >> 4; chunkZ <= (MATRIX_Z + 16) >> 4; chunkZ++) {
						level.getChunk(chunkX, chunkZ);
					}
				}
				for (int x = MATRIX_X - 12; x <= MATRIX_X + 12; x++) {
					for (int z = MATRIX_Z - 4; z <= MATRIX_Z + 16; z++) {
						level.setBlockAndUpdate(new BlockPos(x, MATRIX_Y, z), Blocks.STONE.defaultBlockState());
						for (int y = MATRIX_Y + 1; y <= MATRIX_Y + 7; y++) {
							level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
						}
					}
				}

				for (int index = 0; index < MATRIX_BLOCKS.size(); index++) {
					int x = MATRIX_X - 6 + index;
					Block block = registryBlock(MATRIX_BLOCKS.get(index));
					level.setBlockAndUpdate(new BlockPos(x, MATRIX_Y + 1, MATRIX_Z), block.defaultBlockState());
				}
				preparePortalTile(level, new BlockPos(MATRIX_X - 2, MATRIX_Y + 1, MATRIX_Z + 3));

				level.setBlockAndUpdate(WTHIT_DRYING_RACK, registryBlock("twilightforest:oak_drying_rack").defaultBlockState());
				BlockEntity rackEntity = level.getBlockEntity(WTHIT_DRYING_RACK);
				if (rackEntity instanceof DryingRackBlockEntity rack) {
					rack.setTheItem(new ItemStack(Items.BEEF));
				}

				Block shelf = registryBlock("twilightforest:chiseled_canopy_bookshelf");
				level.setBlockAndUpdate(WTHIT_CANOPY_SHELF, shelf.defaultBlockState().setValue(ChiseledCanopyShelfBlock.SPAWNER, true));
				if (level.getBlockEntity(WTHIT_CANOPY_SHELF) instanceof ChiseledCanopyShelfBlockEntity shelfEntity) {
					shelfEntity.setItem(0, new ItemStack(Items.BOOK));
					EntityType<?> shelfSpawnType = BuiltInRegistries.ENTITY_TYPE
						.getOptional(Identifier.withDefaultNamespace("zombie"))
						.orElseThrow(() -> new IllegalStateException("Missing minecraft:zombie"));
					shelfEntity.setEntityId(shelfSpawnType, level.getRandom());
				}

				EntityType<?> questRamType = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse("twilightforest:quest_ram"))
					.orElseThrow(() -> new IllegalStateException("Missing twilightforest:quest_ram"));
				Entity questRam = require(questRamType.create(level, EntitySpawnReason.COMMAND), "quest ram");
				questRam.setPos(MATRIX_X + 7.5D, MATRIX_Y + 1.0D, MATRIX_Z + 2.5D);
				if (questRam instanceof Mob mob) {
					mob.setNoAi(true);
					mob.setPersistenceRequired();
				}
				level.addFreshEntity(questRam);

				for (int slot = 0; slot < SPECIAL_ITEMS.size(); slot++) {
					player.getInventory().setItem(slot, runtimeItemStack(SPECIAL_ITEMS.get(slot)));
				}
				player.inventoryMenu.broadcastChanges();
				player.teleportTo(level, MATRIX_X + 0.5D, MATRIX_Y + 1.0D, MATRIX_Z + 12.5D, Set.of(), 180.0F, 4.0F, false);
				minecraft.execute(() -> advance(2));
			} catch (Throwable failure) {
				minecraft.execute(() -> fail(minecraft, failure));
			}
		});
	}

	private static void preparePortalTile(ServerLevel level, BlockPos anchor) {
		for (int x = -1; x <= 2; x++) {
			for (int z = -1; z <= 2; z++) {
				level.setBlockAndUpdate(anchor.offset(x, 0, z), Blocks.GRASS_BLOCK.defaultBlockState());
			}
		}
		Block portal = registryBlock("twilightforest:twilight_portal");
		for (int x = 0; x < 2; x++) {
			for (int z = 0; z < 2; z++) {
				level.setBlockAndUpdate(anchor.offset(x, 0, z), portal.defaultBlockState());
			}
		}
	}

	private void prepareDimensionPlatformAndTeleport(Minecraft minecraft, ResourceKey<Level> dimension) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		UUID playerId = minecraft.player.getUUID();
		server.execute(() -> {
			ServerPlayer player = require(server.getPlayerList().getPlayer(playerId), "dimension-trip player");
			ServerLevel level = require(server.getLevel(dimension), dimension.identifier().toString());
			level.getChunk(TRIP_X >> 4, TRIP_Z >> 4);
			for (int x = TRIP_X - 3; x <= TRIP_X + 3; x++) {
				for (int z = TRIP_Z - 3; z <= TRIP_Z + 3; z++) {
					level.setBlockAndUpdate(new BlockPos(x, MATRIX_Y, z), Blocks.STONE.defaultBlockState());
					for (int y = MATRIX_Y + 1; y <= MATRIX_Y + 4; y++) {
						level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
					}
				}
			}
			player.teleportTo(level, TRIP_X + 0.5D, MATRIX_Y + 1.0D, TRIP_Z + 0.5D, Set.of(), 0.0F, 0.0F, false);
		});
	}

	private void teleport(Minecraft minecraft, ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		UUID playerId = minecraft.player.getUUID();
		server.execute(() -> {
			ServerPlayer player = require(server.getPlayerList().getPlayer(playerId), "teleport player");
			ServerLevel level = require(server.getLevel(dimension), dimension.identifier().toString());
			player.teleportTo(level, x, y, z, Set.of(), yaw, pitch, false);
		});
	}

	private void capture(Minecraft minecraft, String name, int nextPhase) {
		if (this.screenshotInFlight) {
			return;
		}
		this.screenshotInFlight = true;
		String fileName = this.scenario + "-" + name + ".png";
		Screenshot.grab(minecraft.gameDirectory, fileName, minecraft.gameRenderer.mainRenderTarget(), 1, message -> minecraft.execute(() -> {
			LOGGER.info("{} SCREENSHOT client scenario={} file={} result={}", MARKER, this.scenario, fileName, message.getString());
			this.screenshotInFlight = false;
			advance(nextPhase);
		}));
	}

	private boolean waited(int ticks) {
		return ++this.phaseTicks >= ticks;
	}

	private void advance(int nextPhase) {
		this.phase = nextPhase;
		this.phaseTicks = 0;
	}

	private void finish(Minecraft minecraft) {
		long elapsedMillis = (System.nanoTime() - this.startedNanos) / 1_000_000L;
		LOGGER.info("{} PASS client scenario={} dimension_round_trips={} elapsed_ms={}", MARKER, this.scenario, this.dimensionTrips, elapsedMillis);
		this.phase = 25;
		minecraft.stop();
	}

	private void fail(Minecraft minecraft, Throwable failure) {
		LOGGER.error("{} FAIL client scenario={} phase={}", MARKER, this.scenario, this.phase, failure);
		this.phase = 25;
		minecraft.stop();
	}

	private static Block registryBlock(String id) {
		return BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id))
			.orElseThrow(() -> new IllegalStateException("Missing block " + id));
	}

	private static Item registryItem(String id) {
		return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id))
			.orElseThrow(() -> new IllegalStateException("Missing item " + id));
	}

	private static ItemStack runtimeItemStack(String id) {
		ItemStack stack = new ItemStack(registryItem(id));
		if (id.endsWith("_skull_candle")) {
			stack.set(TFDataComponents.SKULL_CANDLES,
				new SkullCandles(AbstractSkullCandleBlock.CandleColors.PLAIN.getValue(), 1));
		}
		return stack;
	}

	private static void requireBlockTarget(Minecraft minecraft, BlockPos expected, String description) {
		if (!(minecraft.hitResult instanceof BlockHitResult hit) || !hit.getBlockPos().equals(expected)) {
			String actual = minecraft.hitResult instanceof BlockHitResult blockHit
				? blockHit.getBlockPos().toShortString()
				: String.valueOf(minecraft.hitResult);
			throw new IllegalStateException(description + " was not targeted: expected=" + expected
				+ " actual=" + actual);
		}
	}

	private static void requireEntityTarget(Minecraft minecraft, String expectedId, String description) {
		if (!(minecraft.hitResult instanceof EntityHitResult hit)) {
			throw new IllegalStateException(description + " was not targeted: actual=" + minecraft.hitResult);
		}
		String actualId = BuiltInRegistries.ENTITY_TYPE.getKey(hit.getEntity().getType()).toString();
		if (!actualId.equals(expectedId)) {
			throw new IllegalStateException(description + " targeted " + actualId + " instead of " + expectedId);
		}
	}

	private static Component experimentalProceedButton(Screen screen) {
		if (screen instanceof ConfirmExperimentalFeaturesScreen) {
			return CommonComponents.GUI_PROCEED;
		}
		if (screen instanceof BackupConfirmScreen && screen.getTitle().equals(EXPERIMENTAL_BACKUP_TITLE)) {
			return SKIP_BACKUP_AND_JOIN;
		}
		return null;
	}

	private void clickScreenButton(Screen screen, Component proceedButton, String action) {
		if (this.automaticallyClickedScreens.contains(screen)) {
			return;
		}
		for (GuiEventListener child : screen.children()) {
			if (child instanceof Button button && button.getMessage().equals(proceedButton)) {
				double clickX = button.getX() + button.getWidth() / 2.0D;
				double clickY = button.getY() + button.getHeight() / 2.0D;
				MouseButtonEvent click = new MouseButtonEvent(clickX, clickY, new MouseButtonInfo(0, 0));
				this.automaticallyClickedScreens.add(screen);
				boolean pressed = screen.mouseClicked(click, false);
				screen.mouseReleased(click);
				if (!pressed) {
					throw new IllegalStateException(action + " proceed button rejected simulated mouse click");
				}
				LOGGER.info("{} ACCEPT {} scenario={} input=mouse screen={} x={} y={}",
					MARKER, action, this.scenario, screen.getClass().getSimpleName(), clickX, clickY);
				return;
			}
		}
		throw new IllegalStateException(action + " screen has no proceed button");
	}

	private static void verifyExpectedMods() {
		String expected = System.getProperty("twilightforest.runtimeExpectedMods", "");
		Arrays.stream(expected.split(","))
			.map(String::trim)
			.filter(id -> !id.isEmpty())
			.forEach(id -> {
				if (!FabricLoader.getInstance().isModLoaded(id)) {
					throw new IllegalStateException("Expected mod is not loaded: " + id);
				}
			});
	}

	private static <T> T require(T value, String description) {
		if (value == null) {
			throw new IllegalStateException("Missing " + description);
		}
		return value;
	}

	private static final class SpecialItemGridScreen extends Screen {
		private final List<ItemStack> stacks;

		private SpecialItemGridScreen(List<ItemStack> stacks) {
			super(Component.literal("Twilight Forest special-item render probe"));
			this.stacks = List.copyOf(stacks);
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			super.extractRenderState(graphics, mouseX, mouseY, partialTick);
			int columns = 3;
			int cellWidth = Math.min(220, (this.width - 40) / columns);
			int cellHeight = 42;
			int gridWidth = cellWidth * columns;
			int startX = (this.width - gridWidth) / 2;
			int startY = Math.max(36, (this.height - (this.stacks.size() + columns - 1) / columns * cellHeight) / 2);
			graphics.centeredText(this.font, this.title, this.width / 2, startY - 24, 0xFFFFFFFF);
			for (int index = 0; index < this.stacks.size(); index++) {
				int x = startX + index % columns * cellWidth;
				int y = startY + index / columns * cellHeight;
				graphics.fill(x, y, x + cellWidth - 4, y + cellHeight - 4, 0xCC202630);
				graphics.item(this.stacks.get(index), x + 10, y + 10, index);
				graphics.text(this.font, this.stacks.get(index).getHoverName(), x + 34, y + 14, 0xFFFFFFFF, false);
			}
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}
	}
}
