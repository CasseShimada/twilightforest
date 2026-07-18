package twilightforest.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.datagen.assets.AtlasGenerator;
import twilightforest.datagen.assets.EquipmentAssetsGenerator;
import twilightforest.datagen.assets.ParticleGenerator;
import twilightforest.datagen.data.DataMapGenerator;
import twilightforest.datagen.data.RegistrySnapshotGenerator;
import twilightforest.datagen.data.custom.QuestGenerator;
import twilightforest.datagen.data.custom.StalactiteGenerator;
import twilightforest.datagen.data.registries.BannerPatternGenerator;
import twilightforest.datagen.data.registries.BiomeLayerStackGenerator;
import twilightforest.datagen.data.registries.BiomeGenerator;
import twilightforest.datagen.data.registries.BiomeTerrainDataGenerator;
import twilightforest.datagen.data.registries.ChunkBlanketProcessorGenerator;
import twilightforest.datagen.data.registries.ConfiguredCarverGenerator;
import twilightforest.datagen.data.registries.ConfiguredFeatureGenerator;
import twilightforest.datagen.data.registries.DamageTypeGenerator;
import twilightforest.datagen.data.registries.DensityFunctionGenerator;
import twilightforest.datagen.data.registries.DimensionGenerator;
import twilightforest.datagen.data.registries.DimensionTypeGenerator;
import twilightforest.datagen.data.registries.DwarfRabbitVariantGenerator;
import twilightforest.datagen.data.registries.EnchantmentGenerator;
import twilightforest.datagen.data.registries.JukeboxSongGenerator;
import twilightforest.datagen.data.registries.MagicPaintingVariantGenerator;
import twilightforest.datagen.data.registries.NoiseSettingsGenerator;
import twilightforest.datagen.data.registries.PlacedFeatureGenerator;
import twilightforest.datagen.data.registries.RestrictionGenerator;
import twilightforest.datagen.data.registries.StructureGenerator;
import twilightforest.datagen.data.registries.StructureSetGenerator;
import twilightforest.datagen.data.registries.StructureSpeleothemConfigGenerator;
import twilightforest.datagen.data.registries.TinyBirdVariantGenerator;
import twilightforest.datagen.data.registries.TrimMaterialGenerator;
import twilightforest.datagen.data.registries.TravellersModifierGenerator;
import twilightforest.datagen.data.registries.WoodPaletteGenerator;
import twilightforest.datagen.data.tags.BannerPatternTagGenerator;
import twilightforest.datagen.data.tags.BiomeTagGenerator;
import twilightforest.datagen.data.tags.BlockEntityTypeTagGenerator;
import twilightforest.datagen.data.tags.BlockTagGenerator;
import twilightforest.datagen.data.tags.DamageTypeTagGenerator;
import twilightforest.datagen.data.tags.DimensionTypeTagGenerator;
import twilightforest.datagen.data.tags.EntityTypeTagGenerator;
import twilightforest.datagen.data.tags.ItemTagGenerator;
import twilightforest.datagen.data.tags.PaintingVariantTagGenerator;
import twilightforest.datagen.data.tags.StructureTagGenerator;
import twilightforest.datagen.data.tags.WoodPaletteTagGenerator;
import twilightforest.init.TFBannerPatterns;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFCaveCarvers;
import twilightforest.init.TFConfiguredFeatures;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFDensityFunctions;
import twilightforest.init.TFDimensionData;
import twilightforest.init.TFEnchantments;
import twilightforest.init.TFJukeboxSongs;
import twilightforest.init.TFPlacedFeatures;
import twilightforest.init.TFStructures;
import twilightforest.init.TFStructureSets;
import twilightforest.init.TFTrimMaterials;
import twilightforest.init.custom.BiomeLayerStack;
import twilightforest.init.custom.ChunkBlanketProcessors;
import twilightforest.init.custom.DwarfRabbitVariants;
import twilightforest.init.custom.MagicPaintingVariants;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.init.custom.Restrictions;
import twilightforest.init.custom.StructureSpeleothemConfigs;
import twilightforest.init.custom.TemplateMarkerHandlers;
import twilightforest.init.custom.TinyBirdVariants;
import twilightforest.init.custom.WoodPalettes;

public final class TwilightForestDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		FabricDataGenerator.Pack pack = dataGenerator.createPack();
		pack.addProvider(AtlasGenerator::new);
		pack.addProvider(EquipmentAssetsGenerator::new);
		pack.addProvider(ParticleGenerator::new);
		pack.addProvider(DataMapGenerator::new);
		pack.addProvider(RegistrySnapshotGenerator::new);
		pack.addProvider(QuestGenerator::new);
		pack.addProvider(StalactiteGenerator::new);
		pack.addProvider(BannerPatternGenerator::new);
		pack.addProvider(BiomeGenerator::new);
		pack.addProvider(BiomeLayerStackGenerator::new);
		pack.addProvider(BiomeTerrainDataGenerator::new);
		pack.addProvider(ChunkBlanketProcessorGenerator::new);
		pack.addProvider(ConfiguredCarverGenerator::new);
		pack.addProvider(ConfiguredFeatureGenerator::new);
		pack.addProvider(DamageTypeGenerator::new);
		pack.addProvider(DensityFunctionGenerator::new);
		pack.addProvider(DimensionGenerator::new);
		pack.addProvider(DimensionTypeGenerator::new);
		pack.addProvider(DwarfRabbitVariantGenerator::new);
		pack.addProvider(EnchantmentGenerator::new);
		pack.addProvider(JukeboxSongGenerator::new);
		pack.addProvider(MagicPaintingVariantGenerator::new);
		pack.addProvider(NoiseSettingsGenerator::new);
		pack.addProvider(PlacedFeatureGenerator::new);
		pack.addProvider(RestrictionGenerator::new);
		pack.addProvider(StructureGenerator::new);
		pack.addProvider(StructureSetGenerator::new);
		pack.addProvider(StructureSpeleothemConfigGenerator::new);
		pack.addProvider(TinyBirdVariantGenerator::new);
		pack.addProvider(TrimMaterialGenerator::new);
		pack.addProvider(TravellersModifierGenerator::new);
		pack.addProvider(WoodPaletteGenerator::new);
		BlockTagGenerator blockTags = pack.addProvider(BlockTagGenerator::new);
		pack.addProvider((output, registries) -> new ItemTagGenerator(output, registries, blockTags));
		pack.addProvider(BannerPatternTagGenerator::new);
		pack.addProvider(BiomeTagGenerator::new);
		pack.addProvider(EntityTypeTagGenerator::new);
		pack.addProvider(BlockEntityTypeTagGenerator::new);
		pack.addProvider(DamageTypeTagGenerator::new);
		pack.addProvider(DimensionTypeTagGenerator::new);
		pack.addProvider(PaintingVariantTagGenerator::new);
		pack.addProvider(StructureTagGenerator::new);
		pack.addProvider(WoodPaletteTagGenerator::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		registryBuilder.add(Registries.BANNER_PATTERN, TFBannerPatterns::bootstrap);
		registryBuilder.add(Registries.CONFIGURED_CARVER, TFCaveCarvers::bootstrap);
		registryBuilder.add(Registries.CONFIGURED_FEATURE, TFConfiguredFeatures::bootstrap);
		registryBuilder.add(Registries.DAMAGE_TYPE, TFDamageTypes::bootstrap);
		registryBuilder.add(Registries.DENSITY_FUNCTION, TFDensityFunctions::bootstrap);
		registryBuilder.add(Registries.LEVEL_STEM, TFDimensionData::bootstrapStem);
		registryBuilder.add(Registries.DIMENSION_TYPE, TFDimensionData::bootstrapType);
		registryBuilder.add(Registries.ENCHANTMENT, TFEnchantments::bootstrap);
		registryBuilder.add(Registries.PLACED_FEATURE, TFPlacedFeatures::bootstrap);
		registryBuilder.add(Registries.BIOME, TFBiomes::bootstrap);
		registryBuilder.add(Registries.JUKEBOX_SONG, TFJukeboxSongs::bootstrap);
		registryBuilder.add(Registries.NOISE_SETTINGS, TFDimensionData::bootstrapNoise);
		registryBuilder.add(Registries.STRUCTURE, TFStructures::bootstrap);
		registryBuilder.add(Registries.STRUCTURE_SET, TFStructureSets::bootstrap);
		registryBuilder.add(Registries.TRIM_MATERIAL, TFTrimMaterials::bootstrap);
		registryBuilder.add(TFRegistries.Keys.BIOME_STACK, BiomeLayerStack::bootstrap);
		registryBuilder.add(TFRegistries.Keys.BIOME_TERRAIN_DATA, BiomeLayerStack::bootstrapData);
		registryBuilder.add(TFRegistries.Keys.CHUNK_BLANKET_PROCESSORS, ChunkBlanketProcessors::bootstrap);
		registryBuilder.add(TFRegistries.Keys.DWARF_RABBIT_VARIANT, DwarfRabbitVariants::bootstrap);
		registryBuilder.add(TFRegistries.Keys.MAGIC_PAINTINGS, MagicPaintingVariants::bootstrap);
		registryBuilder.add(TFRegistries.Keys.TRAVELLERS_MODIFIERS, TravellersModifiersManager::bootstrap);
		registryBuilder.add(TFRegistries.Keys.RESTRICTIONS, Restrictions::bootstrap);
		registryBuilder.add(TFRegistries.Keys.STRUCTURE_SPELEOTHEM_SETTINGS, StructureSpeleothemConfigs::bootstrap);
		registryBuilder.add(TFRegistries.Keys.TEMPLATE_MARKER_HANDLER_LIST, TemplateMarkerHandlers::bootstrap);
		registryBuilder.add(TFRegistries.Keys.TINY_BIRD_VARIANT, TinyBirdVariants::bootstrap);
		registryBuilder.add(TFRegistries.Keys.WOOD_PALETTES, WoodPalettes::bootstrap);
	}

	@Override
	public String getEffectiveModId() {
		return TwilightForestMod.ID;
	}
}
