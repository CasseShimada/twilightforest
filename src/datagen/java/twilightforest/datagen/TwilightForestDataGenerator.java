package twilightforest.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.datagen.data.registries.BannerPatternGenerator;
import twilightforest.datagen.data.registries.ConfiguredCarverGenerator;
import twilightforest.datagen.data.registries.DamageTypeGenerator;
import twilightforest.datagen.data.registries.DwarfRabbitVariantGenerator;
import twilightforest.datagen.data.registries.StructureSpeleothemConfigGenerator;
import twilightforest.datagen.data.registries.TinyBirdVariantGenerator;
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
import twilightforest.init.TFDimensionData;
import twilightforest.init.TFPlacedFeatures;
import twilightforest.init.TFStructures;
import twilightforest.init.custom.DwarfRabbitVariants;
import twilightforest.init.custom.StructureSpeleothemConfigs;
import twilightforest.init.custom.TinyBirdVariants;
import twilightforest.init.custom.WoodPalettes;

public final class TwilightForestDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		FabricDataGenerator.Pack pack = dataGenerator.createPack();
		pack.addProvider(BannerPatternGenerator::new);
		pack.addProvider(ConfiguredCarverGenerator::new);
		pack.addProvider(DamageTypeGenerator::new);
		pack.addProvider(DwarfRabbitVariantGenerator::new);
		pack.addProvider(StructureSpeleothemConfigGenerator::new);
		pack.addProvider(TinyBirdVariantGenerator::new);
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
		registryBuilder.add(Registries.DIMENSION_TYPE, TFDimensionData::bootstrapType);
		registryBuilder.add(Registries.PLACED_FEATURE, TFPlacedFeatures::bootstrap);
		registryBuilder.add(Registries.BIOME, TFBiomes::bootstrap);
		registryBuilder.add(Registries.STRUCTURE, TFStructures::bootstrap);
		registryBuilder.add(TFRegistries.Keys.DWARF_RABBIT_VARIANT, DwarfRabbitVariants::bootstrap);
		registryBuilder.add(TFRegistries.Keys.STRUCTURE_SPELEOTHEM_SETTINGS, StructureSpeleothemConfigs::bootstrap);
		registryBuilder.add(TFRegistries.Keys.TINY_BIRD_VARIANT, TinyBirdVariants::bootstrap);
		registryBuilder.add(TFRegistries.Keys.WOOD_PALETTES, WoodPalettes::bootstrap);
	}

	@Override
	public String getEffectiveModId() {
		return TwilightForestMod.ID;
	}
}
