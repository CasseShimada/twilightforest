package twilightforest.world.components.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record RandomPatchConfiguration(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature) implements FeatureConfiguration {
	public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.intRange(0, 256).fieldOf("tries").forGetter(RandomPatchConfiguration::tries),
		Codec.intRange(0, 128).fieldOf("xz_spread").forGetter(RandomPatchConfiguration::xzSpread),
		Codec.intRange(0, 128).fieldOf("y_spread").forGetter(RandomPatchConfiguration::ySpread),
		PlacedFeature.CODEC.fieldOf("feature").forGetter(RandomPatchConfiguration::feature)
	).apply(instance, RandomPatchConfiguration::new));
}
