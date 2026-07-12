package twilightforest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.world.components.layer.BiomeDensityRuntimeContext;
import twilightforest.world.components.layer.BiomeDensityRuntimeContextHolder;

import java.util.Objects;

@Mixin(RandomState.class)
public class RandomStateMixin implements BiomeDensityRuntimeContextHolder {
	@Shadow
	@Final
	private Climate.Sampler sampler;

	@Unique
	@Nullable
	private BiomeDensityRuntimeContext twilightforest$biomeDensityRuntimeContext;

	@Inject(
		method = "<init>",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;getRandomSource()Lnet/minecraft/world/level/levelgen/WorldgenRandom$Algorithm;")
	)
	private void twilightforest$createBiomeDensityRuntimeContext(NoiseGeneratorSettings settings, HolderGetter<NormalNoise.NoiseParameters> noises, long seed, CallbackInfo ci) {
		this.twilightforest$setBiomeDensityRuntimeContext(new BiomeDensityRuntimeContext(seed));
	}

	@ModifyExpressionValue(
		method = "<init>",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;noiseRouter()Lnet/minecraft/world/level/levelgen/NoiseRouter;")
	)
	private NoiseRouter twilightforest$wireBiomeDensityRuntimes(NoiseRouter router) {
		return router.mapAll(this.twilightforest$getBiomeDensityRuntimeContext());
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void twilightforest$attachBiomeDensityRuntimeContextToSampler(NoiseGeneratorSettings settings, HolderGetter<NormalNoise.NoiseParameters> noises, long seed, CallbackInfo ci) {
		BiomeDensityRuntimeContextHolder.set(this.sampler, this.twilightforest$getBiomeDensityRuntimeContext());
	}

	@Override
	public BiomeDensityRuntimeContext twilightforest$getBiomeDensityRuntimeContext() {
		return Objects.requireNonNull(this.twilightforest$biomeDensityRuntimeContext, "RandomState biome density runtime context is not initialized");
	}

	@Override
	public void twilightforest$setBiomeDensityRuntimeContext(BiomeDensityRuntimeContext context) {
		BiomeDensityRuntimeContext current = this.twilightforest$biomeDensityRuntimeContext;
		if (current != null && current != context) {
			throw new IllegalStateException("RandomState biome density runtime context is already initialized");
		}
		this.twilightforest$biomeDensityRuntimeContext = context;
	}
}
