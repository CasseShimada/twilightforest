package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.BiomeSource;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.biomesources.TFBiomeProvider;

public class TFBiomeSources {
	public static final MapCodec<TFBiomeProvider> TWILIGHT_BIOMES = Registry.register(BuiltInRegistries.BIOME_SOURCE, TwilightForestMod.prefix("twilight_biomes"), TFBiomeProvider.TF_CODEC);

	public static void init() {
	}
}
