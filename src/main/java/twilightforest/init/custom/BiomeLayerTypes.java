package twilightforest.init.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.layer.*;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerType;
import twilightforest.world.components.layer.vanillalegacy.SmoothLayer;
import twilightforest.world.components.layer.vanillalegacy.ZoomLayer;

import java.util.LinkedHashMap;
import java.util.Map;

public class BiomeLayerTypes {
	private static final Map<Identifier, BiomeLayerType> BIOME_LAYER_TYPES = new LinkedHashMap<>();
	private static boolean registered;

	public static final Codec<BiomeLayerType> CODEC = Codec.lazyInitialized(TFRegistries.BIOME_LAYER_TYPE::byNameCodec);

	public static final BiomeLayerType RANDOM_BIOMES = registerType("random_biomes", () -> RandomBiomeLayer.Factory.CODEC);
	public static final BiomeLayerType KEY_BIOMES = registerType("key_biomes", () -> KeyBiomesLayer.Factory.CODEC);
	public static final BiomeLayerType COMPANION_BIOMES = registerType("companion_biomes", () -> CompanionBiomesLayer.Factory.CODEC);
	public static final BiomeLayerType ZOOM = registerType("zoom", () -> ZoomLayer.Factory.CODEC);
	public static final BiomeLayerType STABILIZE = registerType("stabilize", () -> StabilizeLayer.Factory.CODEC);
	public static final BiomeLayerType BORDER = registerType("border", () -> BorderLayer.Factory.CODEC);
	public static final BiomeLayerType SEAM = registerType("seam", () -> SeamLayer.Factory.CODEC);
	public static final BiomeLayerType SMOOTH = registerType("smooth", () -> SmoothLayer.Factory.CODEC);
	public static final BiomeLayerType FILTERED = registerType("filtered", () -> FilteredBiomeLayer.Factory.CODEC);
	public static final BiomeLayerType MEDIAN = registerType("median", () -> MedianLayer.Factory.CODEC);

	private static BiomeLayerType registerType(String name, BiomeLayerType type) {
		BIOME_LAYER_TYPES.put(TwilightForestMod.prefix(name), type);
		return type;
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		BIOME_LAYER_TYPES.forEach((id, type) -> Registry.register(TFRegistries.BIOME_LAYER_TYPE, id, type));
	}
}
