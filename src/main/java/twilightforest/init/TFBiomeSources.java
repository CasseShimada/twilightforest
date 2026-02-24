package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import twilightforest.TwilightForestMod;
import twilightforest.util.registry.DeferredHolder;
import twilightforest.util.registry.DeferredRegister;
import twilightforest.world.components.biomesources.TFBiomeProvider;

public class TFBiomeSources {
	public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
		DeferredRegister.create(Registries.BIOME_SOURCE, TwilightForestMod.ID);

	public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<TFBiomeProvider>> TWILIGHT_BIOMES =
		BIOME_SOURCES.register("twilight_biomes", () -> TFBiomeProvider.TF_CODEC);
}
