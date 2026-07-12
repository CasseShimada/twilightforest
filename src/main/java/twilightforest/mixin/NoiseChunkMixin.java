package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.world.components.layer.BiomeDensityRuntimeContext;
import twilightforest.world.components.layer.BiomeDensityRuntimeContextHolder;

import java.util.List;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin {
	@Unique
	private BiomeDensityRuntimeContext twilightforest$biomeDensityRuntimeContext;

	@Inject(
		method = "<init>",
		at = @At(value = "INVOKE", target = "Ljava/lang/Object;<init>()V", shift = At.Shift.AFTER)
	)
	private void twilightforest$captureBiomeDensityRuntimeContext(int cellCountXZ, RandomState randomState, int firstBlockX, int firstBlockZ, NoiseSettings noiseSettings, DensityFunctions.BeardifierOrMarker beardifier, NoiseGeneratorSettings generatorSettings, Aquifer.FluidPicker fluidPicker, Blender blender, CallbackInfo ci) {
		this.twilightforest$biomeDensityRuntimeContext = BiomeDensityRuntimeContextHolder.get(randomState);
	}

	@ModifyExpressionValue(
		method = "<init>",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/RandomState;router()Lnet/minecraft/world/level/levelgen/NoiseRouter;")
	)
	private NoiseRouter twilightforest$createChunkBiomeDensityCaches(NoiseRouter router) {
		return BiomeDensityRuntimeContext.cacheForChunk(router);
	}

	@Inject(method = "cachedClimateSampler", at = @At("RETURN"))
	private void twilightforest$attachBiomeDensityRuntimeContextToSampler(NoiseRouter router, List<Climate.ParameterPoint> spawnTarget, CallbackInfoReturnable<Climate.Sampler> cir) {
		BiomeDensityRuntimeContextHolder.set(cir.getReturnValue(), this.twilightforest$biomeDensityRuntimeContext);
	}
}
