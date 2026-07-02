package twilightforest.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import twilightforest.TwilightForestMod;
import twilightforest.block.entity.*;
import twilightforest.block.entity.bookshelf.ChiseledCanopyShelfBlockEntity;
import twilightforest.block.entity.spawner.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class TFBlockEntities {
	private static final Map<Identifier, BlockEntityType<?>> BLOCK_ENTITIES = new LinkedHashMap<>();
	private static boolean registered;

	public static final BlockEntityType<AntibuilderBlockEntity> ANTIBUILDER = register("antibuilder", FabricBlockEntityTypeBuilder.create(AntibuilderBlockEntity::new, TFBlocks.ANTIBUILDER).build());
	public static final BlockEntityType<CinderFurnaceBlockEntity> CINDER_FURNACE = register("cinder_furnace", FabricBlockEntityTypeBuilder.create(CinderFurnaceBlockEntity::new, TFBlocks.CINDER_FURNACE).build());
	public static final BlockEntityType<CarminiteReactorBlockEntity> CARMINITE_REACTOR = register("carminite_reactor", FabricBlockEntityTypeBuilder.create(CarminiteReactorBlockEntity::new, TFBlocks.CARMINITE_REACTOR).build());
	public static final BlockEntityType<ReactorDebrisBlockEntity> REACTOR_DEBRIS = register("reactor_debris", FabricBlockEntityTypeBuilder.create(ReactorDebrisBlockEntity::new, TFBlocks.REACTOR_DEBRIS).build());
	public static final BlockEntityType<FireJetBlockEntity> FLAME_JET = register("flame_jet", FabricBlockEntityTypeBuilder.create(FireJetBlockEntity::new, TFBlocks.FIRE_JET, TFBlocks.ENCASED_FIRE_JET).build());
	public static final BlockEntityType<GhastTrapBlockEntity> GHAST_TRAP = register("ghast_trap", FabricBlockEntityTypeBuilder.create(GhastTrapBlockEntity::new, TFBlocks.GHAST_TRAP).build());
	public static final BlockEntityType<TFSmokerBlockEntity> SMOKER = register("smoker", FabricBlockEntityTypeBuilder.create(TFSmokerBlockEntity::new, TFBlocks.SMOKER, TFBlocks.ENCASED_SMOKER).build());
	public static final BlockEntityType<CarminiteBuilderBlockEntity> TOWER_BUILDER = register("tower_builder", FabricBlockEntityTypeBuilder.create(CarminiteBuilderBlockEntity::new, TFBlocks.CARMINITE_BUILDER).build());
	public static final BlockEntityType<AlphaYetiSpawnerBlockEntity> ALPHA_YETI_SPAWNER = register("alpha_yeti_spawner", FabricBlockEntityTypeBuilder.create(AlphaYetiSpawnerBlockEntity::new, TFBlocks.ALPHA_YETI_BOSS_SPAWNER).build());
	public static final BlockEntityType<FinalBossSpawnerBlockEntity> FINAL_BOSS_SPAWNER = register("final_boss_spawner", FabricBlockEntityTypeBuilder.create(FinalBossSpawnerBlockEntity::new, TFBlocks.FINAL_BOSS_BOSS_SPAWNER).build());
	public static final BlockEntityType<HydraSpawnerBlockEntity> HYDRA_SPAWNER = register("hydra_boss_spawner", FabricBlockEntityTypeBuilder.create(HydraSpawnerBlockEntity::new, TFBlocks.HYDRA_BOSS_SPAWNER).build());
	public static final BlockEntityType<KnightPhantomSpawnerBlockEntity> KNIGHT_PHANTOM_SPAWNER = register("knight_phantom_spawner", FabricBlockEntityTypeBuilder.create(KnightPhantomSpawnerBlockEntity::new, TFBlocks.KNIGHT_PHANTOM_BOSS_SPAWNER).build());
	public static final BlockEntityType<LichSpawnerBlockEntity> LICH_SPAWNER = register("lich_spawner", FabricBlockEntityTypeBuilder.create(LichSpawnerBlockEntity::new, TFBlocks.LICH_BOSS_SPAWNER).build());
	public static final BlockEntityType<MinoshroomSpawnerBlockEntity> MINOSHROOM_SPAWNER = register("minoshroom_spawner", FabricBlockEntityTypeBuilder.create(MinoshroomSpawnerBlockEntity::new, TFBlocks.MINOSHROOM_BOSS_SPAWNER).build());
	public static final BlockEntityType<NagaSpawnerBlockEntity> NAGA_SPAWNER = register("naga_spawner", FabricBlockEntityTypeBuilder.create(NagaSpawnerBlockEntity::new, TFBlocks.NAGA_BOSS_SPAWNER).build());
	public static final BlockEntityType<SnowQueenSpawnerBlockEntity> SNOW_QUEEN_SPAWNER = register("snow_queen_spawner", FabricBlockEntityTypeBuilder.create(SnowQueenSpawnerBlockEntity::new, TFBlocks.SNOW_QUEEN_BOSS_SPAWNER).build());
	public static final BlockEntityType<UrGhastSpawnerBlockEntity> UR_GHAST_SPAWNER = register("tower_boss_spawner", FabricBlockEntityTypeBuilder.create(UrGhastSpawnerBlockEntity::new, TFBlocks.UR_GHAST_BOSS_SPAWNER).build());
	public static final BlockEntityType<CicadaBlockEntity> CICADA = register("cicada", FabricBlockEntityTypeBuilder.create(CicadaBlockEntity::new, TFBlocks.CICADA).build());
	public static final BlockEntityType<FireflyBlockEntity> FIREFLY = register("firefly", FabricBlockEntityTypeBuilder.create(FireflyBlockEntity::new, TFBlocks.FIREFLY).build());
	public static final BlockEntityType<MoonwormBlockEntity> MOONWORM = register("moonworm", FabricBlockEntityTypeBuilder.create(MoonwormBlockEntity::new, TFBlocks.MOONWORM).build());
	public static final BlockEntityType<SkullChestBlockEntity> SKULL_CHEST = register("skull_chest", FabricBlockEntityTypeBuilder.create(SkullChestBlockEntity::new, TFBlocks.SKULL_CHEST).build());
	public static final BlockEntityType<KeepsakeCasketBlockEntity> KEEPSAKE_CASKET = register("keepsake_casket", FabricBlockEntityTypeBuilder.create(KeepsakeCasketBlockEntity::new, TFBlocks.KEEPSAKE_CASKET).build());
	public static final BlockEntityType<BrazierBlockEntity> BRAZIER = register("brazier", FabricBlockEntityTypeBuilder.create(BrazierBlockEntity::new, TFBlocks.BRAZIER).build());
	public static final BlockEntityType<ChiseledCanopyShelfBlockEntity> CHISELED_CANOPY_BOOKSHELF = register("chiseled_canopy_bookshelf", FabricBlockEntityTypeBuilder.create(ChiseledCanopyShelfBlockEntity::new, TFBlocks.CHISELED_CANOPY_BOOKSHELF).build());
	public static final BlockEntityType<GrowingBeanstalkBlockEntity> BEANSTALK_GROWER = register("beanstalk_grower", FabricBlockEntityTypeBuilder.create(GrowingBeanstalkBlockEntity::new, TFBlocks.BEANSTALK_GROWER).build());
	public static final BlockEntityType<RedThreadBlockEntity> RED_THREAD = register("red_thread", FabricBlockEntityTypeBuilder.create(RedThreadBlockEntity::new, TFBlocks.RED_THREAD).build());
	public static final BlockEntityType<CandelabraBlockEntity> CANDELABRA = register("candelabra", FabricBlockEntityTypeBuilder.create(CandelabraBlockEntity::new, TFBlocks.CANDELABRA).build());
	public static final BlockEntityType<JarBlockEntity> JAR = register("jar", FabricBlockEntityTypeBuilder.create(JarBlockEntity::new, TFBlocks.FIREFLY_JAR, TFBlocks.CICADA_JAR).build());
	public static final BlockEntityType<MasonJarBlockEntity> MASON_JAR = register("mason_jar", FabricBlockEntityTypeBuilder.create(MasonJarBlockEntity::new, TFBlocks.MASON_JAR).build());
	public static final BlockEntityType<SinisterSpawnerBlockEntity> SINISTER_SPAWNER = register("sinister_spawner", FabricBlockEntityTypeBuilder.create(SinisterSpawnerBlockEntity::new, TFBlocks.SINISTER_SPAWNER.get()).build());
	public static final BlockEntityType<DryingRackBlockEntity> DRYING_RACK = register("drying_rack", FabricBlockEntityTypeBuilder.create(DryingRackBlockEntity::new,
		TFBlocks.OAK_DRYING_RACK.get(), TFBlocks.SPRUCE_DRYING_RACK.get(),
		TFBlocks.BIRCH_DRYING_RACK.get(), TFBlocks.JUNGLE_DRYING_RACK.get(),
		TFBlocks.ACACIA_DRYING_RACK.get(), TFBlocks.DARK_OAK_DRYING_RACK.get(),
		TFBlocks.CRIMSON_DRYING_RACK.get(), TFBlocks.WARPED_DRYING_RACK.get(),
		TFBlocks.VANGROVE_DRYING_RACK.get(), TFBlocks.BAMBOO_DRYING_RACK.get(),
		TFBlocks.CHERRY_DRYING_RACK.get(), TFBlocks.TWILIGHT_OAK_DRYING_RACK.get(),
		TFBlocks.CANOPY_DRYING_RACK.get(), TFBlocks.MANGROVE_DRYING_RACK.get(),
		TFBlocks.DARK_DRYING_RACK.get(), TFBlocks.TIME_DRYING_RACK.get(),
		TFBlocks.TRANSFORMATION_DRYING_RACK.get(), TFBlocks.MINING_DRYING_RACK.get(),
		TFBlocks.SORTING_DRYING_RACK.get()).build());

	public static final BlockEntityType<TrophyBlockEntity> TROPHY = register("trophy", FabricBlockEntityTypeBuilder.create(TrophyBlockEntity::new,
		TFBlocks.NAGA_TROPHY, TFBlocks.LICH_TROPHY, TFBlocks.MINOSHROOM_TROPHY,
		TFBlocks.HYDRA_TROPHY, TFBlocks.KNIGHT_PHANTOM_TROPHY, TFBlocks.UR_GHAST_TROPHY, TFBlocks.ALPHA_YETI_TROPHY,
		TFBlocks.SNOW_QUEEN_TROPHY, TFBlocks.QUEST_RAM_TROPHY, TFBlocks.NAGA_WALL_TROPHY, TFBlocks.LICH_WALL_TROPHY,
		TFBlocks.MINOSHROOM_WALL_TROPHY, TFBlocks.HYDRA_WALL_TROPHY, TFBlocks.KNIGHT_PHANTOM_WALL_TROPHY, TFBlocks.UR_GHAST_WALL_TROPHY,
		TFBlocks.ALPHA_YETI_WALL_TROPHY, TFBlocks.SNOW_QUEEN_WALL_TROPHY, TFBlocks.QUEST_RAM_WALL_TROPHY).build());

	public static final BlockEntityType<TFChestBlockEntity> TF_CHEST = register("tf_chest", FabricBlockEntityTypeBuilder.create(TFChestBlockEntity::new,
		TFBlocks.TWILIGHT_OAK_CHEST.get(), TFBlocks.CANOPY_CHEST.get(), TFBlocks.MANGROVE_CHEST.get(),
		TFBlocks.DARK_CHEST.get(), TFBlocks.TIME_CHEST.get(), TFBlocks.TRANSFORMATION_CHEST.get(),
		TFBlocks.MINING_CHEST.get(), TFBlocks.SORTING_CHEST.get()).build());

	public static final BlockEntityType<TFTrappedChestBlockEntity> TF_TRAPPED_CHEST = register("tf_trapped_chest", FabricBlockEntityTypeBuilder.create(TFTrappedChestBlockEntity::new,
		TFBlocks.TWILIGHT_OAK_TRAPPED_CHEST.get(), TFBlocks.CANOPY_TRAPPED_CHEST.get(), TFBlocks.MANGROVE_TRAPPED_CHEST.get(),
		TFBlocks.DARK_TRAPPED_CHEST.get(), TFBlocks.TIME_TRAPPED_CHEST.get(), TFBlocks.TRANSFORMATION_TRAPPED_CHEST.get(),
		TFBlocks.MINING_TRAPPED_CHEST.get(), TFBlocks.SORTING_TRAPPED_CHEST.get()).build());

	public static final BlockEntityType<SkullCandleBlockEntity> SKULL_CANDLE = register("skull_candle", FabricBlockEntityTypeBuilder.create(SkullCandleBlockEntity::new,
		TFBlocks.ZOMBIE_SKULL_CANDLE, TFBlocks.ZOMBIE_WALL_SKULL_CANDLE,
		TFBlocks.SKELETON_SKULL_CANDLE, TFBlocks.SKELETON_WALL_SKULL_CANDLE,
		TFBlocks.WITHER_SKELE_SKULL_CANDLE, TFBlocks.WITHER_SKELE_WALL_SKULL_CANDLE,
		TFBlocks.CREEPER_SKULL_CANDLE, TFBlocks.CREEPER_WALL_SKULL_CANDLE,
		TFBlocks.PLAYER_SKULL_CANDLE, TFBlocks.PLAYER_WALL_SKULL_CANDLE,
		TFBlocks.PIGLIN_SKULL_CANDLE, TFBlocks.PIGLIN_WALL_SKULL_CANDLE).build());

	public static final BlockEntityType<OminousCandleBlockEntity> OMINOUS_CANDLE = register("ominous_candle", FabricBlockEntityTypeBuilder.create(OminousCandleBlockEntity::new,
		TFBlocks.OMINOUS_CANDLE, TFBlocks.OMINOUS_WHITE_CANDLE,
		TFBlocks.OMINOUS_ORANGE_CANDLE, TFBlocks.OMINOUS_MAGENTA_CANDLE,
		TFBlocks.OMINOUS_LIGHT_BLUE_CANDLE, TFBlocks.OMINOUS_YELLOW_CANDLE,
		TFBlocks.OMINOUS_LIME_CANDLE, TFBlocks.OMINOUS_PINK_CANDLE,
		TFBlocks.OMINOUS_GRAY_CANDLE, TFBlocks.OMINOUS_LIGHT_GRAY_CANDLE,
		TFBlocks.OMINOUS_CYAN_CANDLE, TFBlocks.OMINOUS_PURPLE_CANDLE,
		TFBlocks.OMINOUS_BLUE_CANDLE, TFBlocks.OMINOUS_BROWN_CANDLE,
		TFBlocks.OMINOUS_GREEN_CANDLE, TFBlocks.OMINOUS_RED_CANDLE,
		TFBlocks.OMINOUS_BLACK_CANDLE).build());

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		BLOCK_ENTITIES.forEach((id, type) -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type));
	}

	private static <T extends BlockEntityType<?>> T register(String name, T type) {
		BLOCK_ENTITIES.put(TwilightForestMod.prefix(name), type);
		return type;
	}
}
