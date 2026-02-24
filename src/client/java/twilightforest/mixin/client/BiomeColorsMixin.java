package twilightforest.mixin.client;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.world.components.BiomeColorAlgorithms;
import twilightforest.world.components.TFGrassColorModifier;
import twilightforest.world.components.TFGrassColorModifierHolder;

@Mixin(BiomeColors.class)
public class BiomeColorsMixin {
	@Shadow
	@Final
	@Mutable
	private static ColorResolver GRASS_COLOR_RESOLVER;

	private static final BiomeColorAlgorithms BIOME_COLOR_ALGORITHMS = new BiomeColorAlgorithms();

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void twilightforest$wrapGrassResolver(CallbackInfo ci) {
		ColorResolver original = GRASS_COLOR_RESOLVER;
		GRASS_COLOR_RESOLVER = (Biome biome, double x, double z) -> {
			if ((Object) biome instanceof TFGrassColorModifierHolder holder) {
				TFGrassColorModifier modifier = holder.twilightforest$getGrassColorModifier();
				switch (modifier) {
					case ENCHANTED_FOREST -> {
						int base = original.getColor(biome, x, z);
						return BIOME_COLOR_ALGORITHMS.enchanted(base, (int) x, (int) z);
					}
					case SWAMP -> {
						return BIOME_COLOR_ALGORITHMS.swamp(BiomeColorAlgorithms.Type.Grass);
					}
					case DARK_FOREST -> {
						return BIOME_COLOR_ALGORITHMS.darkForest(BiomeColorAlgorithms.Type.Grass);
					}
					case DARK_FOREST_CENTER -> {
						return BIOME_COLOR_ALGORITHMS.darkForestCenterGrass(x, z);
					}
					case SPOOKY_FOREST -> {
						return BIOME_COLOR_ALGORITHMS.spookyGrass(x, z);
					}
					default -> {
					}
				}
			}
			return original.getColor(biome, x, z);
		};
	}
}
