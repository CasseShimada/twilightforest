package twilightforest.client.event;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
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
import net.minecraft.resources.Identifier;
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

public class RegistrationEvents {
	private static boolean optifinePresent = false;
	private static final CubeDeformation INNER_ARMOR_DEFORMATION = new CubeDeformation(0.5F);
	private static final CubeDeformation OUTER_ARMOR_DEFORMATION = new CubeDeformation(1.0F);

	public static void register() {
		detectOptifine();
		TFRenderPipelines.init();
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

	private static void registerRenderLayers() {
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
		ClientTooltipComponentCallback.EVENT.register(data -> {
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
		net.minecraft.client.gui.screens.MenuScreens.register(TFMenuTypes.UNCRAFTING, UncraftingScreen::new);
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
		BlockEntityRenderers.register(TFBlockEntities.DRYING_RACK.get(), DryingRackRenderer::new);
	}

	private static void registerLayerDefinitions() {
		ModelLayerRegistry.registerModelLayer(TFModelLayers.ARCTIC_ARMOR_INNER, () -> LayerDefinition.create(ArcticArmorModel.addPieces(INNER_ARMOR_DEFORMATION), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.ARCTIC_ARMOR_OUTER, () -> LayerDefinition.create(ArcticArmorModel.addPieces(OUTER_ARMOR_DEFORMATION), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.FIERY_ARMOR_INNER, () -> LayerDefinition.create(FieryArmorModel.createMesh(INNER_ARMOR_DEFORMATION, 0.0F), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.FIERY_ARMOR_OUTER, () -> LayerDefinition.create(FieryArmorModel.createMesh(OUTER_ARMOR_DEFORMATION, 0.0F), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.KNIGHTMETAL_ARMOR_INNER, () -> LayerDefinition.create(KnightmetalArmorModel.addPieces(INNER_ARMOR_DEFORMATION), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.KNIGHTMETAL_ARMOR_OUTER, () -> LayerDefinition.create(KnightmetalArmorModel.addPieces(OUTER_ARMOR_DEFORMATION), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.PHANTOM_ARMOR_INNER, () -> LayerDefinition.create(PhantomArmorModel.addPieces(INNER_ARMOR_DEFORMATION), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.PHANTOM_ARMOR_OUTER, () -> LayerDefinition.create(PhantomArmorModel.addPieces(OUTER_ARMOR_DEFORMATION), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.YETI_ARMOR_INNER, () -> LayerDefinition.create(YetiArmorModel.addPieces(INNER_ARMOR_DEFORMATION), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.YETI_ARMOR_OUTER, () -> LayerDefinition.create(YetiArmorModel.addPieces(OUTER_ARMOR_DEFORMATION), 64, 32));

		ModelLayerRegistry.registerModelLayer(TFModelLayers.ALPHA_YETI_TROPHY, AlphaYetiModel::createTrophy);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.HYDRA_TROPHY, HydraHeadModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.KNIGHT_PHANTOM_TROPHY, KnightPhantomModel::createTrophy);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.LICH_TROPHY, LichModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MINOSHROOM_TROPHY, MinoshroomModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.NAGA_TROPHY, NagaModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.QUEST_RAM_TROPHY, QuestRamModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.SNOW_QUEEN_TROPHY, SnowQueenModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.UR_GHAST_TROPHY, UrGhastModel::create);

		ModelLayerRegistry.registerModelLayer(TFModelLayers.ADHERENT, AdherentModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.ALPHA_YETI, AlphaYetiModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.ARMORED_GIANT, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.BIGHORN_SHEEP, BighornModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.BIGHORN_SHEEP_BABY, () -> BighornModel.create().apply(BighornModel.BABY_TRANSFORMER));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.BLOCKCHAIN_GOBLIN, BlockChainGoblinModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.BOAR, BoarModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.BUNNY, BunnyModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.CARMINITE_BROODLING, SpiderModel::createSpiderBodyLayer);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.CARMINITE_GOLEM, CarminiteGolemModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.CARMINITE_GHASTGUARD, TFGhastModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.CARMINITE_GHASTLING, TFGhastModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.CHAIN, ChainModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.CUBE_OF_ANNIHILATION, CubeOfAnnihilationModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.DEATH_TOME, DeathTomeModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.DEER, DeerModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.FIRE_BEETLE, FireBeetleModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.GIANT_MINER, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.HARBINGER_CUBE, HarbingerCubeModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.HEDGE_SPIDER, SpiderModel::createSpiderBodyLayer);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.HELMET_CRAB, HelmetCrabModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.HOSTILE_WOLF, () -> LayerDefinition.create(net.minecraft.client.model.animal.wolf.AdultWolfModel.createBodyLayer(CubeDeformation.NONE), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.HYDRA_HEAD, HydraHeadModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.HYDRA, HydraModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.HYDRA_MORTAR, HydraMortarModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.HYDRA_NECK, HydraNeckModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.ICE_CRYSTAL, IceCrystalModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.KING_SPIDER, SpiderModel::createSpiderBodyLayer);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.KNIGHT_PHANTOM, KnightPhantomModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.KOBOLD, KoboldModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.LICH_MINION, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.LICH, LichModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.LOWER_GOBLIN_KNIGHT, LowerGoblinKnightModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.LOYAL_ZOMBIE, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MAZE_SLIME, SlimeModel::createInnerBodyLayer);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MAZE_SLIME_OUTER, SlimeModel::createOuterBodyLayer);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MINOSHROOM, MinoshroomModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MINOTAUR, MinotaurModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MIST_WOLF, () -> LayerDefinition.create(net.minecraft.client.model.animal.wolf.AdultWolfModel.createBodyLayer(CubeDeformation.NONE), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MOSQUITO_SWARM, MosquitoSwarmModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.NAGA, NagaModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.NAGA_BODY, NagaModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.NOOP, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 0, 0));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.PENGUIN, PenguinModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.PINCH_BEETLE, PinchBeetleModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.PROTECTION_BOX, () -> LayerDefinition.create(ProtectionBoxModel.createMesh(), 16, 16));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.QUEST_RAM, QuestRamModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.RAVEN, RavenModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.REDCAP, RedcapModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.REDCAP_ARMOR_INNER, () -> LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.25F), 0.7F), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.REDCAP_ARMOR_OUTER, () -> LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.65F), 0.7F), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.RISING_ZOMBIE, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.ROVING_CUBE, CubeOfAnnihilationModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.SKELETON_DRUID, SkeletonDruidModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.SLIME_BEETLE, SlimeBeetleModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.SLIME_BEETLE_TAIL, SlimeBeetleModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.SNOW_QUEEN, SnowQueenModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.CHAIN_BLOCK, SpikeBlockModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.SQUIRREL, SquirrelModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.STABLE_ICE_CORE, StableIceCoreModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.SWARM_SPIDER, SpiderModel::createSpiderBodyLayer);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.TINY_BIRD, TinyBirdModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.TOWERWOOD_BORER, SilverfishModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.TROLL, TrollModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.UNSTABLE_ICE_CORE, UnstableIceCoreModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.UPPER_GOBLIN_KNIGHT, UpperGoblinKnightModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.UR_GHAST, UrGhastModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.WINTER_WOLF, () -> LayerDefinition.create(net.minecraft.client.model.animal.wolf.AdultWolfModel.createBodyLayer(CubeDeformation.NONE), 64, 32));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.WRAITH, WraithModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.YETI, YetiModel::create);

		ModelLayerRegistry.registerModelLayer(TFModelLayers.CICADA, CicadaModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.FIREFLY, FireflyModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.KEEPSAKE_CASKET, () -> KeepsakeCasketModel.create(true));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.SKULL_CHEST, () -> KeepsakeCasketModel.create(false));
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MOONWORM, MoonwormModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.BRAZIER, BrazierModel::create);

		ModelLayerRegistry.registerModelLayer(TFModelLayers.RED_THREAD, RedThreadModel::create);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.KNIGHTMETAL_SHIELD, KnightmetalShieldModel::create);

		ModelLayerRegistry.registerModelLayer(TFModelLayers.TWILIGHT_OAK_BOAT, BoatModel::createBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.CANOPY_BOAT, BoatModel::createBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MANGROVE_BOAT, BoatModel::createBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.DARK_BOAT, BoatModel::createBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.TIME_BOAT, BoatModel::createBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.TRANSFORMATION_BOAT, BoatModel::createBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MINING_BOAT, BoatModel::createBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.SORTING_BOAT, BoatModel::createBoatModel);

		ModelLayerRegistry.registerModelLayer(TFModelLayers.TWILIGHT_OAK_CHEST_BOAT, BoatModel::createChestBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.CANOPY_CHEST_BOAT, BoatModel::createChestBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MANGROVE_CHEST_BOAT, BoatModel::createChestBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.DARK_CHEST_BOAT, BoatModel::createChestBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.TIME_CHEST_BOAT, BoatModel::createChestBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.TRANSFORMATION_CHEST_BOAT, BoatModel::createChestBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.MINING_CHEST_BOAT, BoatModel::createChestBoatModel);
		ModelLayerRegistry.registerModelLayer(TFModelLayers.SORTING_CHEST_BOAT, BoatModel::createChestBoatModel);
	}

	private static void registerSpecialModels() {
	}

	private static void registerParticleFactories() {
		ParticleProviderRegistry registry = ParticleProviderRegistry.getInstance();
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
		registry.register(TFParticleType.TWILIGHT_ORB.get(), (FabricSpriteSet sprite) -> new CustomTextureParticle.Factory(sprite, true));
		registry.register(TFParticleType.SHIELD_BREAK.get(), CustomTextureParticle.ShieldBreak::new);
		registry.register(TFParticleType.DRYING_RACK.get(), DryingRackParticle.Provider::new);
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
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, registrationHelper, context) -> {
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
