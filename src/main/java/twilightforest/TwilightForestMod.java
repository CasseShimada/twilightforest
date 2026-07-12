package twilightforest;

import com.google.common.base.Suppliers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twilightforest.block.entity.JarBlockEntity;
import twilightforest.command.TFCommand;
import twilightforest.dispenser.TFDispenserBehaviors;
import twilightforest.entity.MagicPaintingVariant;
import twilightforest.entity.passive.DwarfRabbitVariant;
import twilightforest.entity.passive.TinyBirdVariant;
import twilightforest.entity.passive.quest.QuestReloadListener;
import twilightforest.init.*;
import twilightforest.item.recipe.UncraftingTableCondition;
import twilightforest.init.custom.BiomeLayerStack;
import twilightforest.init.custom.BiomeLayerTypes;
import twilightforest.init.custom.ChunkBlanketProcessors;
import twilightforest.init.custom.Enforcements;
import twilightforest.events.TFEventHandlers;
import twilightforest.events.LootEvents;
import twilightforest.network.EnforceProgressionStatusPacket;
import twilightforest.network.TFNetworking;
import twilightforest.util.HolidayEvent;
import twilightforest.util.Restriction;
import twilightforest.util.SaveDebug;
import twilightforest.util.TFRemapper;
import twilightforest.util.woods.WoodPalette;
import twilightforest.mixin.accessor.CauldronInteractionDispatcherAccessor;
import twilightforest.world.components.biomesources.TFBiomeProvider;
import twilightforest.world.components.layer.BiomeDensitySource;
import twilightforest.world.components.spelothem.StalactiteReloadListener;
import twilightforest.world.components.spelothem.StructureSpeleothemConfig;
import twilightforest.world.components.structures.lichtowerrevamp.StructureTemplateDefinitions;

import java.util.Locale;
import java.util.function.Supplier;

public final class TwilightForestMod implements ModInitializer {

	public static final String ID = "twilightforest";

	private static final String MODEL_DIR = "textures/entity/";
	private static final String GUI_DIR = "textures/gui/";
	private static final String ENVIRO_DIR = "textures/environment/";

	public static final Logger LOGGER = LogManager.getLogger(ID);

	public static final Supplier<GameRule<Boolean>> ENFORCED_PROGRESSION_RULE = Suppliers.memoize(() ->
		GameRuleBuilder.forBoolean(true)
			.category(GameRuleCategory.UPDATES)
			.buildAndRegister(prefix("tf_enforced_progression"))
	);

	@Override
	public void onInitialize() {
		TFNetworking.init();
		TFDataAttachments.init();
		ServerLifecycleEvents.SERVER_STOPPING.register(SaveDebug::onServerStopping);
		ServerLifecycleEvents.SERVER_STOPPED.register(SaveDebug::onServerStopped);
		ResourceConditions.register(UncraftingTableCondition.TYPE);
		TFRegistries.bootstrap();
		// Registries
		registerStaticRegistries();
		registerDynamicRegistries();
		registerReloadListeners();
		registerCommands();
		registerGameRules();

		// Common setup
		initCommon();
	}

	private static void registerStaticRegistries() {
		TFSounds.init();
		TFDataComponents.register();
		TFRemapper.addBlockAliases();
		TFBlocks.register();
		TFRemapper.addEntityAliases();
		TFEntities.init();
		TFRemapper.addItemAliases();
		TFItems.init();
		TFLoot.init();
		TFPOITypes.init();
		TFFeatures.init();
		TFCreativeTabs.init();
		TFRemapper.addSpawnEggAliases();
		TFEntities.registerSpawnEggs();
		TFMenuTypes.init();
		TFRecipes.init();
		TFAttributes.init();
		TFAdvancements.init();
		TFMobEffects.init();
		TFItemSubPredicates.init();
		Enforcements.init();
		TFCaveCarvers.init();
		TFMapDecorations.init();
		TFParticleType.init();
		TFBlockEntities.init();
		TFStructureTypes.init();
		TFBiomeSources.init();
		BiomeLayerTypes.init();
		TFDataSerializers.init();
		TFFeatureModifiers.init();
		TFEnchantmentEffects.init();
		TFDensityFunctions.init();
		TFRemapper.addStructureProcessorAliases();
		TFStructureProcessors.init();
		TFRemapper.addStructurePieceAliases();
		TFStructurePieceTypes.init();
		ChunkBlanketProcessors.init();
		TFStructurePlacementTypes.init();
	}

	private static void registerDynamicRegistries() {
		DynamicRegistries.register(TFRegistries.Keys.WOOD_PALETTES, WoodPalette.CODEC);
		DynamicRegistries.register(TFRegistries.Keys.BIOME_STACK, BiomeLayerStack.DISPATCH_CODEC);
		DynamicRegistries.register(TFRegistries.Keys.BIOME_TERRAIN_DATA, BiomeDensitySource.CODEC);
		DynamicRegistries.registerSynced(TFRegistries.Keys.RESTRICTIONS, Restriction.CODEC, Restriction.CODEC);
		DynamicRegistries.registerSynced(TFRegistries.Keys.MAGIC_PAINTINGS, MagicPaintingVariant.CODEC, MagicPaintingVariant.CODEC);
		DynamicRegistries.register(TFRegistries.Keys.STRUCTURE_SPELEOTHEM_SETTINGS, StructureSpeleothemConfig.CODEC);
		DynamicRegistries.register(TFRegistries.Keys.CHUNK_BLANKET_PROCESSORS, ChunkBlanketProcessors.DISPATCH_CODEC);
		DynamicRegistries.registerSynced(TFRegistries.Keys.DWARF_RABBIT_VARIANT, DwarfRabbitVariant.DIRECT_CODEC, DwarfRabbitVariant.DIRECT_CODEC);
		DynamicRegistries.registerSynced(TFRegistries.Keys.TINY_BIRD_VARIANT, TinyBirdVariant.DIRECT_CODEC, TinyBirdVariant.DIRECT_CODEC);
	}

	private static void registerReloadListeners() {
		TFDataMaps.registerReloadListener();
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(StalactiteReloadListener.INSTANCE);
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(StructureTemplateDefinitions.INSTANCE);
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new QuestReloadListener());
	}

	private static void registerCommands() {
		TFCommand tfCommand = new TFCommand();
		CommandRegistrationCallback.EVENT.register(tfCommand::register);
	}

	private static void registerGameRules() {
		GameRule<Boolean> rule = ENFORCED_PROGRESSION_RULE.get();
		GameRuleEvents.changeCallback(rule).register((enforced, server) -> {
			EnforceProgressionStatusPacket packet = new EnforceProgressionStatusPacket(enforced);
			PlayerLookup.all(server).forEach(player -> ServerPlayNetworking.send(player, packet));
		});
	}

	private static void initCommon() {
		TFDispenserBehaviors.init();
		TFStats.init();
		TFEventHandlers.register();
		TFCreativeTabs.registerVanillaTabs();

		CauldronInteractionDispatcherAccessor twilightforest$waterCauldron = (CauldronInteractionDispatcherAccessor) (Object) CauldronInteractions.WATER;
		twilightforest$waterCauldron.twilightforest$put(TFItems.ARCTIC_HELMET, TwilightForestMod::cleanDyedItem);
		twilightforest$waterCauldron.twilightforest$put(TFItems.ARCTIC_CHESTPLATE, TwilightForestMod::cleanDyedItem);
		twilightforest$waterCauldron.twilightforest$put(TFItems.ARCTIC_LEGGINGS, TwilightForestMod::cleanDyedItem);
		twilightforest$waterCauldron.twilightforest$put(TFItems.ARCTIC_BOOTS, TwilightForestMod::cleanDyedItem);

		StrippableBlockRegistry.register(TFBlocks.TWILIGHT_OAK_LOG, TFBlocks.STRIPPED_TWILIGHT_OAK_LOG);
		StrippableBlockRegistry.register(TFBlocks.CANOPY_LOG, TFBlocks.STRIPPED_CANOPY_LOG);
		StrippableBlockRegistry.register(TFBlocks.MANGROVE_LOG, TFBlocks.STRIPPED_MANGROVE_LOG);
		StrippableBlockRegistry.register(TFBlocks.DARK_LOG, TFBlocks.STRIPPED_DARK_LOG);
		StrippableBlockRegistry.register(TFBlocks.TIME_LOG, TFBlocks.STRIPPED_TIME_LOG);
		StrippableBlockRegistry.register(TFBlocks.TRANSFORMATION_LOG, TFBlocks.STRIPPED_TRANSFORMATION_LOG);
		StrippableBlockRegistry.register(TFBlocks.MINING_LOG, TFBlocks.STRIPPED_MINING_LOG);
		StrippableBlockRegistry.register(TFBlocks.SORTING_LOG, TFBlocks.STRIPPED_SORTING_LOG);

		StrippableBlockRegistry.register(TFBlocks.TWILIGHT_OAK_WOOD, TFBlocks.STRIPPED_TWILIGHT_OAK_WOOD);
		StrippableBlockRegistry.register(TFBlocks.CANOPY_WOOD, TFBlocks.STRIPPED_CANOPY_WOOD);
		StrippableBlockRegistry.register(TFBlocks.MANGROVE_WOOD, TFBlocks.STRIPPED_MANGROVE_WOOD);
		StrippableBlockRegistry.register(TFBlocks.DARK_WOOD, TFBlocks.STRIPPED_DARK_WOOD);
		StrippableBlockRegistry.register(TFBlocks.TIME_WOOD, TFBlocks.STRIPPED_TIME_WOOD);
		StrippableBlockRegistry.register(TFBlocks.TRANSFORMATION_WOOD, TFBlocks.STRIPPED_TRANSFORMATION_WOOD);
		StrippableBlockRegistry.register(TFBlocks.MINING_WOOD, TFBlocks.STRIPPED_MINING_WOOD);
		StrippableBlockRegistry.register(TFBlocks.SORTING_WOOD, TFBlocks.STRIPPED_SORTING_WOOD);

		FuelValueEvents.BUILD.register((builder, context) -> {
			int burnTime = 300;
			builder.add(TFItems.HOLLOW_TWILIGHT_OAK_LOG, burnTime);
			builder.add(TFItems.HOLLOW_CANOPY_LOG, burnTime);
			builder.add(TFItems.HOLLOW_MANGROVE_LOG, burnTime);
			builder.add(TFItems.HOLLOW_DARK_LOG, burnTime);
			builder.add(TFItems.HOLLOW_TIME_LOG, burnTime);
			builder.add(TFItems.HOLLOW_TRANSFORMATION_LOG, burnTime);
			builder.add(TFItems.HOLLOW_MINING_LOG, burnTime);
			builder.add(TFItems.HOLLOW_SORTING_LOG, burnTime);
			builder.add(TFItems.HOLLOW_OAK_LOG, burnTime);
			builder.add(TFItems.HOLLOW_SPRUCE_LOG, burnTime);
			builder.add(TFItems.HOLLOW_BIRCH_LOG, burnTime);
			builder.add(TFItems.HOLLOW_JUNGLE_LOG, burnTime);
			builder.add(TFItems.HOLLOW_ACACIA_LOG, burnTime);
			builder.add(TFItems.HOLLOW_DARK_OAK_LOG, burnTime);
			builder.add(TFItems.HOLLOW_CRIMSON_STEM, burnTime);
			builder.add(TFItems.HOLLOW_WARPED_STEM, burnTime);
			builder.add(TFItems.HOLLOW_VANGROVE_LOG, burnTime);
			builder.add(TFItems.HOLLOW_CHERRY_LOG, burnTime);
			builder.add(TFItems.HOLLOW_PALE_OAK_LOG, burnTime);
		});

		FlammableBlockRegistry flammables = FlammableBlockRegistry.getDefaultInstance();
		flammables.add(TFBlocks.TWILIGHT_OAK_LOG, 5, 5);
		flammables.add(TFBlocks.TWILIGHT_OAK_WOOD, 5, 5);
		flammables.add(TFBlocks.STRIPPED_TWILIGHT_OAK_LOG, 5, 5);
		flammables.add(TFBlocks.STRIPPED_TWILIGHT_OAK_WOOD, 5, 5);
		flammables.add(TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_HORIZONTAL, 5, 5);
		flammables.add(TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_VERTICAL, 5, 5);
		flammables.add(TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_CLIMBABLE, 5, 5);
		flammables.add(TFBlocks.TWILIGHT_OAK_BANISTER, 5, 20);
		flammables.add(TFBlocks.TWILIGHT_OAK_PLANKS, 5, 20);
		flammables.add(TFBlocks.TWILIGHT_OAK_SLAB, 5, 20);
		flammables.add(TFBlocks.TWILIGHT_OAK_STAIRS, 5, 20);
		flammables.add(TFBlocks.TWILIGHT_OAK_FENCE, 5, 20);
		flammables.add(TFBlocks.TWILIGHT_OAK_GATE, 5, 20);

		flammables.add(TFBlocks.CANOPY_LOG, 5, 5);
		flammables.add(TFBlocks.CANOPY_WOOD, 5, 5);
		flammables.add(TFBlocks.STRIPPED_CANOPY_LOG, 5, 5);
		flammables.add(TFBlocks.STRIPPED_CANOPY_WOOD, 5, 5);
		flammables.add(TFBlocks.HOLLOW_CANOPY_LOG_HORIZONTAL, 5, 5);
		flammables.add(TFBlocks.HOLLOW_CANOPY_LOG_VERTICAL, 5, 5);
		flammables.add(TFBlocks.HOLLOW_CANOPY_LOG_CLIMBABLE, 5, 5);
		flammables.add(TFBlocks.CANOPY_BANISTER, 5, 20);
		flammables.add(TFBlocks.CANOPY_PLANKS, 5, 20);
		flammables.add(TFBlocks.CANOPY_SLAB, 5, 20);
		flammables.add(TFBlocks.CANOPY_STAIRS, 5, 20);
		flammables.add(TFBlocks.CANOPY_FENCE, 5, 20);
		flammables.add(TFBlocks.CANOPY_GATE, 5, 20);
		flammables.add(TFBlocks.CANOPY_BOOKSHELF, 5, 20);

		LootEvents.GIANT_PICK_CONVERSIONS.put(Blocks.COBBLESTONE, TFBlocks.GIANT_COBBLESTONE.asItem());
		LootEvents.GIANT_PICK_CONVERSIONS.put(Blocks.OAK_LOG, TFBlocks.GIANT_LOG.asItem());
		LootEvents.GIANT_PICK_CONVERSIONS.put(Blocks.OAK_LEAVES, TFBlocks.GIANT_LEAVES.asItem());
		LootEvents.GIANT_PICK_CONVERSIONS.put(Blocks.OBSIDIAN, TFBlocks.GIANT_OBSIDIAN.asItem());

		// Jar lids
		HolidayEvent holidayEvent = new HolidayEvent();
		JarBlockEntity.addLid(Items.PUMPKIN, holidayEvent::isHalloweenWeek);
	}

	private static InteractionResult cleanDyedItem(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
		if (!stack.has(DataComponents.DYED_COLOR)) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			stack.remove(DataComponents.DYED_COLOR);
			player.awardStat(Stats.CLEAN_ARMOR);
			LayeredCauldronBlock.lowerFillLevel(state, level, pos);
		}

		return InteractionResult.SUCCESS;
	}

	public static Identifier prefix(String name) {
		return Identifier.fromNamespaceAndPath(ID, name.toLowerCase(Locale.ROOT));
	}

	public static Identifier getModelTexture(String name) {
		return Identifier.fromNamespaceAndPath(ID, MODEL_DIR + name);
	}

	public static Identifier getGuiTexture(String name) {
		return Identifier.fromNamespaceAndPath(ID, GUI_DIR + name);
	}

	public static Identifier getEnvTexture(String name) {
		return Identifier.fromNamespaceAndPath(ID, ENVIRO_DIR + name);
	}
}
