package twilightforest.client.event;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialBlockRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.model.*;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.monster.silverfish.SilverfishModel;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.SkullBlock;
import twilightforest.TwilightForestMod;
import twilightforest.client.BakedMultiPartRenderers;
import twilightforest.client.MagicPaintingTextureManager;
import twilightforest.client.TextureGeneratorReloadListener;
import twilightforest.client.UncraftingScreen;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.block.aurorablock.AuroraModelRegistry;
import twilightforest.client.model.block.aurorablock.NoiseVaryingModelLoader;
import twilightforest.client.model.block.connected.ConnectedTextureModelLoader;
import twilightforest.client.model.block.forcefield.ForceFieldModelLoader;
import twilightforest.client.model.block.giantblock.GiantBlockModelLoader;
import twilightforest.client.model.block.giantblock.GiantBlockStateModel;
import twilightforest.client.model.block.patch.PatchModelLoader;
import twilightforest.client.model.armor.*;
import twilightforest.client.model.block.BrazierModel;
import twilightforest.client.model.entity.*;
import twilightforest.client.particle.*;
import twilightforest.client.properties.Experiment115Type;
import twilightforest.client.properties.MoonwormQueenPulse;
import twilightforest.client.properties.NaturalDimension;
import twilightforest.client.properties.OreMeterFlash;
import twilightforest.client.properties.PotionFlaskDamage;
import twilightforest.client.properties.PotionFlaskDosage;
import twilightforest.client.renderer.TFRenderPipelines;
import twilightforest.client.renderer.TFSimpleArmorRenderer;
import twilightforest.client.renderer.block.*;
import twilightforest.client.renderer.entity.*;
import twilightforest.client.renderer.entity.layers.IceLayer;
import twilightforest.client.renderer.entity.layers.ShieldLayer;
import twilightforest.client.renderer.special.*;
import twilightforest.client.renderer.PotionFlaskTooltipComponent;
import twilightforest.enums.BossVariant;
import twilightforest.init.*;
import twilightforest.item.BrittleFlaskItem;
import twilightforest.util.woods.TFWoodTypes;

public class RegistrationEvents {
	private static boolean optifinePresent = false;
	private static final CubeDeformation INNER_ARMOR_DEFORMATION = new CubeDeformation(0.5F);
	private static final CubeDeformation OUTER_ARMOR_DEFORMATION = new CubeDeformation(1.0F);

	public static void register() {
		detectOptifine();
		TFRenderPipelines.init();
		registerWoodTypes();
		registerRenderLayers();
		registerModelLoaders();
		registerItemModelTypes();
		registerTooltipComponents();
		ColorHandler.registerColors();
		OverlayHandler.registerOverlays();
		registerEntityRenderers();
		registerBlockEntityRenderers();
		registerLayerDefinitions();
		registerScreens();
		registerParticleFactories();
		registerReloadListeners();
		registerSpecialModels();
		registerArmorRenderers();
		registerFeatureLayers();
	}

	private static void detectOptifine() {
		try {
			Class.forName("net.optifine.Config");
			optifinePresent = true;
		} catch (ClassNotFoundException e) {
			optifinePresent = false;
		}
	}

	private static void registerWoodTypes() {
		registerWoodType(TFWoodTypes.TWILIGHT_OAK_WOOD_TYPE);
		registerWoodType(TFWoodTypes.CANOPY_WOOD_TYPE);
		registerWoodType(TFWoodTypes.MANGROVE_WOOD_TYPE);
		registerWoodType(TFWoodTypes.DARK_WOOD_TYPE);
		registerWoodType(TFWoodTypes.TIME_WOOD_TYPE);
		registerWoodType(TFWoodTypes.TRANSFORMATION_WOOD_TYPE);
		registerWoodType(TFWoodTypes.MINING_WOOD_TYPE);
		registerWoodType(TFWoodTypes.SORTING_WOOD_TYPE);
	}

	private static void registerWoodType(WoodType woodType) {
		Identifier id = Identifier.parse(woodType.name());
		Sheets.SIGN_MATERIALS.put(woodType, Sheets.SIGN_MAPPER.apply(id));
		Sheets.HANGING_SIGN_MATERIALS.put(woodType, Sheets.HANGING_SIGN_MAPPER.apply(id));
	}

	private static void registerRenderLayers() {
		BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
			TFBlocks.VANISHING_BLOCK.get(),
			TFBlocks.UNBREAKABLE_VANISHING_BLOCK.get(),
			TFBlocks.LOCKED_VANISHING_BLOCK.get(),
			TFBlocks.REAPPEARING_BLOCK.get(),
			TFBlocks.CARMINITE_BUILDER.get(),
			TFBlocks.ANTIBUILDER.get(),
			TFBlocks.CARMINITE_REACTOR.get(),
			TFBlocks.CARMINITE_BLOCK.get(),
			TFBlocks.GHAST_TRAP.get(),
			TFBlocks.TORCHBERRY_PLANT.get(),
			TFBlocks.MUSHGLOOM.get(),
			TFBlocks.FIDDLEHEAD.get(),
			TFBlocks.POTTED_FIDDLEHEAD.get(),
			TFBlocks.POTTED_MUSHGLOOM.get(),
			TFBlocks.ROPE.get()
		);
	}

	private static void registerModelLoaders() {
		CustomUnbakedBlockStateModel.register(TwilightForestMod.prefix("giant_block"), GiantBlockStateModel.Unbaked.MAP_CODEC);
		UnbakedModelDeserializer.register(TwilightForestMod.prefix("connected_texture_block"), ConnectedTextureModelLoader.INSTANCE);
		UnbakedModelDeserializer.register(TwilightForestMod.prefix("noise_varying"), NoiseVaryingModelLoader.INSTANCE);
		UnbakedModelDeserializer.register(TwilightForestMod.prefix("patch"), PatchModelLoader.INSTANCE);
		UnbakedModelDeserializer.register(TwilightForestMod.prefix("force_field"), ForceFieldModelLoader.INSTANCE);
		UnbakedModelDeserializer.register(TwilightForestMod.prefix("giant_block"), GiantBlockModelLoader.INSTANCE);
		JarLidModels.register();
		AuroraModelRegistry.register();
	}

	private static void registerItemModelTypes() {
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("tf_chest"), TFChestSpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("skull_chest"), SkullChestSpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("keepsake_casket"), KeepsakeCasketSpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("candelabra"), CandelabraSpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("cicada"), CicadaSpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("firefly"), FireflySpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("moonworm"), MoonwormSpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("mason_jar"), MasonJarSpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("skull_candle"), SkullCandleSpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("boss_trophy"), TrophySpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("mystic_crown"), MysticCrownSpecialRenderer.Unbaked.MAP_CODEC);
		registerLateBound(net.minecraft.client.renderer.special.SpecialModelRenderers.class,
			TwilightForestMod.prefix("knightmetal_shield"), KnightmetalShieldSpecialRenderer.Unbaked.MAP_CODEC);

		registerLateBound(net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties.class,
			TwilightForestMod.prefix("natural_dimension"), NaturalDimension.TYPE);
		registerLateBound(net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties.class,
			TwilightForestMod.prefix("moonworm_queen_pulse"), MoonwormQueenPulse.TYPE);
		registerLateBound(net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties.class,
			TwilightForestMod.prefix("ore_meter_flash"), OreMeterFlash.TYPE);

		registerLateBound(net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties.class,
			TwilightForestMod.prefix("potion_flask_dosage"), PotionFlaskDosage.TYPE);
		registerLateBound(net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties.class,
			TwilightForestMod.prefix("potion_flask_damage"), PotionFlaskDamage.TYPE);

		registerLateBound(net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties.class,
			TwilightForestMod.prefix("experiment_115_variant"), Experiment115Type.TYPE);
	}

	private static void registerTooltipComponents() {
		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof BrittleFlaskItem.Tooltip tooltip) {
				return new PotionFlaskTooltipComponent(tooltip);
			}
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	private static <V> void registerLateBound(Class<?> owner, Identifier id, V value) {
		try {
			java.lang.reflect.Field mapperField = null;
			for (var field : owner.getDeclaredFields()) {
				if (ExtraCodecs.LateBoundIdMapper.class.isAssignableFrom(field.getType())) {
					mapperField = field;
					break;
				}
			}
			if (mapperField == null) {
				throw new NoSuchFieldException("LateBoundIdMapper field not found in " + owner.getName());
			}
			mapperField.setAccessible(true);
			ExtraCodecs.LateBoundIdMapper<Identifier, V> mapper = (ExtraCodecs.LateBoundIdMapper<Identifier, V>) mapperField.get(null);
			mapper.put(id, value);
		} catch (Throwable t) {
			TwilightForestMod.LOGGER.warn("Failed to register item model entry {} in {}", id, owner.getName(), t);
		}
	}

	private static void registerReloadListeners() {
		MagicPaintingTextureManager.instance = new MagicPaintingTextureManager();
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(TextureGeneratorReloadListener.INSTANCE);
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new TFSimpleArmorRenderer.ResourceReloadListener());
	}

	private static void registerScreens() {
		net.minecraft.client.gui.screens.MenuScreens.register(TFMenuTypes.UNCRAFTING.get(), UncraftingScreen::new);
	}

	private static void registerEntityRenderers() {
		EntityRenderers.register(TFEntities.BOAR.get(), BoarRenderer::new);
		EntityRenderers.register(TFEntities.BIGHORN_SHEEP.get(), BighornRenderer::new);
		EntityRenderers.register(TFEntities.DEER.get(), DeerRenderer::new);
		EntityRenderers.register(TFEntities.REDCAP.get(), RedcapRenderer::new);
		EntityRenderers.register(TFEntities.SKELETON_DRUID.get(), SkeletonDruidRenderer::new);
		EntityRenderers.register(TFEntities.HOSTILE_WOLF.get(), HostileWolfRenderer::new);
		EntityRenderers.register(TFEntities.WRAITH.get(), WraithRenderer::new);
		EntityRenderers.register(TFEntities.HYDRA.get(), HydraRenderer::new);
		EntityRenderers.register(TFEntities.LICH.get(), LichRenderer::new);
		EntityRenderers.register(TFEntities.PENGUIN.get(), m -> new BirdRenderer<>(m, new PenguinModel(m.bakeLayer(TFModelLayers.PENGUIN)), 0.375F, "penguin.png"));
		EntityRenderers.register(TFEntities.LICH_MINION.get(), LichMinionRenderer::new);
		EntityRenderers.register(TFEntities.LOYAL_ZOMBIE.get(), LoyalZombieRenderer::new);
		EntityRenderers.register(TFEntities.TINY_BIRD.get(), TinyBirdRenderer::new);
		EntityRenderers.register(TFEntities.SQUIRREL.get(), SquirrelRenderer::new);
		EntityRenderers.register(TFEntities.DWARF_RABBIT.get(), BunnyRenderer::new);
		EntityRenderers.register(TFEntities.RAVEN.get(), m -> new BirdRenderer<>(m, new RavenModel(m.bakeLayer(TFModelLayers.RAVEN)), 0.3F, "raven.png"));
		EntityRenderers.register(TFEntities.QUEST_RAM.get(), QuestRamRenderer::new);
		EntityRenderers.register(TFEntities.KOBOLD.get(), KoboldRenderer::new);
		//EntityRenderers.register(TFEntities.BOGGARD.get(), m -> new RenderTFBiped<>(m, new BipedModel<>(0), 0.625F, "kobold.png"));
		EntityRenderers.register(TFEntities.MOSQUITO_SWARM.get(), MosquitoSwarmRenderer::new);
		EntityRenderers.register(TFEntities.DEATH_TOME.get(), DeathTomeRenderer::new);
		EntityRenderers.register(TFEntities.MINOTAUR.get(), MinotaurRenderer::new);
		EntityRenderers.register(TFEntities.MINOSHROOM.get(), MinoshroomRenderer::new);
		EntityRenderers.register(TFEntities.FIRE_BEETLE.get(), FireBeetleRenderer::new);
		EntityRenderers.register(TFEntities.SLIME_BEETLE.get(), SlimeBeetleRenderer::new);
		EntityRenderers.register(TFEntities.PINCH_BEETLE.get(), PinchBeetleRenderer::new);
		EntityRenderers.register(TFEntities.MIST_WOLF.get(), MistWolfRenderer::new);
		EntityRenderers.register(TFEntities.CARMINITE_GHASTLING.get(), m -> new TFGhastRenderer<>(m, new TFGhastModel(m.bakeLayer(TFModelLayers.CARMINITE_GHASTLING)), 0.625F));
		EntityRenderers.register(TFEntities.CARMINITE_GOLEM.get(), CarminiteGolemRenderer::new);
		EntityRenderers.register(TFEntities.TOWERWOOD_BORER.get(), TowerwoodBorerRenderer::new);
		EntityRenderers.register(TFEntities.CARMINITE_GHASTGUARD.get(), CarminiteGhastRenderer::new);
		EntityRenderers.register(TFEntities.UR_GHAST.get(), UrGhastRenderer::new);
		EntityRenderers.register(TFEntities.BLOCKCHAIN_GOBLIN.get(), BlockChainGoblinRenderer::new);
		EntityRenderers.register(TFEntities.UPPER_GOBLIN_KNIGHT.get(), UpperGoblinKnightRenderer::new);
		EntityRenderers.register(TFEntities.LOWER_GOBLIN_KNIGHT.get(), LowerGoblinKnightRenderer::new);
		EntityRenderers.register(TFEntities.HELMET_CRAB.get(), HelmetCrabRenderer::new);
		EntityRenderers.register(TFEntities.KNIGHT_PHANTOM.get(), KnightPhantomRenderer::new);
		EntityRenderers.register(TFEntities.NAGA.get(), NagaRenderer::new);
		EntityRenderers.register(TFEntities.SWARM_SPIDER.get(), m -> new TFSpiderRenderer<>(m, 0.25F, "swarmspider.png", 0.5F));
		EntityRenderers.register(TFEntities.KING_SPIDER.get(), m -> new TFSpiderRenderer<>(m, 1.25F, "kingspider.png", 1.9F));
		EntityRenderers.register(TFEntities.CARMINITE_BROODLING.get(), m -> new TFSpiderRenderer<>(m, 0.6F, "towerbroodling.png", 0.7F));
		EntityRenderers.register(TFEntities.HEDGE_SPIDER.get(), m -> new TFSpiderRenderer<>(m, 0.8F, "hedgespider.png", 1.0F));
		EntityRenderers.register(TFEntities.REDCAP_SAPPER.get(), RedcapSapperRenderer::new);
		EntityRenderers.register(TFEntities.MAZE_SLIME.get(), MazeSlimeRenderer::new);
		EntityRenderers.register(TFEntities.YETI.get(), YetiRenderer::new);
		EntityRenderers.register(TFEntities.PROTECTION_BOX.get(), ProtectionBoxRenderer::new);
		EntityRenderers.register(TFEntities.MAGIC_PAINTING.get(), MagicPaintingRenderer::new);
		EntityRenderers.register(TFEntities.ALPHA_YETI.get(), AlphaYetiRenderer::new);
		EntityRenderers.register(TFEntities.WINTER_WOLF.get(), WinterWolfRenderer::new);
		EntityRenderers.register(TFEntities.SNOW_GUARDIAN.get(), SnowGuardianRenderer::new);
		EntityRenderers.register(TFEntities.STABLE_ICE_CORE.get(), StableIceCoreRenderer::new);
		EntityRenderers.register(TFEntities.UNSTABLE_ICE_CORE.get(), UnstableIceCoreRenderer::new);
		EntityRenderers.register(TFEntities.SNOW_QUEEN.get(), SnowQueenRenderer::new);
		EntityRenderers.register(TFEntities.TROLL.get(), TrollRenderer::new);
		EntityRenderers.register(TFEntities.GIANT_MINER.get(), TFGiantRenderer::new);
		EntityRenderers.register(TFEntities.ARMORED_GIANT.get(), TFGiantRenderer::new);
		EntityRenderers.register(TFEntities.ICE_CRYSTAL.get(), IceCrystalRenderer::new);
		EntityRenderers.register(TFEntities.CHAIN_BLOCK.get(), BlockChainRenderer::new);
		EntityRenderers.register(TFEntities.CUBE_OF_ANNIHILATION.get(), CubeOfAnnihilationRenderer::new);
		EntityRenderers.register(TFEntities.HARBINGER_CUBE.get(), HarbingerCubeRenderer::new);
		EntityRenderers.register(TFEntities.ADHERENT.get(), AdherentRenderer::new);
		EntityRenderers.register(TFEntities.ROVING_CUBE.get(), RovingCubeRenderer::new);
		EntityRenderers.register(TFEntities.RISING_ZOMBIE.get(), RisingZombieRenderer::new);
		EntityRenderers.register(TFEntities.PLATEAU_BOSS.get(), NoopRenderer::new);

		// projectiles
		EntityRenderers.register(TFEntities.NATURE_BOLT.get(), ThrownItemRenderer::new);
		EntityRenderers.register(TFEntities.LICH_BOLT.get(), c -> new CustomProjectileTextureRenderer(c, TwilightForestMod.prefix("textures/particle/twilight_orb.png"), 1.0F, true, false));
		EntityRenderers.register(TFEntities.WAND_BOLT.get(), c -> new CustomProjectileTextureRenderer(c, TwilightForestMod.prefix("textures/particle/twilight_orb.png"), 1.0F, true, false));
		EntityRenderers.register(TFEntities.LICH_BOMB.get(), c -> new CustomProjectileTextureRenderer(c, Identifier.withDefaultNamespace("textures/item/magma_cream.png"), 1.0F, true, true));
		EntityRenderers.register(TFEntities.TOME_BOLT.get(), ThrownItemRenderer::new);
		EntityRenderers.register(TFEntities.HYDRA_MORTAR.get(), HydraMortarRenderer::new);
		EntityRenderers.register(TFEntities.SLIME_BLOB.get(), ThrownItemRenderer::new);
		EntityRenderers.register(TFEntities.MOONWORM_SHOT.get(), MoonwormShotRenderer::new);
		EntityRenderers.register(TFEntities.CHARM_EFFECT.get(), ThrownItemRenderer::new);
		EntityRenderers.register(TFEntities.THROWN_WEP.get(), ThrownWepRenderer::new);
		EntityRenderers.register(TFEntities.FALLING_ICE.get(), FallingIceRenderer::new);
		EntityRenderers.register(TFEntities.THROWN_ICE.get(), ThrownIceRenderer::new);
		EntityRenderers.register(TFEntities.THROWN_BLOCK.get(), ThrownBlockRenderer::new);
		EntityRenderers.register(TFEntities.ICE_SNOWBALL.get(), ThrownItemRenderer::new);
		EntityRenderers.register(TFEntities.SLIDER.get(), SlideBlockRenderer::new);
		EntityRenderers.register(TFEntities.SEEKER_ARROW.get(), DefaultArrowRenderer::new);
		EntityRenderers.register(TFEntities.ICE_ARROW.get(), DefaultArrowRenderer::new);

		// boats
		EntityRenderers.register(TFEntities.TWILIGHT_OAK_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.TWILIGHT_OAK_BOAT));
		EntityRenderers.register(TFEntities.CANOPY_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.CANOPY_BOAT));
		EntityRenderers.register(TFEntities.MANGROVE_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.MANGROVE_BOAT));
		EntityRenderers.register(TFEntities.DARK_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.DARK_BOAT));
		EntityRenderers.register(TFEntities.TIME_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.TIME_BOAT));
		EntityRenderers.register(TFEntities.TRANSFORMATION_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.TRANSFORMATION_BOAT));
		EntityRenderers.register(TFEntities.MINING_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.MINING_BOAT));
		EntityRenderers.register(TFEntities.SORTING_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.SORTING_BOAT));
		EntityRenderers.register(TFEntities.TWILIGHT_OAK_CHEST_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.TWILIGHT_OAK_CHEST_BOAT));
		EntityRenderers.register(TFEntities.CANOPY_CHEST_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.CANOPY_CHEST_BOAT));
		EntityRenderers.register(TFEntities.MANGROVE_CHEST_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.MANGROVE_CHEST_BOAT));
		EntityRenderers.register(TFEntities.DARK_CHEST_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.DARK_CHEST_BOAT));
		EntityRenderers.register(TFEntities.TIME_CHEST_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.TIME_CHEST_BOAT));
		EntityRenderers.register(TFEntities.TRANSFORMATION_CHEST_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.TRANSFORMATION_CHEST_BOAT));
		EntityRenderers.register(TFEntities.MINING_CHEST_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.MINING_CHEST_BOAT));
		EntityRenderers.register(TFEntities.SORTING_CHEST_BOAT.get(), context -> new BoatRenderer(context, TFModelLayers.SORTING_CHEST_BOAT));
	}

	private static void registerBlockEntityRenderers() {
		BlockEntityRenderers.register(TFBlockEntities.FIREFLY.get(), FireflyRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.CICADA.get(), CicadaRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.MOONWORM.get(), MoonwormRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.TROPHY.get(), TrophyRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.TF_CHEST.get(), TFChestRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.TF_TRAPPED_CHEST.get(), TFChestRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.SKULL_CHEST.get(), SkullChestRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.KEEPSAKE_CASKET.get(), KeepsakeCasketRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.SKULL_CANDLE.get(), SkullCandleRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.REACTOR_DEBRIS.get(), ReactorDebrisRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.RED_THREAD.get(), RedThreadRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.CANDELABRA.get(), CandelabraRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.JAR.get(), JarRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.MASON_JAR.get(), JarRenderer.MasonJarRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.OMINOUS_CANDLE.get(), OminousCandleRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.SINISTER_SPAWNER.get(), SinisterSpawnerRenderer::new);
		BlockEntityRenderers.register(TFBlockEntities.BRAZIER.get(), BrazierRenderer::new);
	}

	private static void registerLayerDefinitions() {
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.ARCTIC_ARMOR_INNER, () -> LayerDefinition.create(ArcticArmorModel.addPieces(INNER_ARMOR_DEFORMATION), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.ARCTIC_ARMOR_OUTER, () -> LayerDefinition.create(ArcticArmorModel.addPieces(OUTER_ARMOR_DEFORMATION), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.FIERY_ARMOR_INNER, () -> LayerDefinition.create(FieryArmorModel.createMesh(INNER_ARMOR_DEFORMATION, 0.0F), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.FIERY_ARMOR_OUTER, () -> LayerDefinition.create(FieryArmorModel.createMesh(OUTER_ARMOR_DEFORMATION, 0.0F), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.KNIGHTMETAL_ARMOR_INNER, () -> LayerDefinition.create(KnightmetalArmorModel.addPieces(INNER_ARMOR_DEFORMATION), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.KNIGHTMETAL_ARMOR_OUTER, () -> LayerDefinition.create(KnightmetalArmorModel.addPieces(OUTER_ARMOR_DEFORMATION), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.PHANTOM_ARMOR_INNER, () -> LayerDefinition.create(PhantomArmorModel.addPieces(INNER_ARMOR_DEFORMATION), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.PHANTOM_ARMOR_OUTER, () -> LayerDefinition.create(PhantomArmorModel.addPieces(OUTER_ARMOR_DEFORMATION), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.YETI_ARMOR_INNER, () -> LayerDefinition.create(YetiArmorModel.addPieces(INNER_ARMOR_DEFORMATION), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.YETI_ARMOR_OUTER, () -> LayerDefinition.create(YetiArmorModel.addPieces(OUTER_ARMOR_DEFORMATION), 64, 32));

		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.ALPHA_YETI_TROPHY, AlphaYetiModel::createTrophy);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.HYDRA_TROPHY, HydraHeadModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.KNIGHT_PHANTOM_TROPHY, KnightPhantomModel::createTrophy);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.LICH_TROPHY, LichModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MINOSHROOM_TROPHY, MinoshroomModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.NAGA_TROPHY, NagaModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.QUEST_RAM_TROPHY, QuestRamModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.SNOW_QUEEN_TROPHY, SnowQueenModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.UR_GHAST_TROPHY, UrGhastModel::create);

		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.ADHERENT, AdherentModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.ALPHA_YETI, AlphaYetiModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.ARMORED_GIANT, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.BIGHORN_SHEEP, BighornModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.BIGHORN_SHEEP_BABY, () -> BighornModel.create().apply(BighornModel.BABY_TRANSFORMER));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.BLOCKCHAIN_GOBLIN, BlockChainGoblinModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.BOAR, BoarModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.BUNNY, BunnyModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.CARMINITE_BROODLING, SpiderModel::createSpiderBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.CARMINITE_GOLEM, CarminiteGolemModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.CARMINITE_GHASTGUARD, TFGhastModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.CARMINITE_GHASTLING, TFGhastModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.CHAIN, ChainModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.CUBE_OF_ANNIHILATION, CubeOfAnnihilationModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.DEATH_TOME, DeathTomeModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.DEER, DeerModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.FIRE_BEETLE, FireBeetleModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.GIANT_MINER, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.HARBINGER_CUBE, HarbingerCubeModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.HEDGE_SPIDER, SpiderModel::createSpiderBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.HELMET_CRAB, HelmetCrabModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.HOSTILE_WOLF, () -> LayerDefinition.create(WolfModel.createMeshDefinition(CubeDeformation.NONE), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.HYDRA_HEAD, HydraHeadModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.HYDRA, HydraModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.HYDRA_MORTAR, HydraMortarModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.HYDRA_NECK, HydraNeckModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.ICE_CRYSTAL, IceCrystalModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.KING_SPIDER, SpiderModel::createSpiderBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.KNIGHT_PHANTOM, KnightPhantomModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.KOBOLD, KoboldModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.LICH_MINION, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.LICH, LichModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.LOWER_GOBLIN_KNIGHT, LowerGoblinKnightModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.LOYAL_ZOMBIE, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MAZE_SLIME, SlimeModel::createInnerBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MAZE_SLIME_OUTER, SlimeModel::createOuterBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MINOSHROOM, MinoshroomModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MINOTAUR, MinotaurModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MIST_WOLF, () -> LayerDefinition.create(WolfModel.createMeshDefinition(CubeDeformation.NONE), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MOSQUITO_SWARM, MosquitoSwarmModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.NAGA, NagaModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.NAGA_BODY, NagaModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.NOOP, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 0, 0));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.PENGUIN, PenguinModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.PINCH_BEETLE, PinchBeetleModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.PROTECTION_BOX, () -> LayerDefinition.create(ProtectionBoxModel.createMesh(), 16, 16));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.QUEST_RAM, QuestRamModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.RAVEN, RavenModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.REDCAP, RedcapModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.REDCAP_ARMOR_INNER, () -> LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.25F), 0.7F), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.REDCAP_ARMOR_OUTER, () -> LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.65F), 0.7F), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.RISING_ZOMBIE, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.ROVING_CUBE, CubeOfAnnihilationModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.SKELETON_DRUID, SkeletonDruidModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.SLIME_BEETLE, SlimeBeetleModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.SLIME_BEETLE_TAIL, SlimeBeetleModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.SNOW_QUEEN, SnowQueenModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.CHAIN_BLOCK, SpikeBlockModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.SQUIRREL, SquirrelModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.STABLE_ICE_CORE, StableIceCoreModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.SWARM_SPIDER, SpiderModel::createSpiderBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.TINY_BIRD, TinyBirdModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.TOWERWOOD_BORER, SilverfishModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.TROLL, TrollModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.UNSTABLE_ICE_CORE, UnstableIceCoreModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.UPPER_GOBLIN_KNIGHT, UpperGoblinKnightModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.UR_GHAST, UrGhastModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.WINTER_WOLF, () -> LayerDefinition.create(WolfModel.createMeshDefinition(CubeDeformation.NONE), 64, 32));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.WRAITH, WraithModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.YETI, YetiModel::create);

		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.CICADA, CicadaModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.FIREFLY, FireflyModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.KEEPSAKE_CASKET, () -> KeepsakeCasketModel.create(true));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.SKULL_CHEST, () -> KeepsakeCasketModel.create(false));
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MOONWORM, MoonwormModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.BRAZIER, BrazierModel::create);

		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.RED_THREAD, RedThreadModel::create);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.KNIGHTMETAL_SHIELD, KnightmetalShieldModel::create);

		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.TWILIGHT_OAK_BOAT, BoatModel::createBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.CANOPY_BOAT, BoatModel::createBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MANGROVE_BOAT, BoatModel::createBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.DARK_BOAT, BoatModel::createBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.TIME_BOAT, BoatModel::createBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.TRANSFORMATION_BOAT, BoatModel::createBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MINING_BOAT, BoatModel::createBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.SORTING_BOAT, BoatModel::createBoatModel);

		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.TWILIGHT_OAK_CHEST_BOAT, BoatModel::createChestBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.CANOPY_CHEST_BOAT, BoatModel::createChestBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MANGROVE_CHEST_BOAT, BoatModel::createChestBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.DARK_CHEST_BOAT, BoatModel::createChestBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.TIME_CHEST_BOAT, BoatModel::createChestBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.TRANSFORMATION_CHEST_BOAT, BoatModel::createChestBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.MINING_CHEST_BOAT, BoatModel::createChestBoatModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelLayers.SORTING_CHEST_BOAT, BoatModel::createChestBoatModel);
	}

	private static void registerSpecialModels() {
		SpecialBlockRendererRegistry.register(TFBlocks.TWILIGHT_OAK_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("twilight_oak/normal")));
		SpecialBlockRendererRegistry.register(TFBlocks.CANOPY_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("canopy/normal")));
		SpecialBlockRendererRegistry.register(TFBlocks.MANGROVE_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("mangrove/normal")));
		SpecialBlockRendererRegistry.register(TFBlocks.DARK_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("darkwood/normal")));
		SpecialBlockRendererRegistry.register(TFBlocks.TIME_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("time/normal")));
		SpecialBlockRendererRegistry.register(TFBlocks.TRANSFORMATION_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("tranformation/normal")));
		SpecialBlockRendererRegistry.register(TFBlocks.MINING_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("mining/normal")));
		SpecialBlockRendererRegistry.register(TFBlocks.SORTING_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("sorting/normal")));
		SpecialBlockRendererRegistry.register(TFBlocks.TWILIGHT_OAK_TRAPPED_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("twilight_oak/trapped")));
		SpecialBlockRendererRegistry.register(TFBlocks.CANOPY_TRAPPED_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("canopy/trapped")));
		SpecialBlockRendererRegistry.register(TFBlocks.MANGROVE_TRAPPED_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("mangrove/trapped")));
		SpecialBlockRendererRegistry.register(TFBlocks.DARK_TRAPPED_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("darkwood/trapped")));
		SpecialBlockRendererRegistry.register(TFBlocks.TIME_TRAPPED_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("time/trapped")));
		SpecialBlockRendererRegistry.register(TFBlocks.TRANSFORMATION_TRAPPED_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("tranformation/trapped")));
		SpecialBlockRendererRegistry.register(TFBlocks.MINING_TRAPPED_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("mining/trapped")));
		SpecialBlockRendererRegistry.register(TFBlocks.SORTING_TRAPPED_CHEST.get(), new TFChestSpecialRenderer.Unbaked(TwilightForestMod.prefix("sorting/trapped")));

		SpecialBlockRendererRegistry.register(TFBlocks.SKULL_CHEST.get(), new SkullChestSpecialRenderer.Unbaked());
		SpecialBlockRendererRegistry.register(TFBlocks.KEEPSAKE_CASKET.get(), new KeepsakeCasketSpecialRenderer.Unbaked());
		SpecialBlockRendererRegistry.register(TFBlocks.CANDELABRA.get(), new CandelabraSpecialRenderer.Unbaked());
		SpecialBlockRendererRegistry.register(TFBlocks.CICADA.get(), new CicadaSpecialRenderer.Unbaked());
		SpecialBlockRendererRegistry.register(TFBlocks.FIREFLY.get(), new FireflySpecialRenderer.Unbaked());
		SpecialBlockRendererRegistry.register(TFBlocks.MOONWORM.get(), new MoonwormSpecialRenderer.Unbaked());

		SpecialBlockRendererRegistry.register(TFBlocks.FIREFLY_JAR.get(), new MasonJarSpecialRenderer.Unbaked(TFBlocks.TWILIGHT_OAK_LOG.asItem()));
		SpecialBlockRendererRegistry.register(TFBlocks.CICADA_JAR.get(), new MasonJarSpecialRenderer.Unbaked(TFBlocks.CANOPY_LOG.asItem()));
		SpecialBlockRendererRegistry.register(TFBlocks.MASON_JAR.get(), new MasonJarSpecialRenderer.Unbaked(TFBlocks.TWILIGHT_OAK_LOG.asItem()));

		SpecialBlockRendererRegistry.register(TFBlocks.ZOMBIE_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.ZOMBIE));
		SpecialBlockRendererRegistry.register(TFBlocks.ZOMBIE_WALL_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.ZOMBIE));
		SpecialBlockRendererRegistry.register(TFBlocks.SKELETON_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.SKELETON));
		SpecialBlockRendererRegistry.register(TFBlocks.SKELETON_WALL_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.SKELETON));
		SpecialBlockRendererRegistry.register(TFBlocks.CREEPER_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.CREEPER));
		SpecialBlockRendererRegistry.register(TFBlocks.CREEPER_WALL_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.CREEPER));
		SpecialBlockRendererRegistry.register(TFBlocks.WITHER_SKELE_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.WITHER_SKELETON));
		SpecialBlockRendererRegistry.register(TFBlocks.WITHER_SKELE_WALL_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.WITHER_SKELETON));
		SpecialBlockRendererRegistry.register(TFBlocks.PLAYER_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.PLAYER));
		SpecialBlockRendererRegistry.register(TFBlocks.PLAYER_WALL_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.PLAYER));
		SpecialBlockRendererRegistry.register(TFBlocks.PIGLIN_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.PIGLIN));
		SpecialBlockRendererRegistry.register(TFBlocks.PIGLIN_WALL_SKULL_CANDLE.get(), new SkullCandleSpecialRenderer.Unbaked(SkullBlock.Types.PIGLIN));

		SpecialBlockRendererRegistry.register(TFBlocks.NAGA_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.NAGA));
		SpecialBlockRendererRegistry.register(TFBlocks.NAGA_WALL_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.NAGA));
		SpecialBlockRendererRegistry.register(TFBlocks.LICH_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.LICH));
		SpecialBlockRendererRegistry.register(TFBlocks.LICH_WALL_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.LICH));
		SpecialBlockRendererRegistry.register(TFBlocks.MINOSHROOM_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.MINOSHROOM));
		SpecialBlockRendererRegistry.register(TFBlocks.MINOSHROOM_WALL_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.MINOSHROOM));
		SpecialBlockRendererRegistry.register(TFBlocks.HYDRA_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.HYDRA));
		SpecialBlockRendererRegistry.register(TFBlocks.HYDRA_WALL_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.HYDRA));
		SpecialBlockRendererRegistry.register(TFBlocks.KNIGHT_PHANTOM_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.KNIGHT_PHANTOM));
		SpecialBlockRendererRegistry.register(TFBlocks.KNIGHT_PHANTOM_WALL_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.KNIGHT_PHANTOM));
		SpecialBlockRendererRegistry.register(TFBlocks.UR_GHAST_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.UR_GHAST));
		SpecialBlockRendererRegistry.register(TFBlocks.UR_GHAST_WALL_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.UR_GHAST));
		SpecialBlockRendererRegistry.register(TFBlocks.ALPHA_YETI_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.ALPHA_YETI));
		SpecialBlockRendererRegistry.register(TFBlocks.ALPHA_YETI_WALL_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.ALPHA_YETI));
		SpecialBlockRendererRegistry.register(TFBlocks.SNOW_QUEEN_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.SNOW_QUEEN));
		SpecialBlockRendererRegistry.register(TFBlocks.SNOW_QUEEN_WALL_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.SNOW_QUEEN));
		SpecialBlockRendererRegistry.register(TFBlocks.QUEST_RAM_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.QUEST_RAM));
		SpecialBlockRendererRegistry.register(TFBlocks.QUEST_RAM_WALL_TROPHY.get(), new TrophySpecialRenderer.Unbaked(BossVariant.QUEST_RAM));
	}

	private static void registerParticleFactories() {
		ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
		registry.register(TFParticleType.LARGE_FLAME.get(), LargeFlameParticle.Factory::new);
		registry.register(TFParticleType.LEAF_RUNE.get(), LeafRuneParticle.Factory::new);
		registry.register(TFParticleType.BOSS_TEAR.get(), new GhastTearParticle.Factory());
		registry.register(TFParticleType.GHAST_TRAP.get(), GhastTrapParticle.Factory::new);
		registry.register(TFParticleType.PROTECTION.get(), ProtectionParticle.Factory::new);
		registry.register(TFParticleType.SNOW.get(), SnowParticle.Factory::new);
		registry.register(TFParticleType.SNOW_GUARDIAN.get(), SnowGuardianParticle.Factory::new);
		registry.register(TFParticleType.SNOW_WARNING.get(), SnowWarningParticle.SimpleFactory::new);
		registry.register(TFParticleType.EXTENDED_SNOW_WARNING.get(), SnowWarningParticle.ExtendedFactory::new);
		registry.register(TFParticleType.ICE_BEAM.get(), IceBeamParticle.Factory::new);
		registry.register(TFParticleType.ANNIHILATE.get(), AnnihilateParticle.Factory::new);
		registry.register(TFParticleType.HUGE_SMOKE.get(), SmokeScaleParticle.Factory::new);
		registry.register(TFParticleType.FIREFLY.get(), FireflyParticle.StationaryProvider::new);
		registry.register(TFParticleType.WANDERING_FIREFLY.get(), FireflyParticle.WanderingProvider::new);
		registry.register(TFParticleType.PARTICLE_SPAWNER_FIREFLY.get(), FireflyParticle.ParticleSpawnerProvider::new);
		registry.register(TFParticleType.FALLEN_LEAF.get(), LeafParticle.Factory::new);
		registry.register(TFParticleType.DIM_FLAME.get(), FlameParticle.SmallFlameProvider::new);
		registry.register(TFParticleType.OMINOUS_FLAME.get(), FlameParticle.SmallFlameProvider::new);
		registry.register(TFParticleType.SORTING_PARTICLE.get(), SortingParticle.Factory::new);
		registry.register(TFParticleType.TRANSFORMATION_PARTICLE.get(), TransformationParticle.Factory::new);
		registry.register(TFParticleType.LOG_CORE_PARTICLE.get(), LogCoreParticle.Factory::new);
		registry.register(TFParticleType.CLOUD_PUFF.get(), CloudPuffParticle.Factory::new);
		registry.register(TFParticleType.MAGIC_EFFECT.get(), MagicEffectParticle.Factory::new);
		registry.register(TFParticleType.ANGRY_LICH.get(), AngryLichParticle.Factory::new);
		registry.register(TFParticleType.TWILIGHT_ORB.get(), (FabricSpriteProvider sprite) -> new CustomTextureParticle.Factory(sprite, true));
		registry.register(TFParticleType.SHIELD_BREAK.get(), CustomTextureParticle.ShieldBreak::new);
	}

	private static void registerArmorRenderers() {
		ArmorRenderer.register(new TFSimpleArmorRenderer(HumanoidModel::new, TFModelLayers.ARCTIC_ARMOR_INNER, TFModelLayers.ARCTIC_ARMOR_OUTER),
			TFItems.ARCTIC_HELMET.get(), TFItems.ARCTIC_CHESTPLATE.get(), TFItems.ARCTIC_LEGGINGS.get(), TFItems.ARCTIC_BOOTS.get());
		ArmorRenderer.register(new TFSimpleArmorRenderer(FieryArmorModel::new, TFModelLayers.FIERY_ARMOR_INNER, TFModelLayers.FIERY_ARMOR_OUTER, true),
			TFItems.FIERY_HELMET.get(), TFItems.FIERY_CHESTPLATE.get(), TFItems.FIERY_LEGGINGS.get(), TFItems.FIERY_BOOTS.get());
		ArmorRenderer.register(new TFSimpleArmorRenderer(HumanoidModel::new, TFModelLayers.KNIGHTMETAL_ARMOR_INNER, TFModelLayers.KNIGHTMETAL_ARMOR_OUTER),
			TFItems.KNIGHTMETAL_HELMET.get(), TFItems.KNIGHTMETAL_CHESTPLATE.get(), TFItems.KNIGHTMETAL_LEGGINGS.get(), TFItems.KNIGHTMETAL_BOOTS.get());
		ArmorRenderer.register(new TFSimpleArmorRenderer(HumanoidModel::new, TFModelLayers.PHANTOM_ARMOR_INNER, TFModelLayers.PHANTOM_ARMOR_OUTER),
			TFItems.PHANTOM_HELMET.get(), TFItems.PHANTOM_CHESTPLATE.get());
		ArmorRenderer.register(new TFSimpleArmorRenderer(HumanoidModel::new, TFModelLayers.YETI_ARMOR_INNER, TFModelLayers.YETI_ARMOR_OUTER),
			TFItems.YETI_HELMET.get(), TFItems.YETI_CHESTPLATE.get(), TFItems.YETI_LEGGINGS.get(), TFItems.YETI_BOOTS.get());
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void registerFeatureLayers() {
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, registrationHelper, context) -> {
			BakedMultiPartRenderers.ensureInitialized(context);
			if (renderer instanceof LivingEntityRenderer living) {
				registrationHelper.register(new ShieldLayer(living));
				registrationHelper.register(new IceLayer(living));
			}
		});
	}

	public static boolean isOptifinePresent() {
		return optifinePresent;
	}
}
