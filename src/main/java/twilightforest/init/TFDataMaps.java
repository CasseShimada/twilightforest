package twilightforest.init;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.util.datamaps.CrumbledBlock;
import twilightforest.util.datamaps.EntityTransformation;
import twilightforest.util.datamaps.MagicMapBiomeColor;
import twilightforest.util.datamaps.OreMapOreColor;
import twilightforest.item.MagicMapItem;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TFDataMaps {
	private static final ReloadedDataMap<EntityTransformation> TRANSFORMATION_POWDER = dataMap("entity_type/transformation_powder", EntityTransformation.CODEC);
	private static final ReloadedDataMap<EntityTransformation> OMINOUS_FIRE = dataMap("entity_type/ominous_fire", EntityTransformation.CODEC);
	private static final ReloadedDataMap<CrumbledBlock> CRUMBLE_HORN = dataMap("block/crumble_horn", CrumbledBlock.CODEC);
	private static final ReloadedDataMap<MagicMapBiomeColor> MAGIC_MAP_BIOME_COLOR = dataMap("worldgen/biome/magic_map_color", MagicMapBiomeColor.CODEC);
	private static final ReloadedDataMap<OreMapOreColor> ORE_MAP_ORE_COLOR = dataMap("block/ore_map_color", OreMapOreColor.CODEC);
	private static final List<ReloadedDataMap<?>> DATA_MAPS = List.of(
		CRUMBLE_HORN,
		ORE_MAP_ORE_COLOR,
		TRANSFORMATION_POWDER,
		OMINOUS_FIRE,
		MAGIC_MAP_BIOME_COLOR
	);

	private TFDataMaps() {
	}

	public static void registerReloadListener() {
		net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(PackType.SERVER_DATA)
			.registerReloadListener(new ReloadListener());
	}

	@Nullable
	public static EntityTransformation getTransformation(EntityType<?> type) {
		return TRANSFORMATION_POWDER.get(BuiltInRegistries.ENTITY_TYPE.getKey(type));
	}

	@Nullable
	public static EntityTransformation getOminousFire(EntityType<?> type) {
		return OMINOUS_FIRE.get(BuiltInRegistries.ENTITY_TYPE.getKey(type));
	}

	@Nullable
	public static CrumbledBlock getCrumble(Block block) {
		return CRUMBLE_HORN.get(BuiltInRegistries.BLOCK.getKey(block));
	}

	@Nullable
	public static OreMapOreColor getOreMapColor(Block block) {
		return ORE_MAP_ORE_COLOR.get(BuiltInRegistries.BLOCK.getKey(block));
	}

	@Nullable
	public static MagicMapBiomeColor getMagicMapColor(Holder<Biome> biome) {
		return biome.unwrapKey().map(key -> MAGIC_MAP_BIOME_COLOR.get(key.identifier())).orElse(null);
	}

	private static <T> ReloadedDataMap<T> dataMap(String path, Codec<T> codec) {
		return new ReloadedDataMap<>(TwilightForestMod.prefix("data_maps/" + path + ".json"), codec);
	}

	private static void clear() {
		DATA_MAPS.forEach(ReloadedDataMap::clear);
	}

	private static final class ReloadListener implements SimpleSynchronousResourceReloadListener {
		@Override
		public Identifier getFabricId() {
			return TwilightForestMod.prefix("data_maps");
		}

		@Override
		public void onResourceManagerReload(ResourceManager manager) {
			MagicMapItem.clearBiomeCache();
			clear();
			DATA_MAPS.forEach(dataMap -> dataMap.load(manager));
		}
	}

	static final class ReloadedDataMap<T> {
		private final Identifier id;
		private final Codec<DataMapFile<T>> codec;
		private final Map<Identifier, T> values = new HashMap<>();

		ReloadedDataMap(Identifier id, Codec<T> codec) {
			this.id = id;
			this.codec = DataMapFile.codec(codec);
		}

		@Nullable
		T get(Identifier key) {
			return this.values.get(key);
		}

		private void clear() {
			this.values.clear();
		}

		void load(ResourceManager manager) {
			List<Resource> resources = manager.getResourceStack(this.id);
			if (resources.isEmpty()) {
				TwilightForestMod.LOGGER.warn("Missing data map resource {}", this.id);
				return;
			}

			resources.forEach(this::load);
		}

		private void load(Resource resource) {
			try (Reader reader = resource.openAsReader()) {
				DataResult<DataMapFile<T>> result = this.codec.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader));
				result.resultOrPartial(error -> TwilightForestMod.LOGGER.warn("Failed to parse data map {} from pack {}: {}", this.id, resource.sourcePackId(), error))
					.ifPresent(file -> {
						if (file.replace()) {
							this.clear();
						}
						this.values.putAll(file.values());
					});
			} catch (Exception e) {
				TwilightForestMod.LOGGER.error("Failed reading data map {} from pack {}", this.id, resource.sourcePackId(), e);
			}
		}
	}

	private record DataMapFile<T>(boolean replace, Map<Identifier, T> values) {
		private static <T> Codec<DataMapFile<T>> codec(Codec<T> valueCodec) {
			return RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("replace", false).forGetter(DataMapFile::replace),
				Codec.unboundedMap(Identifier.CODEC, valueCodec).fieldOf("values").forGetter(DataMapFile::values)
			).apply(instance, DataMapFile::new));
		}
	}
}
