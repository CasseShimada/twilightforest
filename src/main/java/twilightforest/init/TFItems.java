package twilightforest.init;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;
import twilightforest.TwilightForestMod;
import twilightforest.util.registry.DeferredItem;
import twilightforest.components.item.PotionFlaskComponent;
import twilightforest.item.*;
import twilightforest.item.food.TFConsumables;
import twilightforest.item.food.TFFoods;
import twilightforest.tags.TFBannerPatternTags;
import twilightforest.util.TFToolMaterials;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class TFItems {

	private static final Rarity TWILIGHT_RARITY = Rarity.RARE;

	private static final Map<Identifier, ItemEntry> ITEMS = new LinkedHashMap<>();
	private static final Map<Identifier, Identifier> ITEM_ALIASES = new LinkedHashMap<>();
	private static boolean registered;

	public static final Item NAGA_SCALE = registerDirect("naga_scale", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item NAGA_CHESTPLATE = registerDirect("naga_chestplate", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.NAGA, ArmorType.CHESTPLATE)), () -> new Item.Properties().durability(ArmorType.CHESTPLATE.getDurability(21)).rarity(Rarity.UNCOMMON));
	public static final Item NAGA_LEGGINGS = registerDirect("naga_leggings", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.NAGA, ArmorType.LEGGINGS)), () -> new Item.Properties().durability(ArmorType.LEGGINGS.getDurability(21)).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> TWILIGHT_SCEPTER = register("twilight_scepter", TwilightWandItem::new, () -> new Item.Properties().durability(99).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> LIFEDRAIN_SCEPTER = register("lifedrain_scepter", LifedrainScepterItem::new, () -> new Item.Properties().durability(99).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> ZOMBIE_SCEPTER = register("zombie_scepter", ZombieWandItem::new, () -> new Item.Properties().durability(9).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> FORTIFICATION_SCEPTER = register("fortification_scepter", FortificationWandItem::new, () -> new Item.Properties().durability(9).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> EXANIMATE_ESSENCE = register("exanimate_essence", ExanimateEssenceItem::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16));
	public static final Item WROUGHT_IRON_BAR = registerDirect("wrought_iron_bar", Item::new, Item.Properties::new);
	//items.register("Wand of Pacification [NYI]", new Item().setIconIndex(6).setTranslationKey("wandPacification").setMaxStackSize(1));
	public static final DeferredItem<Item> MAGIC_PAINTING = register("magic_painting", MagicPaintingItem::new, Item.Properties::new);
	public static final DeferredItem<Item> ORE_METER = register("ore_meter", OreMeterItem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item FILLED_MAGIC_MAP = registerDirect("filled_magic_map", MagicMapItem::new, Item.Properties::new);
	public static final Item FILLED_MAZE_MAP = registerDirect("filled_maze_map", properties -> new MazeMapItem(false, properties), Item.Properties::new);
	public static final Item FILLED_ORE_MAP = registerDirect("filled_ore_map", properties -> new MazeMapItem(true, properties), Item.Properties::new);
	public static final Item RAVEN_FEATHER = registerDirect("raven_feather", Item::new, Item.Properties::new);
	public static final Item MAGIC_MAP_FOCUS = registerDirect("magic_map_focus", Item::new, Item.Properties::new);
	public static final Item MAZE_MAP_FOCUS = registerDirect("maze_map_focus", Item::new, Item.Properties::new);
	public static final Item MAGIC_MAP = registerDirect("magic_map", EmptyMagicMapItem::new, Item.Properties::new);
	public static final Item MAZE_MAP = registerDirect("maze_map", properties -> new EmptyMazeMapItem(false, properties), Item.Properties::new);
	public static final Item ORE_MAP = registerDirect("ore_map", properties -> new EmptyMazeMapItem(true, properties), Item.Properties::new);
	public static final Item LIVEROOT = registerDirect("liveroot", Item::new, Item.Properties::new);
	public static final Item RAW_IRONWOOD = registerDirect("raw_ironwood", Item::new, Item.Properties::new);
	public static final Item IRONWOOD_INGOT = registerDirect("ironwood_ingot", Item::new, Item.Properties::new);
	public static final Item IRONWOOD_HELMET = registerDirect("ironwood_helmet", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.IRONWOOD, ArmorType.HELMET)), () -> new Item.Properties().durability(ArmorType.HELMET.getDurability(20)));
	public static final Item IRONWOOD_CHESTPLATE = registerDirect("ironwood_chestplate", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.IRONWOOD, ArmorType.CHESTPLATE)), () -> new Item.Properties().durability(ArmorType.CHESTPLATE.getDurability(20)));
	public static final Item IRONWOOD_LEGGINGS = registerDirect("ironwood_leggings", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.IRONWOOD, ArmorType.LEGGINGS)), () -> new Item.Properties().durability(ArmorType.LEGGINGS.getDurability(20)));
	public static final Item IRONWOOD_BOOTS = registerDirect("ironwood_boots", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.IRONWOOD, ArmorType.BOOTS)), () -> new Item.Properties().durability(ArmorType.BOOTS.getDurability(20)));
	public static final DeferredItem<Item> IRONWOOD_SWORD = register("ironwood_sword", properties -> new Item(properties.sword(TFToolMaterials.IRONWOOD, 3.0F, -2.4F)), Item.Properties::new);
	public static final DeferredItem<Item> IRONWOOD_SHOVEL = register("ironwood_shovel", properties -> new ShovelItem(TFToolMaterials.IRONWOOD, 1.5F, -3.0F, properties), Item.Properties::new);
	public static final DeferredItem<Item> IRONWOOD_PICKAXE = register("ironwood_pickaxe", properties -> new Item(properties.pickaxe(TFToolMaterials.IRONWOOD, 1.0F, -2.8F)), Item.Properties::new);
	public static final DeferredItem<Item> IRONWOOD_AXE = register("ironwood_axe", properties -> new AxeItem(TFToolMaterials.IRONWOOD, 6.0F, -3.1F, properties), Item.Properties::new);
	public static final DeferredItem<Item> IRONWOOD_HOE = register("ironwood_hoe", properties -> new HoeItem(TFToolMaterials.IRONWOOD, -2, -1.0F, properties), Item.Properties::new);
	public static final Item TORCHBERRIES = registerDirect("torchberries", Item::new, () -> new Item.Properties().food(TFFoods.TORCHBERRIES, TFConsumables.TORCHBERRIES));
	public static final Item RAW_VENISON = registerDirect("raw_venison", Item::new, () -> new Item.Properties().food(TFFoods.RAW_VENISON));
	public static final Item COOKED_VENISON = registerDirect("cooked_venison", Item::new, () -> new Item.Properties().food(TFFoods.VENISON_STEAK));
	public static final Item HYDRA_CHOP = registerDirect("hydra_chop", HydraChopItem::new, () -> new Item.Properties().fireResistant().food(TFFoods.HYDRA_CHOP, TFConsumables.HYDRA_CHOP).rarity(Rarity.UNCOMMON));
	public static final Item FIERY_BLOOD = registerDirect("fiery_blood", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item FIERY_TEARS = registerDirect("fiery_tears", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item FIERY_INGOT = registerDirect("fiery_ingot", Item::new, () -> new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> FIERY_HELMET = register("fiery_helmet", properties -> new FieryArmorItem(TFArmorMaterials.FIERY, ArmorType.HELMET, properties), () -> new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> FIERY_CHESTPLATE = register("fiery_chestplate", properties -> new FieryArmorItem(TFArmorMaterials.FIERY, ArmorType.CHESTPLATE, properties), () -> new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> FIERY_LEGGINGS = register("fiery_leggings", properties -> new FieryArmorItem(TFArmorMaterials.FIERY, ArmorType.LEGGINGS, properties), () -> new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> FIERY_BOOTS = register("fiery_boots", properties -> new FieryArmorItem(TFArmorMaterials.FIERY, ArmorType.BOOTS, properties), () -> new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> FIERY_SWORD = register("fiery_sword", properties -> new FierySwordItem(TFToolMaterials.FIERY, properties), () -> new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> FIERY_PICKAXE = register("fiery_pickaxe", properties -> new FieryPickItem(TFToolMaterials.FIERY, properties), () -> new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
	public static final Item STEELEAF_INGOT = registerDirect("steeleaf_ingot", Item::new, Item.Properties::new);
	public static final Item STEELEAF_HELMET = registerDirect("steeleaf_helmet", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.STEELEAF, ArmorType.HELMET)), Item.Properties::new);
	public static final Item STEELEAF_CHESTPLATE = registerDirect("steeleaf_chestplate", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.STEELEAF, ArmorType.CHESTPLATE)), Item.Properties::new);
	public static final Item STEELEAF_LEGGINGS = registerDirect("steeleaf_leggings", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.STEELEAF, ArmorType.LEGGINGS)), Item.Properties::new);
	public static final Item STEELEAF_BOOTS = registerDirect("steeleaf_boots", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.STEELEAF, ArmorType.BOOTS)), Item.Properties::new);
	public static final DeferredItem<Item> STEELEAF_SWORD = register("steeleaf_sword", properties -> new Item(properties.sword(TFToolMaterials.STEELEAF, 3.0F, -2.4F)), Item.Properties::new);
	public static final DeferredItem<Item> STEELEAF_SHOVEL = register("steeleaf_shovel", properties -> new ShovelItem(TFToolMaterials.STEELEAF, 1.5F, -3.0F, properties), Item.Properties::new);
	public static final DeferredItem<Item> STEELEAF_PICKAXE = register("steeleaf_pickaxe", properties -> new Item(properties.pickaxe(TFToolMaterials.STEELEAF, 1.0F, -2.8F)), Item.Properties::new);
	public static final DeferredItem<Item> STEELEAF_AXE = register("steeleaf_axe", properties -> new AxeItem(TFToolMaterials.STEELEAF, 6.0F, -3.0F, properties), Item.Properties::new);
	public static final DeferredItem<Item> STEELEAF_HOE = register("steeleaf_hoe", properties -> new HoeItem(TFToolMaterials.STEELEAF, -3.0F, -0.5F, properties), Item.Properties::new);
	public static final DeferredItem<Item> GOLDEN_MINOTAUR_AXE = register("gold_minotaur_axe", properties -> new MinotaurAxeItem(ToolMaterial.GOLD, properties), Item.Properties::new);
	public static final DeferredItem<Item> DIAMOND_MINOTAUR_AXE = register("diamond_minotaur_axe", properties -> new MinotaurAxeItem(ToolMaterial.DIAMOND, properties), () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> MAZEBREAKER_PICKAXE = register("mazebreaker_pickaxe", properties -> new MazebreakerPickItem(ToolMaterial.DIAMOND, properties), () -> new Item.Properties().rarity(Rarity.RARE));
	public static final Item TRANSFORMATION_POWDER = registerDirect("transformation_powder", TransformPowderItem::new, Item.Properties::new);
	public static final Item RAW_MEEF = registerDirect("raw_meef", Item::new, () -> new Item.Properties().food(TFFoods.RAW_MEEF));
	public static final Item COOKED_MEEF = registerDirect("cooked_meef", Item::new, () -> new Item.Properties().food(TFFoods.MEEF_STEAK));
	public static final Item MEEF_STROGANOFF = registerDirect("meef_stroganoff", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant().food(TFFoods.MEEF_STROGANOFF).usingConvertsTo(Items.BOWL));
	public static final Item MAZE_WAFER = registerDirect("maze_wafer", Item::new, () -> new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
	public static final Item ORE_MAGNET = registerDirect("ore_magnet", OreMagnetItem::new, () -> new Item.Properties().durability(64));
	public static final DeferredItem<Item> CRUMBLE_HORN = register("crumble_horn", CrumbleHornItem::new, () -> new Item.Properties().durability(1024).rarity(Rarity.RARE));
	public static final DeferredItem<Item> PEACOCK_FEATHER_FAN = register("peacock_feather_fan", PeacockFanItem::new, () -> new Item.Properties().durability(1024).rarity(Rarity.RARE));
	public static final DeferredItem<Item> MOONWORM_QUEEN = register("moonworm_queen", MoonwormQueenItem::new, () -> new Item.Properties().durability(256).rarity(Rarity.RARE));
	public static final DeferredItem<Item> BRITTLE_FLASK = register("brittle_potion_flask", BrittleFlaskItem::new, () -> new Item.Properties().component(TFDataComponents.POTION_FLASK_CONTENTS, PotionFlaskComponent.EMPTY));
	public static final DeferredItem<Item> GREATER_FLASK = register("greater_potion_flask", GreaterFlaskItem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant().component(TFDataComponents.POTION_FLASK_CONTENTS, PotionFlaskComponent.EMPTY_UNBREAKABLE));
	public static final DeferredItem<Item> CHARM_OF_LIFE_1 = register("charm_of_life_1", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> CHARM_OF_LIFE_2 = register("charm_of_life_2", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> CHARM_OF_KEEPING_1 = register("charm_of_keeping_1", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> CHARM_OF_KEEPING_2 = register("charm_of_keeping_2", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> CHARM_OF_KEEPING_3 = register("charm_of_keeping_3", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item TOWER_KEY = registerDirect("tower_key", Item::new, () -> new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
	public static final Item BORER_ESSENCE = registerDirect("borer_essence", Item::new, Item.Properties::new);
	public static final Item CARMINITE = registerDirect("carminite", Item::new, Item.Properties::new);
	public static final DeferredItem<Item> EXPERIMENT_115 = register("experiment_115", properties -> new Experiment115Item(TFBlocks.EXPERIMENT_115.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().food(TFFoods.EXPERIMENT_115));
	public static final DeferredItem<Item> ROPE = register("rope", properties -> new RopeItem(TFBlocks.ROPE.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<Item> MASON_JAR = register("mason_jar", properties -> new JarItem.MasonJarItem(TFBlocks.MASON_JAR.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<Item> FIREFLY_JAR = register("firefly_jar", properties -> new JarItem(TFBlocks.FIREFLY_JAR.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<Item> CICADA_JAR = register("cicada_jar", properties -> new JarItem(TFBlocks.CICADA_JAR.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final Item ARMOR_SHARD = registerDirect("armor_shard", Item::new, Item.Properties::new);
	public static final Item ARMOR_SHARD_CLUSTER = registerDirect("armor_shard_cluster", Item::new, Item.Properties::new);
	public static final Item KNIGHTMETAL_INGOT = registerDirect("knightmetal_ingot", Item::new, Item.Properties::new);
	public static final Item KNIGHTMETAL_HELMET = registerDirect("knightmetal_helmet", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.KNIGHTMETAL, ArmorType.HELMET)), Item.Properties::new);
	public static final Item KNIGHTMETAL_CHESTPLATE = registerDirect("knightmetal_chestplate", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.KNIGHTMETAL, ArmorType.CHESTPLATE)), Item.Properties::new);
	public static final Item KNIGHTMETAL_LEGGINGS = registerDirect("knightmetal_leggings", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.KNIGHTMETAL, ArmorType.LEGGINGS)), Item.Properties::new);
	public static final Item KNIGHTMETAL_BOOTS = registerDirect("knightmetal_boots", properties -> new Item(properties.humanoidArmor(TFArmorMaterials.KNIGHTMETAL, ArmorType.BOOTS)), Item.Properties::new);
	public static final DeferredItem<Item> KNIGHTMETAL_SWORD = register("knightmetal_sword", properties -> new KnightmetalSwordItem(TFToolMaterials.KNIGHTMETAL, properties), Item.Properties::new);
	public static final DeferredItem<Item> KNIGHTMETAL_PICKAXE = register("knightmetal_pickaxe", properties -> new KnightmetalPickItem(TFToolMaterials.KNIGHTMETAL, properties), Item.Properties::new);
	public static final DeferredItem<Item> KNIGHTMETAL_AXE = register("knightmetal_axe", properties -> new KnightmetalAxeItem(TFToolMaterials.KNIGHTMETAL, properties), Item.Properties::new);
	public static final Item KNIGHTMETAL_RING = registerDirect("knightmetal_ring", Item::new, Item.Properties::new);
	public static final DeferredItem<Item> KNIGHTMETAL_SHIELD = register("knightmetal_shield", KnightmetalShieldItem::new, () -> new Item.Properties().durability(1024));
	public static final DeferredItem<Item> BLOCK_AND_CHAIN = register("block_and_chain", ChainBlockItem::new, () -> new Item.Properties().durability(99));
	public static final DeferredItem<Item> PHANTOM_HELMET = register("phantom_helmet", properties -> new PhantomArmorItem(TFArmorMaterials.PHANTOM, ArmorType.HELMET, properties), () -> new Item.Properties().durability(ArmorType.HELMET.getDurability(30)).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> PHANTOM_CHESTPLATE = register("phantom_chestplate", properties -> new PhantomArmorItem(TFArmorMaterials.PHANTOM, ArmorType.CHESTPLATE, properties), () -> new Item.Properties().durability(ArmorType.CHESTPLATE.getDurability(30)).rarity(Rarity.UNCOMMON));
	public static final Item ICE_BOMB = registerDirect("ice_bomb", IceBombItem::new, () -> new Item.Properties().stacksTo(16));
	public static final Item ARCTIC_FUR = registerDirect("arctic_fur", Item::new, Item.Properties::new);
	public static final DeferredItem<Item> ARCTIC_HELMET = register("arctic_helmet", properties -> new ArcticArmorItem(TFArmorMaterials.ARCTIC, ArmorType.HELMET, properties), Item.Properties::new);
	public static final DeferredItem<Item> ARCTIC_CHESTPLATE = register("arctic_chestplate", properties -> new ArcticArmorItem(TFArmorMaterials.ARCTIC, ArmorType.CHESTPLATE, properties), Item.Properties::new);
	public static final DeferredItem<Item> ARCTIC_LEGGINGS = register("arctic_leggings", properties -> new ArcticArmorItem(TFArmorMaterials.ARCTIC, ArmorType.LEGGINGS, properties), () -> new Item.Properties().durability(ArmorType.LEGGINGS.getDurability(10)));
	public static final DeferredItem<Item> ARCTIC_BOOTS = register("arctic_boots", properties -> new ArcticArmorItem(TFArmorMaterials.ARCTIC, ArmorType.BOOTS, properties), () -> new Item.Properties().durability(ArmorType.BOOTS.getDurability(10)));
	public static final Item ALPHA_YETI_FUR = registerDirect("alpha_yeti_fur", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> YETI_HELMET = register("yeti_helmet", properties -> new YetiArmorItem(TFArmorMaterials.YETI, ArmorType.HELMET, properties), () -> new Item.Properties().durability(ArmorType.HELMET.getDurability(20)).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> YETI_CHESTPLATE = register("yeti_chestplate", properties -> new YetiArmorItem(TFArmorMaterials.YETI, ArmorType.CHESTPLATE, properties), () -> new Item.Properties().durability(ArmorType.CHESTPLATE.getDurability(20)).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> YETI_LEGGINGS = register("yeti_leggings", properties -> new YetiArmorItem(TFArmorMaterials.YETI, ArmorType.LEGGINGS, properties), () -> new Item.Properties().durability(ArmorType.LEGGINGS.getDurability(20)).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> YETI_BOOTS = register("yeti_boots", properties -> new YetiArmorItem(TFArmorMaterials.YETI, ArmorType.BOOTS, properties), () -> new Item.Properties().durability(ArmorType.BOOTS.getDurability(20)).rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> TRIPLE_BOW = register("triple_bow", TripleBowItem::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON).durability(384));
	public static final DeferredItem<Item> SEEKER_BOW = register("seeker_bow", SeekerBowItem::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON).durability(384));
	public static final DeferredItem<Item> ICE_BOW = register("ice_bow", IceBowItem::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON).durability(384));
	public static final DeferredItem<Item> ENDER_BOW = register("ender_bow", EnderBowItem::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON).durability(384));
	public static final DeferredItem<Item> ICE_SWORD = register("ice_sword", properties -> new IceSwordItem(TFToolMaterials.ICE, properties), Item.Properties::new);
	public static final DeferredItem<Item> GLASS_SWORD = register("glass_sword", properties -> new GlassSwordItem(TFToolMaterials.GLASS, 3, -2.4F, properties), () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item MAGIC_BEANS = registerDirect("magic_beans", MagicBeansItem::new, Item.Properties::new);
	public static final DeferredItem<Item> GIANT_PICKAXE = register("giant_pickaxe", properties -> new GiantPickItem(TFToolMaterials.GIANT, properties), () -> new Item.Properties().pickaxe(TFToolMaterials.GIANT, 8.0F, -3.5F).attributes(GiantPickItem.createGiantAttributes(TFToolMaterials.GIANT, 8, -3.5F)));
	public static final DeferredItem<Item> GIANT_SWORD = register("giant_sword", properties -> new Item(properties.sword(TFToolMaterials.GIANT, 10.0F, -3.5F).attributes(GiantPickItem.createGiantAttributes(TFToolMaterials.GIANT, 10, -3.5F))), Item.Properties::new);
	public static final DeferredItem<Item> LAMP_OF_CINDERS = register("lamp_of_cinders", LampOfCindersItem::new, () -> new Item.Properties().fireResistant().durability(1024).rarity(Rarity.UNCOMMON));
	public static final Item CUBE_TALISMAN = registerDirect("cube_talisman", Item::new, () -> new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> CUBE_OF_ANNIHILATION = register("cube_of_annihilation", CubeOfAnnihilationItem::new, () -> new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON));
	public static final Item MOON_DIAL = registerDirect("moon_dial", MoonDialItem::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item POCKET_WATCH = registerDirect("pocket_watch", PocketWatchItem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item EMPERORS_CLOTH = registerDirect("emperors_cloth", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item FOUR_LEAF_CLOVER = registerDirect("four_leaf_clover", Item::new, Item.Properties::new);
	public static final Item CROWN_SPLINTER = registerDirect("crown_splinter", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> MYSTIC_CROWN = register("mystic_crown", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1).attributes(ItemAttributeModifiers.builder().add(Attributes.ARMOR, new AttributeModifier(Identifier.withDefaultNamespace("armor." + EquipmentSlot.HEAD.getName()), 2.0F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD).build()).equippable(EquipmentSlot.HEAD));

	public static final DeferredItem<Item> KEEPSAKE_CASKET = register("keepsake_casket", KeepsakeCasketItem::new, () -> new Item.Properties().useBlockDescriptionPrefix().rarity(Rarity.UNCOMMON));
	public static final DeferredItem<Item> HUGE_LILY_PAD = register("huge_lily_pad", properties -> new HugeLilyPadItem(TFBlocks.HUGE_LILY_PAD.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<Item> HUGE_WATER_LILY = register("huge_water_lily", properties -> new PlaceOnWaterBlockItem(TFBlocks.HUGE_WATER_LILY.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<Item> FALLEN_LEAVES = register("fallen_leaves", properties -> new FallenLeavesItem(TFBlocks.FALLEN_LEAVES.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());

	public static final DeferredItem<Item> ZOMBIE_SKULL_CANDLE = register("zombie_skull_candle", properties -> new SkullCandleItem(TFBlocks.ZOMBIE_SKULL_CANDLE.get(), TFBlocks.ZOMBIE_WALL_SKULL_CANDLE.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(Rarity.UNCOMMON).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> SKELETON_SKULL_CANDLE = register("skeleton_skull_candle", properties -> new SkullCandleItem(TFBlocks.SKELETON_SKULL_CANDLE.get(), TFBlocks.SKELETON_WALL_SKULL_CANDLE.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(Rarity.UNCOMMON).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> WITHER_SKELETON_SKULL_CANDLE = register("wither_skeleton_skull_candle", properties -> new SkullCandleItem(TFBlocks.WITHER_SKELE_SKULL_CANDLE.get(), TFBlocks.WITHER_SKELE_WALL_SKULL_CANDLE.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(Rarity.UNCOMMON).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> CREEPER_SKULL_CANDLE = register("creeper_skull_candle", properties -> new SkullCandleItem(TFBlocks.CREEPER_SKULL_CANDLE.get(), TFBlocks.CREEPER_WALL_SKULL_CANDLE.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(Rarity.UNCOMMON).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> PLAYER_SKULL_CANDLE = register("player_skull_candle", properties -> new SkullCandleItem(TFBlocks.PLAYER_SKULL_CANDLE.get(), TFBlocks.PLAYER_WALL_SKULL_CANDLE.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(Rarity.UNCOMMON).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> PIGLIN_SKULL_CANDLE = register("piglin_skull_candle", properties -> new SkullCandleItem(TFBlocks.PIGLIN_SKULL_CANDLE.get(), TFBlocks.PIGLIN_WALL_SKULL_CANDLE.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(Rarity.UNCOMMON).equippable(EquipmentSlot.HEAD));

	public static final DeferredItem<Item> NAGA_TROPHY = register("naga_trophy", properties -> new TrophyItem(TFBlocks.NAGA_TROPHY.get(), TFBlocks.NAGA_WALL_TROPHY.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(TWILIGHT_RARITY).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> LICH_TROPHY = register("lich_trophy", properties -> new TrophyItem(TFBlocks.LICH_TROPHY.get(), TFBlocks.LICH_WALL_TROPHY.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(TWILIGHT_RARITY).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> MINOSHROOM_TROPHY = register("minoshroom_trophy", properties -> new TrophyItem(TFBlocks.MINOSHROOM_TROPHY.get(), TFBlocks.MINOSHROOM_WALL_TROPHY.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(TWILIGHT_RARITY).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> HYDRA_TROPHY = register("hydra_trophy", properties -> new TrophyItem(TFBlocks.HYDRA_TROPHY.get(), TFBlocks.HYDRA_WALL_TROPHY.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(TWILIGHT_RARITY).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> KNIGHT_PHANTOM_TROPHY = register("knight_phantom_trophy", properties -> new TrophyItem(TFBlocks.KNIGHT_PHANTOM_TROPHY.get(), TFBlocks.KNIGHT_PHANTOM_WALL_TROPHY.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(TWILIGHT_RARITY).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> UR_GHAST_TROPHY = register("ur_ghast_trophy", properties -> new TrophyItem(TFBlocks.UR_GHAST_TROPHY.get(), TFBlocks.UR_GHAST_WALL_TROPHY.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(TWILIGHT_RARITY).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> ALPHA_YETI_TROPHY = register("alpha_yeti_trophy", properties -> new TrophyItem(TFBlocks.ALPHA_YETI_TROPHY.get(), TFBlocks.ALPHA_YETI_WALL_TROPHY.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(TWILIGHT_RARITY).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> SNOW_QUEEN_TROPHY = register("snow_queen_trophy", properties -> new TrophyItem(TFBlocks.SNOW_QUEEN_TROPHY.get(), TFBlocks.SNOW_QUEEN_WALL_TROPHY.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(TWILIGHT_RARITY).equippable(EquipmentSlot.HEAD));
	public static final DeferredItem<Item> QUEST_RAM_TROPHY = register("quest_ram_trophy", properties -> new TrophyItem(TFBlocks.QUEST_RAM_TROPHY.get(), TFBlocks.QUEST_RAM_WALL_TROPHY.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().rarity(TWILIGHT_RARITY).equippable(EquipmentSlot.HEAD));

	public static final DeferredItem<HollowLogItem> HOLLOW_TWILIGHT_OAK_LOG = register("hollow_twilight_oak_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_TWILIGHT_OAK_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_CANOPY_LOG = register("hollow_canopy_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_CANOPY_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_CANOPY_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_CANOPY_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_MANGROVE_LOG = register("hollow_mangrove_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_MANGROVE_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_MANGROVE_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_MANGROVE_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_DARK_LOG = register("hollow_dark_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_DARK_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_DARK_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_DARK_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_TIME_LOG = register("hollow_time_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_TIME_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_TIME_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_TIME_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_TRANSFORMATION_LOG = register("hollow_transformation_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_TRANSFORMATION_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_TRANSFORMATION_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_TRANSFORMATION_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_MINING_LOG = register("hollow_mining_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_MINING_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_MINING_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_MINING_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_SORTING_LOG = register("hollow_sorting_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_SORTING_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_SORTING_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_SORTING_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());

	public static final DeferredItem<HollowLogItem> HOLLOW_OAK_LOG = register("hollow_oak_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_OAK_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_OAK_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_OAK_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_SPRUCE_LOG = register("hollow_spruce_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_SPRUCE_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_SPRUCE_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_SPRUCE_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_BIRCH_LOG = register("hollow_birch_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_BIRCH_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_BIRCH_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_BIRCH_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_JUNGLE_LOG = register("hollow_jungle_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_JUNGLE_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_JUNGLE_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_JUNGLE_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_ACACIA_LOG = register("hollow_acacia_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_ACACIA_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_ACACIA_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_ACACIA_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_DARK_OAK_LOG = register("hollow_dark_oak_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_DARK_OAK_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_DARK_OAK_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_DARK_OAK_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_CRIMSON_STEM = register("hollow_crimson_stem", properties -> new HollowLogItem(TFBlocks.HOLLOW_CRIMSON_STEM_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_CRIMSON_STEM_VERTICAL.asHolder(), TFBlocks.HOLLOW_CRIMSON_STEM_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_WARPED_STEM = register("hollow_warped_stem", properties -> new HollowLogItem(TFBlocks.HOLLOW_WARPED_STEM_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_WARPED_STEM_VERTICAL.asHolder(), TFBlocks.HOLLOW_WARPED_STEM_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_VANGROVE_LOG = register("hollow_vangrove_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_VANGROVE_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_VANGROVE_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_VANGROVE_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_CHERRY_LOG = register("hollow_cherry_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_CHERRY_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_CHERRY_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_CHERRY_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());
	public static final DeferredItem<HollowLogItem> HOLLOW_PALE_OAK_LOG = register("hollow_pale_oak_log", properties -> new HollowLogItem(TFBlocks.HOLLOW_PALE_OAK_LOG_HORIZONTAL.asHolder(), TFBlocks.HOLLOW_PALE_OAK_LOG_VERTICAL.asHolder(), TFBlocks.HOLLOW_PALE_OAK_LOG_CLIMBABLE.asHolder(), properties), () -> new Item.Properties().useBlockDescriptionPrefix());

	public static final DeferredItem<Item> TWILIGHT_OAK_SIGN = register("twilight_oak_sign", properties -> new SignItem(TFBlocks.TWILIGHT_OAK_SIGN.get(), TFBlocks.TWILIGHT_WALL_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> TWILIGHT_OAK_HANGING_SIGN = register("twilight_oak_hanging_sign", properties -> new HangingSignItem(TFBlocks.TWILIGHT_OAK_HANGING_SIGN.get(), TFBlocks.TWILIGHT_OAK_WALL_HANGING_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> CANOPY_SIGN = register("canopy_sign", properties -> new SignItem(TFBlocks.CANOPY_SIGN.get(), TFBlocks.CANOPY_WALL_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> CANOPY_HANGING_SIGN = register("canopy_hanging_sign", properties -> new HangingSignItem(TFBlocks.CANOPY_HANGING_SIGN.get(), TFBlocks.CANOPY_WALL_HANGING_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> MANGROVE_SIGN = register("mangrove_sign", properties -> new SignItem(TFBlocks.MANGROVE_SIGN.get(), TFBlocks.MANGROVE_WALL_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> MANGROVE_HANGING_SIGN = register("mangrove_hanging_sign", properties -> new HangingSignItem(TFBlocks.MANGROVE_HANGING_SIGN.get(), TFBlocks.MANGROVE_WALL_HANGING_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> DARK_SIGN = register("dark_sign", properties -> new SignItem(TFBlocks.DARK_SIGN.get(), TFBlocks.DARK_WALL_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> DARK_HANGING_SIGN = register("dark_hanging_sign", properties -> new HangingSignItem(TFBlocks.DARK_HANGING_SIGN.get(), TFBlocks.DARK_WALL_HANGING_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> TIME_SIGN = register("time_sign", properties -> new SignItem(TFBlocks.TIME_SIGN.get(), TFBlocks.TIME_WALL_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> TIME_HANGING_SIGN = register("time_hanging_sign", properties -> new HangingSignItem(TFBlocks.TIME_HANGING_SIGN.get(), TFBlocks.TIME_WALL_HANGING_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> TRANSFORMATION_SIGN = register("transformation_sign", properties -> new SignItem(TFBlocks.TRANSFORMATION_SIGN.get(), TFBlocks.TRANSFORMATION_WALL_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> TRANSFORMATION_HANGING_SIGN = register("transformation_hanging_sign", properties -> new HangingSignItem(TFBlocks.TRANSFORMATION_HANGING_SIGN.get(), TFBlocks.TRANSFORMATION_WALL_HANGING_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> MINING_SIGN = register("mining_sign", properties -> new SignItem(TFBlocks.MINING_SIGN.get(), TFBlocks.MINING_WALL_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> MINING_HANGING_SIGN = register("mining_hanging_sign", properties -> new HangingSignItem(TFBlocks.MINING_HANGING_SIGN.get(), TFBlocks.MINING_WALL_HANGING_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> SORTING_SIGN = register("sorting_sign", properties -> new SignItem(TFBlocks.SORTING_SIGN.get(), TFBlocks.SORTING_WALL_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));
	public static final DeferredItem<Item> SORTING_HANGING_SIGN = register("sorting_hanging_sign", properties -> new HangingSignItem(TFBlocks.SORTING_HANGING_SIGN.get(), TFBlocks.SORTING_WALL_HANGING_SIGN.get(), properties), () -> new Item.Properties().useBlockDescriptionPrefix().stacksTo(16));

	public static final DeferredItem<Item> TWILIGHT_OAK_BOAT = register("twilight_oak_boat", properties -> new BoatItem(TFEntities.TWILIGHT_OAK_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> TWILIGHT_OAK_CHEST_BOAT = register("twilight_oak_chest_boat", properties -> new BoatItem(TFEntities.TWILIGHT_OAK_CHEST_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> CANOPY_BOAT = register("canopy_boat", properties -> new BoatItem(TFEntities.CANOPY_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> CANOPY_CHEST_BOAT = register("canopy_chest_boat", properties -> new BoatItem(TFEntities.CANOPY_CHEST_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> MANGROVE_BOAT = register("mangrove_boat", properties -> new BoatItem(TFEntities.MANGROVE_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> MANGROVE_CHEST_BOAT = register("mangrove_chest_boat", properties -> new BoatItem(TFEntities.MANGROVE_CHEST_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> DARK_BOAT = register("dark_boat", properties -> new BoatItem(TFEntities.DARK_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> DARK_CHEST_BOAT = register("dark_chest_boat", properties -> new BoatItem(TFEntities.DARK_CHEST_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> TIME_BOAT = register("time_boat", properties -> new BoatItem(TFEntities.TIME_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> TIME_CHEST_BOAT = register("time_chest_boat", properties -> new BoatItem(TFEntities.TIME_CHEST_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> TRANSFORMATION_BOAT = register("transformation_boat", properties -> new BoatItem(TFEntities.TRANSFORMATION_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> TRANSFORMATION_CHEST_BOAT = register("transformation_chest_boat", properties -> new BoatItem(TFEntities.TRANSFORMATION_CHEST_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> MINING_BOAT = register("mining_boat", properties -> new BoatItem(TFEntities.MINING_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> MINING_CHEST_BOAT = register("mining_chest_boat", properties -> new BoatItem(TFEntities.MINING_CHEST_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> SORTING_BOAT = register("sorting_boat", properties -> new BoatItem(TFEntities.SORTING_BOAT, properties), () -> new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> SORTING_CHEST_BOAT = register("sorting_chest_boat", properties -> new BoatItem(TFEntities.SORTING_CHEST_BOAT, properties), () -> new Item.Properties().stacksTo(1));

	public static final Item MUSIC_DISC_RADIANCE = registerDirect("music_disc_radiance", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(TFJukeboxSongs.RADIANCE));
	public static final Item MUSIC_DISC_STEPS = registerDirect("music_disc_steps", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(TFJukeboxSongs.STEPS));
	public static final Item MUSIC_DISC_SUPERSTITIOUS = registerDirect("music_disc_superstitious", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(TFJukeboxSongs.SUPERSTITIOUS));
	public static final Item MUSIC_DISC_HOME = registerDirect("music_disc_home", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(TFJukeboxSongs.HOME));
	public static final Item MUSIC_DISC_WAYFARER = registerDirect("music_disc_wayfarer", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(TFJukeboxSongs.WAYFARER));
	public static final Item MUSIC_DISC_FINDINGS = registerDirect("music_disc_findings", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(TFJukeboxSongs.FINDINGS));
	public static final Item MUSIC_DISC_MAKER = registerDirect("music_disc_maker", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(TFJukeboxSongs.MAKER));
	public static final Item MUSIC_DISC_THREAD = registerDirect("music_disc_thread", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(TFJukeboxSongs.THREAD));
	public static final Item MUSIC_DISC_MOTION = registerDirect("music_disc_motion", Item::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(TFJukeboxSongs.MOTION));

	public static final Item NAGA_BANNER_PATTERN = registerDirect("naga_banner_pattern", properties -> new Item(properties.component(DataComponents.PROVIDES_BANNER_PATTERNS, bannerPatterns(TFBannerPatternTags.NAGA_BANNER_PATTERN))), () -> new Item.Properties().stacksTo(1).rarity(TWILIGHT_RARITY));
	public static final Item LICH_BANNER_PATTERN = registerDirect("lich_banner_pattern", properties -> new Item(properties.component(DataComponents.PROVIDES_BANNER_PATTERNS, bannerPatterns(TFBannerPatternTags.LICH_BANNER_PATTERN))), () -> new Item.Properties().stacksTo(1).rarity(TWILIGHT_RARITY));
	public static final Item MINOSHROOM_BANNER_PATTERN = registerDirect("minoshroom_banner_pattern", properties -> new Item(properties.component(DataComponents.PROVIDES_BANNER_PATTERNS, bannerPatterns(TFBannerPatternTags.MINOSHROOM_BANNER_PATTERN))), () -> new Item.Properties().stacksTo(1).rarity(TWILIGHT_RARITY));
	public static final Item HYDRA_BANNER_PATTERN = registerDirect("hydra_banner_pattern", properties -> new Item(properties.component(DataComponents.PROVIDES_BANNER_PATTERNS, bannerPatterns(TFBannerPatternTags.HYDRA_BANNER_PATTERN))), () -> new Item.Properties().stacksTo(1).rarity(TWILIGHT_RARITY));
	public static final Item KNIGHT_PHANTOM_BANNER_PATTERN = registerDirect("knight_phantom_banner_pattern", properties -> new Item(properties.component(DataComponents.PROVIDES_BANNER_PATTERNS, bannerPatterns(TFBannerPatternTags.KNIGHT_PHANTOM_BANNER_PATTERN))), () -> new Item.Properties().stacksTo(1).rarity(TWILIGHT_RARITY));
	public static final Item UR_GHAST_BANNER_PATTERN = registerDirect("ur_ghast_banner_pattern", properties -> new Item(properties.component(DataComponents.PROVIDES_BANNER_PATTERNS, bannerPatterns(TFBannerPatternTags.UR_GHAST_BANNER_PATTERN))), () -> new Item.Properties().stacksTo(1).rarity(TWILIGHT_RARITY));
	public static final Item ALPHA_YETI_BANNER_PATTERN = registerDirect("alpha_yeti_banner_pattern", properties -> new Item(properties.component(DataComponents.PROVIDES_BANNER_PATTERNS, bannerPatterns(TFBannerPatternTags.ALPHA_YETI_BANNER_PATTERN))), () -> new Item.Properties().stacksTo(1).rarity(TWILIGHT_RARITY));
	public static final Item SNOW_QUEEN_BANNER_PATTERN = registerDirect("snow_queen_banner_pattern", properties -> new Item(properties.component(DataComponents.PROVIDES_BANNER_PATTERNS, bannerPatterns(TFBannerPatternTags.SNOW_QUEEN_BANNER_PATTERN))), () -> new Item.Properties().stacksTo(1).rarity(TWILIGHT_RARITY));
	public static final Item QUEST_RAM_BANNER_PATTERN = registerDirect("quest_ram_banner_pattern", properties -> new Item(properties.component(DataComponents.PROVIDES_BANNER_PATTERNS, bannerPatterns(TFBannerPatternTags.QUESTING_RAM_BANNER_PATTERN))), () -> new Item.Properties().stacksTo(1).rarity(TWILIGHT_RARITY));

	@SuppressWarnings("unchecked")
	private static HolderSet<BannerPattern> bannerPatterns(net.minecraft.tags.TagKey<BannerPattern> tag) {
		Registry<BannerPattern> registry = (Registry<BannerPattern>) BuiltInRegistries.REGISTRY.getValue(Registries.BANNER_PATTERN.identifier());
		return HolderSet.emptyNamed(registry, tag);
	}

	public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> item, Supplier<Item.Properties> properties) {
		if (registered) throw new IllegalStateException("Cannot register new items after item registry has been frozen.");
		Identifier id = TwilightForestMod.prefix(name);
		DeferredItem<T> holder = DeferredItem.create(id, () -> item.apply(properties.get().setId(ResourceKey.create(Registries.ITEM, id))));
		ITEMS.put(id, new ItemEntry() {
			@Override
			public Item value() {
				return holder.get();
			}

			@Override
			public void register() {
				holder.register(BuiltInRegistries.ITEM);
			}
		});
		return holder;
	}

	private static <T extends Item> T registerDirect(String name, Function<Item.Properties, T> item, Supplier<Item.Properties> properties) {
		if (registered) throw new IllegalStateException("Cannot register new items after item registry has been frozen.");
		Identifier id = TwilightForestMod.prefix(name);
		T value = item.apply(properties.get().setId(ResourceKey.create(Registries.ITEM, id)));
		ITEMS.put(id, new ItemEntry() {
			@Override
			public Item value() {
				return value;
			}

			@Override
			public void register() {
				Registry.register(BuiltInRegistries.ITEM, id, value);
			}
		});
		return value;
	}

	public static void addAlias(Identifier from, Identifier to) {
		if (registered) throw new IllegalStateException("Cannot add aliases after items have been registered.");
		ITEM_ALIASES.put(from, to);
	}

	public static Collection<? extends Item> registeredItems() {
		return ITEMS.values().stream().map(ItemEntry::value).toList();
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		ITEMS.values().forEach(ItemEntry::register);
		applyItemAliases();
	}

	private static void applyItemAliases() {
		if (!ITEM_ALIASES.isEmpty()) {
			TwilightForestMod.LOGGER.warn("Skipping item registry aliases on Fabric to avoid registry sync duplicates.");
		}
	}

	private interface ItemEntry {
		Item value();

		void register();
	}
}
