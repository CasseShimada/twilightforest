package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import twilightforest.util.registry.DeferredHolder;
import twilightforest.util.registry.DeferredRegister;
import twilightforest.TwilightForestMod;
import twilightforest.particle.data.LeafParticleData;

public class TFParticleType {

	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, TwilightForestMod.ID);

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LARGE_FLAME = PARTICLE_TYPES.register("large_flame", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LEAF_RUNE = PARTICLE_TYPES.register("leaf_rune", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BOSS_TEAR = PARTICLE_TYPES.register("boss_tear", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GHAST_TRAP = PARTICLE_TYPES.register("ghast_trap", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PROTECTION = PARTICLE_TYPES.register("protection", () -> FabricParticleTypes.simple(true));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SNOW = PARTICLE_TYPES.register("snow", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SNOW_WARNING = PARTICLE_TYPES.register("snow_warning", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EXTENDED_SNOW_WARNING = PARTICLE_TYPES.register("extended_snow_warning", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SNOW_GUARDIAN = PARTICLE_TYPES.register("snow_guardian", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ICE_BEAM = PARTICLE_TYPES.register("ice_beam", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ANNIHILATE = PARTICLE_TYPES.register("annihilate", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HUGE_SMOKE = PARTICLE_TYPES.register("huge_smoke", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIREFLY = PARTICLE_TYPES.register("firefly", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WANDERING_FIREFLY = PARTICLE_TYPES.register("wandering_firefly", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PARTICLE_SPAWNER_FIREFLY = PARTICLE_TYPES.register("particle_spawner_firefly", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, ParticleType<LeafParticleData>> FALLEN_LEAF = PARTICLE_TYPES.register("fallen_leaf", () -> new ParticleType<>(false) {
		@Override
		public MapCodec<LeafParticleData> codec() {
			return LeafParticleData.CODEC;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, LeafParticleData> streamCodec() {
			return LeafParticleData.STREAM_CODEC;
		}
	});
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DIM_FLAME = PARTICLE_TYPES.register("dim_flame", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> OMINOUS_FLAME = PARTICLE_TYPES.register("ominous_flame", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SORTING_PARTICLE = PARTICLE_TYPES.register("sorting_particle", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TRANSFORMATION_PARTICLE = PARTICLE_TYPES.register("transformation_particle", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LOG_CORE_PARTICLE = PARTICLE_TYPES.register("log_core_particle", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CLOUD_PUFF = PARTICLE_TYPES.register("cloud_puff", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> MAGIC_EFFECT = PARTICLE_TYPES.register("magic_effect", () -> new ParticleType<>(false) {
		@Override
		public MapCodec<ColorParticleOption> codec() {
			return ColorParticleOption.codec(MAGIC_EFFECT.get());
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
			return ColorParticleOption.streamCodec(MAGIC_EFFECT.get());
		}
	});
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ANGRY_LICH = PARTICLE_TYPES.register("angry_lich", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TWILIGHT_ORB = PARTICLE_TYPES.register("twilight_orb", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHIELD_BREAK = PARTICLE_TYPES.register("shield_break", () -> FabricParticleTypes.simple(false));
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DRYING_RACK = PARTICLE_TYPES.register("drying_rack", () -> FabricParticleTypes.simple(false));
}
