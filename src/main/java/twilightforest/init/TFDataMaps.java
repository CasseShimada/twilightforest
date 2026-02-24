package twilightforest.init;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import twilightforest.network.PacketDistributor;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public final class TFDataMaps {
	private static final Map<Identifier, EntityTransformation> TRANSFORMATION_POWDER = new HashMap<>();
	private static final Map<Identifier, EntityTransformation> OMINOUS_FIRE = new HashMap<>();
	private static final Map<Identifier, CrumbledBlock> CRUMBLE_HORN = new HashMap<>();
	private static final Map<Identifier, MagicMapBiomeColor> MAGIC_MAP_BIOME_COLOR = new HashMap<>();
	private static final Map<Identifier, OreMapOreColor> ORE_MAP_ORE_COLOR = new HashMap<>();

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

	@Nullable
	public static MagicMapBiomeColor getMagicMapColor(Biome biome) {
		var server = PacketDistributor.getServer();
		if (server == null) {
			return null;
		}
		Identifier key = server.registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome);
		return key == null ? null : MAGIC_MAP_BIOME_COLOR.get(key);
	}

	private static void clear() {
		TRANSFORMATION_POWDER.clear();
		OMINOUS_FIRE.clear();
		CRUMBLE_HORN.clear();
		MAGIC_MAP_BIOME_COLOR.clear();
		ORE_MAP_ORE_COLOR.clear();
	}

	private static <T> void loadMap(ResourceManager manager, String path, Map<Identifier, T> target, Codec<T> codec) {
		Identifier id = Identifier.fromNamespaceAndPath(TwilightForestMod.ID, path);
		Resource resource = manager.getResource(id).orElse(null);
		if (resource == null) {
			TwilightForestMod.LOGGER.warn("Missing data map resource {}", id);
			return;
		}

		try (Reader reader = resource.openAsReader()) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			JsonObject values = root.getAsJsonObject("values");
			if (values == null) {
				TwilightForestMod.LOGGER.warn("Data map {} has no values object", id);
				return;
			}

			for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
				Identifier key = Identifier.tryParse(entry.getKey());
				if (key == null) {
					TwilightForestMod.LOGGER.warn("Invalid data map key {} in {}", entry.getKey(), id);
					continue;
				}

				DataResult<T> result = codec.parse(JsonOps.INSTANCE, entry.getValue());
				result.resultOrPartial(error -> TwilightForestMod.LOGGER.warn("Failed to parse data map {} entry {}: {}", id, key, error))
					.ifPresent(value -> target.put(key, value));
			}
		} catch (Exception e) {
			TwilightForestMod.LOGGER.error("Failed reading data map {}", id, e);
		}
	}

	private static final class ReloadListener implements SimpleSynchronousResourceReloadListener {
		@Override
		public Identifier getFabricId() {
			return TwilightForestMod.prefix("data_maps");
		}

		@Override
		public void onResourceManagerReload(ResourceManager manager) {
			clear();
			loadMap(manager, "data_maps/block/crumble_horn.json", CRUMBLE_HORN, CrumbledBlock.CODEC);
			loadMap(manager, "data_maps/block/ore_map_color.json", ORE_MAP_ORE_COLOR, OreMapOreColor.CODEC);
			loadMap(manager, "data_maps/entity_type/transformation_powder.json", TRANSFORMATION_POWDER, EntityTransformation.CODEC);
			loadMap(manager, "data_maps/entity_type/ominous_fire.json", OMINOUS_FIRE, EntityTransformation.CODEC);
			loadMap(manager, "data_maps/worldgen/biome/magic_map_color.json", MAGIC_MAP_BIOME_COLOR, MagicMapBiomeColor.CODEC);
		}
	}
}
