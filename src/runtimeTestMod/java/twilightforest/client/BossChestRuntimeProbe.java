package twilightforest.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import twilightforest.client.renderer.TFChestSpriteIds;
import twilightforest.config.TFConfig;
import twilightforest.entity.boss.AlphaYeti;
import twilightforest.entity.boss.BaseTFBoss;
import twilightforest.entity.boss.Hydra;
import twilightforest.entity.boss.IBossLootBuffer;
import twilightforest.entity.boss.KnightPhantom;
import twilightforest.entity.boss.Lich;
import twilightforest.entity.boss.Minoshroom;
import twilightforest.entity.boss.Naga;
import twilightforest.entity.boss.PlateauBoss;
import twilightforest.entity.boss.SnowQueen;
import twilightforest.entity.boss.UrGhast;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFEntities;
import twilightforest.init.TFItems;
import twilightforest.client.renderer.block.TFChestRenderer;
import twilightforest.util.TFChestTextures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Development-run-only reproduction for Boss reward chests. This class is compiled as a Loom
 * runtime test mod and is excluded from every production and sources artifact.
 */
public final class BossChestRuntimeProbe implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String MARKER = "[TF-BOSS-CHEST-PROBE]";
	private static final Component EXPERIMENTAL_BACKUP_TITLE = Component.translatable("selectWorld.backupQuestion.experimental");
	private static final Component SKIP_BACKUP_AND_JOIN = Component.translatable("selectWorld.backupJoinSkipButton");
	private static final int FLOOR_Y = 120;
	private static final int CHEST_Y = FLOOR_Y + 1;
	private static final int CHEST_Z = 0;
	private static final BlockPos REAL_DEATH_POS = new BlockPos(10, CHEST_Y, CHEST_Z);
	private static final BlockPos NORMAL_DOUBLE_RIGHT = new BlockPos(-7, CHEST_Y, 18);
	private static final BlockPos NORMAL_DOUBLE_LEFT = NORMAL_DOUBLE_RIGHT.east();
	private static final BlockPos TRAPPED_DOUBLE_RIGHT = new BlockPos(-2, CHEST_Y, 18);
	private static final BlockPos TRAPPED_DOUBLE_LEFT = TRAPPED_DOUBLE_RIGHT.east();
	private static final List<ChestSpec> CHESTS = List.of(
		new ChestSpec("naga-twilight-oak", new BlockPos(-6, CHEST_Y, CHEST_Z), TFBlocks.TWILIGHT_OAK_CHEST),
		new ChestSpec("naga-canopy", new BlockPos(-2, CHEST_Y, CHEST_Z), TFBlocks.CANOPY_CHEST),
		new ChestSpec("hydra-mangrove", new BlockPos(2, CHEST_Y, CHEST_Z), TFBlocks.MANGROVE_CHEST),
		new ChestSpec("ur-ghast-dark", new BlockPos(6, CHEST_Y, CHEST_Z), TFBlocks.DARK_CHEST)
	);
	private static final List<ChestFamily> CHEST_FAMILIES = List.of(
		new ChestFamily(TFChestTextures.Wood.TWILIGHT_OAK, TFBlocks.TWILIGHT_OAK_CHEST, TFBlocks.TWILIGHT_OAK_TRAPPED_CHEST),
		new ChestFamily(TFChestTextures.Wood.CANOPY, TFBlocks.CANOPY_CHEST, TFBlocks.CANOPY_TRAPPED_CHEST),
		new ChestFamily(TFChestTextures.Wood.MANGROVE, TFBlocks.MANGROVE_CHEST, TFBlocks.MANGROVE_TRAPPED_CHEST),
		new ChestFamily(TFChestTextures.Wood.DARK, TFBlocks.DARK_CHEST, TFBlocks.DARK_TRAPPED_CHEST),
		new ChestFamily(TFChestTextures.Wood.TIME, TFBlocks.TIME_CHEST, TFBlocks.TIME_TRAPPED_CHEST),
		new ChestFamily(TFChestTextures.Wood.TRANSFORMATION, TFBlocks.TRANSFORMATION_CHEST, TFBlocks.TRANSFORMATION_TRAPPED_CHEST),
		new ChestFamily(TFChestTextures.Wood.MINING, TFBlocks.MINING_CHEST, TFBlocks.MINING_TRAPPED_CHEST),
		new ChestFamily(TFChestTextures.Wood.SORTING, TFBlocks.SORTING_CHEST, TFBlocks.SORTING_TRAPPED_CHEST)
	);
	private static final List<Block> PRESENTATION_ITEMS = List.of(
		TFBlocks.TWILIGHT_OAK_CHEST,
		TFBlocks.CANOPY_CHEST,
		TFBlocks.MANGROVE_CHEST,
		TFBlocks.DARK_CHEST,
		TFBlocks.TIME_CHEST,
		TFBlocks.TRANSFORMATION_CHEST,
		TFBlocks.MINING_CHEST,
		TFBlocks.SORTING_CHEST
	);

	private final Set<Screen> automaticallyClickedScreens = Collections.newSetFromMap(new IdentityHashMap<>());
	private final List<SpriteEvidence> spriteEvidence = new ArrayList<>();
	private String scenario;
	private boolean expectMissing;
	private int phase;
	private int phaseTicks;
	private int totalTicks;
	private int validatedSpriteVariants;
	private int resourceReloads;
	private boolean screenshotInFlight;
	private Naga realDeathBoss;
	private BlockPos realDeathChestPos;
	private int realDeathLootItemCount;

	@Override
	public void onInitializeClient() {
		this.scenario = System.getProperty("twilightforest.runtimeScenario", "manual");
		if (!Boolean.getBoolean("twilightforest.runtimeProbe") || !this.scenario.startsWith("boss-chest")) {
			return;
		}
		this.expectMissing = this.scenario.endsWith("before");
		ScreenEvents.AFTER_INIT.register((minecraft, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof AccessibilityOnboardingScreen) {
				ScreenEvents.afterExtract(screen).register((rendered, graphics, mouseX, mouseY, tickDelta) ->
					this.clickScreenButton(rendered, CommonComponents.GUI_CONTINUE, "accessibility_onboarding"));
			}
			Component proceed = experimentalProceedButton(screen);
			if (proceed != null) {
				ScreenEvents.afterExtract(screen).register((rendered, graphics, mouseX, mouseY, tickDelta) ->
					this.clickScreenButton(rendered, proceed, "experimental_features"));
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
		LOGGER.info("{} START scenario={} expect_missing={}", MARKER, this.scenario, this.expectMissing);
	}

	private void tick(Minecraft minecraft) {
		try {
			if (++this.totalTicks > 2_400) {
				throw new IllegalStateException("Boss chest probe timed out after 120 seconds");
			}
			if (minecraft.player == null || minecraft.level == null || minecraft.getSingleplayerServer() == null) {
				return;
			}
			switch (this.phase) {
				case 0 -> {
					this.phase = 1;
					prepareRewardChests(minecraft);
				}
				case 1 -> {
					// Integrated-server preparation advances this phase on the client executor.
				}
				case 2 -> {
					if (waited(500)) {
						this.phase = 22;
						locateRealDeathChest(minecraft);
					}
				}
				case 22 -> {
					// Integrated-server inspection advances the phase.
				}
				case 23 -> {
					advance(20);
					validateClientRenderPath(minecraft);
					capture(minecraft, "actual-boss-reward-chests", 30);
				}
				case 20 -> {
					// Screenshot capture completes asynchronously and advances the phase.
				}
				case 30 -> focusChestPresentation(minecraft);
				case 300 -> {
					// Integrated-server teleport/open events advance this phase.
				}
				case 31 -> {
					if (waited(40)) {
						this.phase = 310;
						minecraft.level.destroyBlockProgress(0x54464348, TRAPPED_DOUBLE_RIGHT, 5);
						validateClientPresentation(minecraft);
						capture(minecraft, "double-open-break-and-dropped-items", 32);
					}
				}
				case 310 -> {
					// Screenshot capture completes asynchronously and advances the phase.
				}
				case 32 -> {
					minecraft.gui.setScreen(new CreativeModeInventoryScreen(minecraft.player,
						minecraft.player.connection.enabledFeatures(), minecraft.options.operatorItemsTab().get()));
					advance(33);
				}
				case 33 -> {
					if (waited(40)) {
						capture(minecraft, "chest-items-inventory", 34);
					}
				}
				case 34 -> restoreBossChestView(minecraft);
				case 340 -> {
					// Integrated-server teleport advances this phase.
				}
				case 3 -> {
					this.phase = 4;
					minecraft.reloadResourcePacks().whenComplete((ignored, failure) -> minecraft.execute(() -> {
						if (failure != null) {
							fail(minecraft, new IllegalStateException("resource reload failed", failure));
						} else {
							this.resourceReloads++;
							advance(5);
						}
					}));
				}
				case 4 -> {
					// Resource reload completion advances the phase.
				}
				case 5 -> {
					if (waited(100)) {
						advance(21);
						validateClientRenderPath(minecraft);
						capture(minecraft, "actual-boss-reward-chests-after-reload", 6);
					}
				}
				case 21 -> {
					// Screenshot capture completes asynchronously and advances the phase.
				}
				case 6 -> finish(minecraft);
				default -> {
				}
			}
		} catch (Throwable failure) {
			fail(minecraft, failure);
		}
	}

	private void prepareRewardChests(Minecraft minecraft) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		UUID playerId = minecraft.player.getUUID();
		server.execute(() -> {
			try {
				server.setDifficulty(Difficulty.NORMAL, true);
				check(TFConfig.bossDropChests, "bossesSpawnDropChests/bossDropChests is disabled");
				ServerPlayer player = require(server.getPlayerList().getPlayer(playerId), "integrated-server player");
				ServerLevel level = player.level();
				check(level.getDifficulty() == Difficulty.NORMAL, "Boss death probe requires Normal difficulty");
				check(level.getGameRules().get(GameRules.MOB_DROPS), "mobDrops gamerule is disabled");
				if (this.scenario.contains("rejoin")) {
					verifyPersistedRewardChests(level);
				}
				player.setGameMode(GameType.CREATIVE);
				level.getEntitiesOfClass(ItemEntity.class, new AABB(-20.0D, FLOOR_Y - 4.0D, -16.0D, 21.0D, CHEST_Y + 12.0D, 33.0D))
					.forEach(ItemEntity::discard);
				for (int chunkX = -1; chunkX <= 1; chunkX++) {
					for (int chunkZ = -1; chunkZ <= 1; chunkZ++) {
						level.getChunk(chunkX, chunkZ);
					}
				}
				for (int x = -12; x <= 12; x++) {
					for (int z = -4; z <= 31; z++) {
						level.setBlockAndUpdate(new BlockPos(x, FLOOR_Y, z), Blocks.STONE.defaultBlockState());
						for (int y = CHEST_Y; y <= CHEST_Y + 6; y++) {
							BlockPos cleanupPos = new BlockPos(x, y, z);
							if (level.getBlockEntity(cleanupPos) instanceof Container container) {
								container.clearContent();
							}
							level.setBlockAndUpdate(cleanupPos, Blocks.AIR.defaultBlockState());
						}
					}
				}

				Naga naga = createBoss(TFEntities.NAGA, level, new BlockPos(-10, CHEST_Y, -8));
				Block oak = chooseNagaBranch(naga, TFBlocks.TWILIGHT_OAK_CHEST);
				Block canopy = chooseNagaBranch(naga, TFBlocks.CANOPY_CHEST);
				deposit(naga, oak, CHESTS.get(0), level, 1);
				deposit(naga, canopy, CHESTS.get(1), level, 2);
				naga.discard();

				Hydra hydra = createBoss(TFEntities.HYDRA, level, new BlockPos(-10, CHEST_Y, -8));
				deposit(hydra, hydra.getDeathContainer(RandomSource.create(300L)), CHESTS.get(2), level, 3);
				hydra.discard();

				UrGhast urGhast = createBoss(TFEntities.UR_GHAST, level, new BlockPos(-10, CHEST_Y, -8));
				deposit(urGhast, urGhast.getDeathContainer(RandomSource.create(400L)), CHESTS.get(3), level, 4);
				urGhast.discard();

				verifyPlacementAndFallbackPaths(level);
				verifyAllBossContainerBranches(level);
				prepareChestPresentation(level, player);
				this.realDeathBoss = startRealNagaDeath(level, player);
				player.teleportTo(level, 0.5D, CHEST_Y + 1.0D, 12.5D, Set.of(), 180.0F, 5.0F, false);
				minecraft.execute(() -> {
					minecraft.getWindow().setWindowed(1280, 720);
					advance(2);
				});
			} catch (Throwable failure) {
				minecraft.execute(() -> fail(minecraft, failure));
			}
		});
	}

	private static void prepareChestPresentation(ServerLevel level, ServerPlayer player) {
		BlockState normalRight = TFBlocks.TIME_CHEST.defaultBlockState()
			.setValue(ChestBlock.FACING, Direction.SOUTH)
			.setValue(ChestBlock.TYPE, ChestType.RIGHT);
		BlockState normalLeft = normalRight.setValue(ChestBlock.TYPE, ChestType.LEFT);
		level.setBlockAndUpdate(NORMAL_DOUBLE_RIGHT, normalRight);
		level.setBlockAndUpdate(NORMAL_DOUBLE_LEFT, normalLeft);

		BlockState trappedRight = TFBlocks.SORTING_TRAPPED_CHEST.defaultBlockState()
			.setValue(ChestBlock.FACING, Direction.SOUTH)
			.setValue(ChestBlock.TYPE, ChestType.RIGHT);
		BlockState trappedLeft = trappedRight.setValue(ChestBlock.TYPE, ChestType.LEFT);
		level.setBlockAndUpdate(TRAPPED_DOUBLE_RIGHT, trappedRight);
		level.setBlockAndUpdate(TRAPPED_DOUBLE_LEFT, trappedLeft);

		check(level.getBlockState(NORMAL_DOUBLE_RIGHT).getValue(ChestBlock.TYPE) == ChestType.RIGHT,
			"normal double chest lost its right half");
		check(level.getBlockState(NORMAL_DOUBLE_LEFT).getValue(ChestBlock.TYPE) == ChestType.LEFT,
			"normal double chest lost its left half");
		check(level.getBlockState(TRAPPED_DOUBLE_RIGHT).getValue(ChestBlock.TYPE) == ChestType.RIGHT,
			"trapped double chest lost its right half");
		check(level.getBlockState(TRAPPED_DOUBLE_LEFT).getValue(ChestBlock.TYPE) == ChestType.LEFT,
			"trapped double chest lost its left half");

		for (int index = 0; index < PRESENTATION_ITEMS.size(); index++) {
			Block block = PRESENTATION_ITEMS.get(index);
			player.getInventory().setItem(index, new ItemStack(block.asItem()));
			ItemEntity dropped = new ItemEntity(level, 2.0D + index * 1.25D, CHEST_Y + 0.65D, 18.5D, new ItemStack(block.asItem()));
			dropped.setDeltaMovement(Vec3.ZERO);
			dropped.setNoGravity(true);
			check(level.addFreshEntity(dropped), "could not spawn dropped chest item " + BuiltInRegistries.BLOCK.getKey(block));
		}
		player.getInventory().setSelectedSlot(0);
		player.inventoryMenu.broadcastChanges();
		LOGGER.info("{} SERVER_PRESENTATION normal_double=time trapped_double=sorting item_entities={} hotbar_items={}",
			MARKER, PRESENTATION_ITEMS.size(), PRESENTATION_ITEMS.size());
	}

	private void focusChestPresentation(Minecraft minecraft) {
		this.phase = 300;
		IntegratedServer server = minecraft.getSingleplayerServer();
		UUID playerId = minecraft.player.getUUID();
		server.execute(() -> {
			try {
				ServerPlayer player = require(server.getPlayerList().getPlayer(playerId), "presentation player");
				ServerLevel level = player.level();
				player.teleportTo(level, 0.5D, CHEST_Y + 1.0D, 26.5D, Set.of(), 180.0F, 8.0F, false);
				level.blockEvent(NORMAL_DOUBLE_RIGHT, TFBlocks.TIME_CHEST, 1, 1);
				level.blockEvent(NORMAL_DOUBLE_LEFT, TFBlocks.TIME_CHEST, 1, 1);
				minecraft.execute(() -> {
					minecraft.player.getInventory().setSelectedSlot(0);
					advance(31);
				});
			} catch (Throwable failure) {
				minecraft.execute(() -> fail(minecraft, failure));
			}
		});
	}

	private static void validateClientPresentation(Minecraft minecraft) {
		validatePresentationHalf(minecraft, NORMAL_DOUBLE_RIGHT, TFBlocks.TIME_CHEST, ChestType.RIGHT);
		validatePresentationHalf(minecraft, NORMAL_DOUBLE_LEFT, TFBlocks.TIME_CHEST, ChestType.LEFT);
		validatePresentationHalf(minecraft, TRAPPED_DOUBLE_RIGHT, TFBlocks.SORTING_TRAPPED_CHEST, ChestType.RIGHT);
		validatePresentationHalf(minecraft, TRAPPED_DOUBLE_LEFT, TFBlocks.SORTING_TRAPPED_CHEST, ChestType.LEFT);

		ChestBlockEntity normalRight = (ChestBlockEntity) require(minecraft.level.getBlockEntity(NORMAL_DOUBLE_RIGHT),
			"open normal double chest right half");
		ChestBlockEntity normalLeft = (ChestBlockEntity) require(minecraft.level.getBlockEntity(NORMAL_DOUBLE_LEFT),
			"open normal double chest left half");
		float rightOpenness = normalRight.getOpenNess(1.0F);
		float leftOpenness = normalLeft.getOpenNess(1.0F);
		check(rightOpenness > 0.5F && leftOpenness > 0.5F,
			"double chest lid did not animate open: right=" + rightOpenness + ", left=" + leftOpenness);

		check(minecraft.level.destructionProgress().containsKey(TRAPPED_DOUBLE_RIGHT.asLong()),
			"trapped double chest has no client break-stage overlay");
		List<ItemEntity> droppedItems = minecraft.level.getEntitiesOfClass(ItemEntity.class,
			new AABB(1.0D, CHEST_Y - 1.0D, 17.0D, 13.0D, CHEST_Y + 3.0D, 20.0D),
			item -> PRESENTATION_ITEMS.stream().anyMatch(block -> item.getItem().is(block.asItem())));
		check(droppedItems.size() == PRESENTATION_ITEMS.size(),
			"client tracks " + droppedItems.size() + " dropped chest items instead of " + PRESENTATION_ITEMS.size());
		for (int index = 0; index < PRESENTATION_ITEMS.size(); index++) {
			check(minecraft.player.getInventory().getItem(index).is(PRESENTATION_ITEMS.get(index).asItem()),
				"hotbar chest item mismatch at slot " + index);
		}
		check(minecraft.player.getMainHandItem().is(PRESENTATION_ITEMS.getFirst().asItem()),
			"main hand does not contain the selected custom chest item");
		LOGGER.info("{} CLIENT_PRESENTATION normal_double=left|right trapped_double=left|right open_ness_right={} open_ness_left={} break_stage=5 dropped_items={} hotbar_items={} hand_item={}",
			MARKER, rightOpenness, leftOpenness, droppedItems.size(), PRESENTATION_ITEMS.size(),
			BuiltInRegistries.ITEM.getKey(minecraft.player.getMainHandItem().getItem()));
	}

	private static void validatePresentationHalf(Minecraft minecraft, BlockPos pos, Block block, ChestType expectedType) {
		BlockState state = minecraft.level.getBlockState(pos);
		check(state.is(block) && state.getValue(ChestBlock.TYPE) == expectedType,
			"presentation chest mismatch at " + pos + ": " + state);
		BlockEntity blockEntity = require(minecraft.level.getBlockEntity(pos), "presentation chest block entity " + pos);
		BlockEntityRenderer<?, ?> renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
		check(renderer instanceof TFChestRenderer<?>,
			"presentation chest has no TFChestRenderer at " + pos);
		SpriteId sprite = require(require(TFChestRenderer.SPRITES.get(block), "presentation sprite map " + block).get(expectedType),
			"presentation sprite " + block + '/' + expectedType);
		check(!minecraft.getAtlasManager().get(sprite).contents().name().equals(MissingTextureAtlasSprite.getLocation()),
			"presentation chest resolved to missingno: " + sprite);
	}

	private void restoreBossChestView(Minecraft minecraft) {
		this.phase = 340;
		minecraft.gui.setScreen(null);
		minecraft.level.destroyBlockProgress(0x54464348, TRAPPED_DOUBLE_RIGHT, -1);
		IntegratedServer server = minecraft.getSingleplayerServer();
		UUID playerId = minecraft.player.getUUID();
		server.execute(() -> {
			try {
				ServerPlayer player = require(server.getPlayerList().getPlayer(playerId), "Boss chest view player");
				player.teleportTo(player.level(), 0.5D, CHEST_Y + 1.0D, 12.5D, Set.of(), 180.0F, 5.0F, false);
				minecraft.execute(() -> advance(3));
			} catch (Throwable failure) {
				minecraft.execute(() -> fail(minecraft, failure));
			}
		});
	}

	private static void verifyPersistedRewardChests(ServerLevel level) {
		for (int index = 0; index < CHESTS.size(); index++) {
			ChestSpec spec = CHESTS.get(index);
			BlockEntity blockEntity = require(level.getBlockEntity(spec.pos), "persisted Boss reward chest " + spec.name);
			check(level.getBlockState(spec.pos).is(spec.block), spec.name + " did not persist across rejoin");
			check(blockEntity instanceof Container container && container.getItem(0).is(Items.DIAMOND)
				&& container.getItem(0).getCount() == index + 1, spec.name + " loot did not persist across rejoin");
		}
		LOGGER.info("{} REJOIN_PERSISTENCE chests={} result=pass", MARKER, CHESTS.size());
	}

	private static Naga startRealNagaDeath(ServerLevel level, ServerPlayer player) {
		Naga naga = createBoss(TFEntities.NAGA, level, REAL_DEATH_POS);
		naga.setHealth(1.0F);
		check(naga.hurtServer(level, level.damageSources().playerAttack(player), 10.0F), "Naga rejected lethal player damage");
		naga.setDeltaMovement(Vec3.ZERO);
		check(naga.isDeadOrDying(), "Naga did not enter its normal death flow");
		int bufferedItems = 0;
		for (ItemStack stack : naga.getItemStacks()) {
			bufferedItems += stack.getCount();
		}
		check(naga.hasBufferedLoot(), "normal Naga death generated no buffered reward loot; loot_table=" + naga.getLootTable());
		LOGGER.info("{} REAL_DEATH_STARTED boss={} pos={} damage_source=player_attack loot_table={} buffered_item_count={}", MARKER,
			BuiltInRegistries.ENTITY_TYPE.getKey(naga.getType()), REAL_DEATH_POS, naga.getLootTable(), bufferedItems);
		return naga;
	}

	private void locateRealDeathChest(Minecraft minecraft) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		server.execute(() -> {
			try {
				ServerLevel level = server.overworld();
				Naga dyingBoss = require(this.realDeathBoss, "real death Boss reference");
				BlockPos finalBossPos = dyingBoss.blockPosition();
				int remainingBufferedItems = dyingBoss.getItemStacks().stream().mapToInt(ItemStack::getCount).sum();
				LOGGER.info("{} REAL_DEATH_ENTITY id={} final_pos={} removed={} tick_count={} remaining_buffered_items={}", MARKER,
					dyingBoss.getUUID(), finalBossPos, dyingBoss.isRemoved(), dyingBoss.tickCount, remainingBufferedItems);

				List<BlockPos> matches = new ArrayList<>();
				for (BlockPos candidate : BlockPos.betweenClosed(finalBossPos.offset(-8, -8, -8), finalBossPos.offset(8, 16, 8))) {
					BlockState state = level.getBlockState(candidate);
					if (!state.is(TFBlocks.TWILIGHT_OAK_CHEST) && !state.is(TFBlocks.CANOPY_CHEST)) {
						continue;
					}
					if (level.getBlockEntity(candidate) instanceof Container container && containsNagaLoot(container)) {
						matches.add(candidate.immutable());
					}
				}
				List<ItemEntity> fallbackLoot = level.getEntitiesOfClass(ItemEntity.class, new AABB(finalBossPos).inflate(12.0D),
					item -> item.getItem().is(TFItems.NAGA_SCALE) || item.getItem().is(TFItems.NAGA_TROPHY));
				check(matches.size() == 1, "normal Naga death produced " + matches.size() + " reward chests with Naga loot; final_pos="
					+ finalBossPos + ", remaining_buffered_items=" + remainingBufferedItems + ", fallback_loot_entities=" + fallbackLoot.size());
				check(fallbackLoot.isEmpty(), "normal Naga death duplicated reward loot outside its chest: " + fallbackLoot.size());
				this.realDeathChestPos = matches.getFirst();
				Container rewardChest = (Container) require(level.getBlockEntity(this.realDeathChestPos), "server Naga reward chest");
				this.realDeathLootItemCount = 0;
				for (int slot = 0; slot < rewardChest.getContainerSize(); slot++) {
					this.realDeathLootItemCount += rewardChest.getItem(slot).getCount();
				}
				check(this.realDeathLootItemCount > 0, "server Naga reward chest contains no loot");
				LOGGER.info("{} REAL_DEATH_LOCATED boss=twilightforest:naga start_pos={} final_pos={} chest_pos={} remaining_buffered_items={} chest_item_count={} fallback_loot_entities={}",
					MARKER, REAL_DEATH_POS, finalBossPos, this.realDeathChestPos, remainingBufferedItems, this.realDeathLootItemCount, fallbackLoot.size());
				minecraft.execute(() -> advance(23));
			} catch (Throwable failure) {
				minecraft.execute(() -> fail(minecraft, failure));
			}
		});
	}

	private static boolean containsNagaLoot(Container container) {
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			ItemStack stack = container.getItem(slot);
			if (stack.is(TFItems.NAGA_SCALE) || stack.is(TFItems.NAGA_TROPHY)) {
				return true;
			}
		}
		return false;
	}

	private static void verifyPlacementAndFallbackPaths(ServerLevel level) {
		BlockPos occupied = new BlockPos(9, CHEST_Y, 30);
		level.setBlockAndUpdate(occupied, TFBlocks.TWILIGHT_OAK_CHEST.defaultBlockState());
		Container existing = (Container) require(level.getBlockEntity(occupied), "occupied chest fixture");
		existing.setItem(0, new ItemStack(Items.GOLD_INGOT, 7));
		Hydra occupiedBoss = createBoss(TFEntities.HYDRA, level, occupied.offset(0, 0, -2));
		occupiedBoss.setItem(0, new ItemStack(Items.DIAMOND, 5));
		IBossLootBuffer.depositDropsIntoChest(occupiedBoss, TFBlocks.MANGROVE_CHEST.defaultBlockState(), occupied, level);
		check(existing.getItem(0).is(Items.GOLD_INGOT) && existing.getItem(0).getCount() == 7,
			"occupied container contents were overwritten");
		check(level.getBlockState(occupied.above()).is(TFBlocks.MANGROVE_CHEST), "occupied position did not search upward");
		check(!occupiedBoss.hasBufferedLoot(), "upward deposit did not consume buffered loot");
		occupiedBoss.discard();

		BlockPos blocked = new BlockPos(11, CHEST_Y, 30);
		level.setBlockAndUpdate(blocked, Blocks.OBSIDIAN.defaultBlockState());
		Hydra blockedBoss = createBoss(TFEntities.HYDRA, level, blocked.offset(0, 0, -2));
		blockedBoss.setItem(0, new ItemStack(Items.DIAMOND, 6));
		IBossLootBuffer.depositDropsIntoChest(blockedBoss, TFBlocks.MANGROVE_CHEST.defaultBlockState(), blocked, level);
		check(level.getBlockState(blocked).is(Blocks.OBSIDIAN), "non-replaceable block was destroyed");
		check(level.getBlockState(blocked.above()).is(TFBlocks.MANGROVE_CHEST), "non-replaceable position did not search upward");
		blockedBoss.discard();

		BlockPos fallback = new BlockPos(12, level.getMaxY() - 1, 30);
		level.setBlockAndUpdate(fallback, Blocks.OBSIDIAN.defaultBlockState());
		Hydra fallbackBoss = createBoss(TFEntities.HYDRA, level, fallback.offset(0, -2, -2));
		fallbackBoss.setItem(0, new ItemStack(Items.DIAMOND, 8));
		IBossLootBuffer.depositDropsIntoChest(fallbackBoss, TFBlocks.MANGROVE_CHEST.defaultBlockState(), fallback, level);
		check(!fallbackBoss.hasBufferedLoot(), "fallback drop did not consume buffered loot");
		List<ItemEntity> fallbackItems = level.getEntitiesOfClass(ItemEntity.class, new AABB(fallback).inflate(2.0D),
			item -> item.getItem().is(Items.DIAMOND) && item.getItem().getCount() == 8);
		check(fallbackItems.size() == 1,
			"failed placement did not create exactly one fallback item entity");
		fallbackItems.forEach(ItemEntity::discard);
		fallbackBoss.discard();

		BlockPos emptyLoot = new BlockPos(8, CHEST_Y, 30);
		PlateauBoss plateau = createBoss(TFEntities.PLATEAU_BOSS, level, emptyLoot.offset(0, 0, -2));
		IBossLootBuffer.depositDropsIntoChest(plateau, TFBlocks.CANOPY_CHEST.defaultBlockState(), emptyLoot, level);
		check(level.getBlockState(emptyLoot).isAir(), "empty Plateau Boss buffer fabricated a reward chest");
		plateau.discard();

		LOGGER.info("{} LOOT_PLACEMENT occupied_preserved=true upward_search=true nonreplaceable_preserved=true fallback_count=1 empty_loot_chest=false", MARKER);
	}

	private static void verifyAllBossContainerBranches(ServerLevel level) {
		assertFixedContainer(createBoss(TFEntities.HYDRA, level, BlockPos.ZERO), TFBlocks.MANGROVE_CHEST);
		assertFixedContainer(createBoss(TFEntities.MINOSHROOM, level, BlockPos.ZERO), TFBlocks.MANGROVE_CHEST);
		assertFixedContainer(createBoss(TFEntities.KNIGHT_PHANTOM, level, BlockPos.ZERO), TFBlocks.DARK_CHEST);
		assertFixedContainer(createBoss(TFEntities.UR_GHAST, level, BlockPos.ZERO), TFBlocks.DARK_CHEST);
		assertFixedContainer(createBoss(TFEntities.ALPHA_YETI, level, BlockPos.ZERO), TFBlocks.CANOPY_CHEST);
		assertFixedContainer(createBoss(TFEntities.SNOW_QUEEN, level, BlockPos.ZERO), TFBlocks.TWILIGHT_OAK_CHEST);
		assertFixedContainer(createBoss(TFEntities.PLATEAU_BOSS, level, BlockPos.ZERO), TFBlocks.CANOPY_CHEST);

		Lich lich = createBoss(TFEntities.LICH, level, BlockPos.ZERO);
		boolean oak = false;
		boolean canopy = false;
		lich.getRandom().setSeed(0L);
		for (int attempt = 0; attempt < 1_024 && !(oak && canopy); attempt++) {
			Block result = lich.getDeathContainer(RandomSource.create(attempt));
			oak |= result == TFBlocks.TWILIGHT_OAK_CHEST;
			canopy |= result == TFBlocks.CANOPY_CHEST;
		}
		check(oak && canopy, "Lich random chest selection did not expose both oak and canopy branches");
		lich.discard();
		LOGGER.info("{} BOSS_BRANCHES naga=twilight_oak|canopy lich=twilight_oak|canopy hydra=minoshroom=mangrove knight_phantom=ur_ghast=dark alpha_yeti=canopy snow_queen=twilight_oak plateau_boss=canopy(no_loot_table)", MARKER);
	}

	private static void assertFixedContainer(BaseTFBoss boss, Block expected) {
		try {
			Block actual = boss.getDeathContainer(RandomSource.create(99L));
			check(actual == expected, BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType()) + " selected "
				+ BuiltInRegistries.BLOCK.getKey(actual) + " instead of " + BuiltInRegistries.BLOCK.getKey(expected));
		} finally {
			boss.discard();
		}
	}

	private static Block chooseNagaBranch(Naga naga, Block desired) {
		RandomSource random = RandomSource.create(0L);
		for (int attempt = 0; attempt < 1_024; attempt++) {
			Block result = naga.getDeathContainer(random);
			if (result == desired) {
				return result;
			}
		}
		throw new IllegalStateException("Could not select Naga chest branch " + BuiltInRegistries.BLOCK.getKey(desired));
	}

	private static <T extends BaseTFBoss> T createBoss(net.minecraft.world.entity.EntityType<T> type, ServerLevel level, BlockPos pos) {
		T boss = require(type.create(level, EntitySpawnReason.COMMAND), BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
		boss.setNoAi(true);
		boss.setPersistenceRequired();
		boss.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
		check(level.addFreshEntity(boss), "Could not add Boss probe entity " + BuiltInRegistries.ENTITY_TYPE.getKey(type));
		return boss;
	}

	private static void deposit(BaseTFBoss boss, Block selected, ChestSpec spec, ServerLevel level, int itemCount) {
		check(selected == spec.block, spec.name + " selected " + BuiltInRegistries.BLOCK.getKey(selected)
			+ " instead of " + BuiltInRegistries.BLOCK.getKey(spec.block));
		boss.setItem(0, new ItemStack(Items.DIAMOND, itemCount));
		BlockState state = selected.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
		IBossLootBuffer.depositDropsIntoChest(boss, state, spec.pos, level);
		BlockEntity blockEntity = require(level.getBlockEntity(spec.pos), "Boss reward chest block entity at " + spec.pos);
		check(level.getBlockState(spec.pos).is(selected), spec.name + " reward block was not placed");
		check(blockEntity instanceof Container container && container.getContainerSize() == IBossLootBuffer.CONTAINER_SIZE,
			spec.name + " reward container is not 27 slots");
		check(((Container) blockEntity).getItem(0).is(Items.DIAMOND), spec.name + " reward item was not deposited");
		check(!boss.hasBufferedLoot(), spec.name + " retained loot after deposit");
		IBossLootBuffer.depositDropsIntoChest(boss, state, spec.pos, level);
		check(level.getBlockState(spec.pos.above()).isAir(), spec.name + " duplicated its reward after the buffer was consumed");
		LOGGER.info("{} SERVER_CHEST boss={} label={} block={} state={} block_entity={} slots={} item_count={}",
			MARKER,
			BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType()),
			spec.name,
			BuiltInRegistries.BLOCK.getKey(selected),
			level.getBlockState(spec.pos),
			BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()),
			((Container) blockEntity).getContainerSize(),
			((Container) blockEntity).getItem(0).getCount());
	}

	private void validateClientRenderPath(Minecraft minecraft) throws IOException {
		this.spriteEvidence.clear();
		for (ChestSpec spec : CHESTS) {
			BlockState state = minecraft.level.getBlockState(spec.pos);
			BlockEntity blockEntity = require(minecraft.level.getBlockEntity(spec.pos), "client Boss reward chest BE " + spec.name);
			check(state.is(spec.block), "client did not receive " + spec.name + " block state");
			BlockEntityRenderer<?, ?> renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
			check(renderer instanceof TFChestRenderer<?>, spec.name + " has renderer " + renderer);
			ChestType chestType = state.getValue(ChestBlock.TYPE);
			SpriteId requested = require(require(TFChestRenderer.SPRITES.get(spec.block), "sprite map " + spec.name).get(chestType),
				"sprite " + spec.name + '/' + chestType);
			TextureAtlasSprite resolved = minecraft.getAtlasManager().get(requested);
			Identifier resolvedId = resolved.contents().name();
			boolean missing = resolvedId.equals(MissingTextureAtlasSprite.getLocation());
			check(missing == this.expectMissing, spec.name + " missing=" + missing + " but scenario expects " + this.expectMissing);
			this.spriteEvidence.add(new SpriteEvidence(spec, blockEntity, chestType, requested, resolvedId, missing));
			LOGGER.info("{} CLIENT_SPRITE label={} block={} state={} block_entity={} renderer={} atlas={} requested_texture={} resolved_texture={} missing={}",
				MARKER,
				spec.name,
				BuiltInRegistries.BLOCK.getKey(spec.block),
				state,
				BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()),
				renderer.getClass().getName(),
				requested.atlasLocation(),
				requested.texture(),
				resolvedId,
				missing);
		}
		validateRealDeathChest(minecraft);
		this.validatedSpriteVariants = validateEveryChestSprite(minecraft);
		writeEvidence(minecraft.gameDirectory.toPath().resolve(this.scenario + "-sprite-evidence.json"));
	}

	private void validateRealDeathChest(Minecraft minecraft) {
		BlockPos chestPos = require(this.realDeathChestPos, "normal Naga death reward chest position");
		BlockState state = minecraft.level.getBlockState(chestPos);
		check(state.is(TFBlocks.TWILIGHT_OAK_CHEST) || state.is(TFBlocks.CANOPY_CHEST),
			"normal Naga death did not place an oak/canopy reward chest: " + state);
		BlockEntity blockEntity = require(minecraft.level.getBlockEntity(chestPos), "normal Naga death reward chest");
		check(blockEntity instanceof Container container && container.getContainerSize() == IBossLootBuffer.CONTAINER_SIZE,
			"normal Naga death reward chest is not a 27-slot container");
		check(this.realDeathLootItemCount > 0, "server-authoritative normal Naga death loot was not validated");
		BlockEntityRenderer<?, ?> renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
		check(renderer instanceof TFChestRenderer<?>, "normal Naga death reward chest has renderer " + renderer);
		LOGGER.info("{} REAL_DEATH_COMPLETE boss=twilightforest:naga pos={} block={} state={} block_entity={} slots={} server_loot_item_count={} renderer={}",
			MARKER, chestPos, BuiltInRegistries.BLOCK.getKey(state.getBlock()), state,
			BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()), ((Container) blockEntity).getContainerSize(),
			this.realDeathLootItemCount, renderer.getClass().getName());
	}

	private static int validateEveryChestSprite(Minecraft minecraft) {
		int variants = 0;
		for (ChestFamily family : CHEST_FAMILIES) {
			for (boolean trapped : new boolean[] {false, true}) {
				Block block = trapped ? family.trapped : family.normal;
				for (ChestType chestType : ChestType.values()) {
					SpriteId actual = require(require(TFChestRenderer.SPRITES.get(block), "sprite family " + family.wood).get(chestType),
						"sprite " + family.wood + '/' + trapped + '/' + chestType);
					SpriteId expected = TFChestSpriteIds.fromTexture(TFChestTextures.texture(family.wood, trapped, chestType));
					check(actual.equals(expected), "renderer sprite drift: " + actual + " != " + expected);
					Identifier resolved = minecraft.getAtlasManager().get(actual).contents().name();
					check(!resolved.equals(MissingTextureAtlasSprite.getLocation()), actual + " resolved to missingno");
					check(resolved.equals(actual.texture()), actual + " resolved to unexpected texture " + resolved);
					variants++;
				}
			}
		}
		check(variants == 48, "expected 48 chest sprite variants, got " + variants);
		LOGGER.info("{} ALL_SPRITES families={} variants={} atlas={} missing=0", MARKER,
			CHEST_FAMILIES.size(), variants, net.minecraft.client.renderer.Sheets.CHEST_SHEET);
		return variants;
	}

	private void writeEvidence(Path path) throws IOException {
		StringBuilder json = new StringBuilder();
		json.append("{\n  \"scenario\": \"").append(this.scenario).append("\",\n")
			.append("  \"boss_drop_chests\": ").append(TFConfig.bossDropChests).append(",\n")
			.append("  \"expected_missing\": ").append(this.expectMissing).append(",\n")
			.append("  \"validated_sprite_variants\": ").append(this.validatedSpriteVariants).append(",\n")
			.append("  \"real_death_loot_item_count\": ").append(this.realDeathLootItemCount).append(",\n")
			.append("  \"resource_reloads\": ").append(this.resourceReloads).append(",\n")
			.append("  \"chests\": [\n");
		for (int index = 0; index < this.spriteEvidence.size(); index++) {
			SpriteEvidence evidence = this.spriteEvidence.get(index);
			json.append("    {\"label\":\"").append(evidence.spec.name)
				.append("\",\"block\":\"").append(BuiltInRegistries.BLOCK.getKey(evidence.spec.block))
				.append("\",\"block_entity\":\"").append(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(evidence.blockEntity.getType()))
				.append("\",\"chest_type\":\"").append(evidence.chestType.getSerializedName())
				.append("\",\"atlas\":\"").append(evidence.requested.atlasLocation())
				.append("\",\"requested_texture\":\"").append(evidence.requested.texture())
				.append("\",\"resolved_texture\":\"").append(evidence.resolved)
				.append("\",\"missing\":").append(evidence.missing).append('}');
			if (index + 1 < this.spriteEvidence.size()) {
				json.append(',');
			}
			json.append('\n');
		}
		json.append("  ]\n}\n");
		Files.writeString(path, json, StandardCharsets.UTF_8);
	}

	private void capture(Minecraft minecraft, String name, int nextPhase) {
		if (this.screenshotInFlight) {
			return;
		}
		this.screenshotInFlight = true;
		String fileName = this.scenario + '-' + name + ".png";
		Screenshot.grab(minecraft.gameDirectory, fileName, minecraft.gameRenderer.mainRenderTarget(), 1, message -> minecraft.execute(() -> {
			LOGGER.info("{} SCREENSHOT scenario={} file={} result={}", MARKER, this.scenario, fileName, message.getString());
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
		LOGGER.info("{} PASS scenario={} chests={} sprite_variants={} resource_reloads={} expected_missing={}", MARKER,
			this.scenario, this.spriteEvidence.size(), this.validatedSpriteVariants, this.resourceReloads, this.expectMissing);
		this.phase = 99;
		minecraft.stop();
	}

	private void fail(Minecraft minecraft, Throwable failure) {
		LOGGER.error("{} FAIL scenario={} phase={}", MARKER, this.scenario, this.phase, failure);
		this.phase = 99;
		minecraft.stop();
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

	private void clickScreenButton(Screen screen, Component message, String action) {
		if (this.automaticallyClickedScreens.contains(screen)) {
			return;
		}
		for (GuiEventListener child : screen.children()) {
			if (child instanceof Button button && button.getMessage().equals(message)) {
				double x = button.getX() + button.getWidth() / 2.0D;
				double y = button.getY() + button.getHeight() / 2.0D;
				MouseButtonEvent click = new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0));
				this.automaticallyClickedScreens.add(screen);
				check(screen.mouseClicked(click, false), action + " proceed button rejected simulated mouse click");
				screen.mouseReleased(click);
				LOGGER.info("{} ACCEPT action={} screen={}", MARKER, action, screen.getClass().getSimpleName());
				return;
			}
		}
		throw new IllegalStateException(action + " screen has no proceed button");
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

	private static <T> T require(T value, String description) {
		if (value == null) {
			throw new IllegalStateException("Missing " + description);
		}
		return value;
	}

	private record ChestSpec(String name, BlockPos pos, Block block) {
	}

	private record ChestFamily(TFChestTextures.Wood wood, Block normal, Block trapped) {
	}

	private record SpriteEvidence(
		ChestSpec spec,
		BlockEntity blockEntity,
		ChestType chestType,
		SpriteId requested,
		Identifier resolved,
		boolean missing
	) {
	}
}
