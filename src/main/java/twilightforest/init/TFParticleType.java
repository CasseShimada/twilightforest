package twilightforest.init;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import twilightforest.TwilightForestMod;
import twilightforest.particle.data.LeafParticleData;

public class TFParticleType {
	public static final SimpleParticleType LARGE_FLAME = register("large_flame", FabricParticleTypes.simple(false));
	public static final SimpleParticleType LEAF_RUNE = register("leaf_rune", FabricParticleTypes.simple(false));
	public static final SimpleParticleType BOSS_TEAR = register("boss_tear", FabricParticleTypes.simple(false));
	public static final SimpleParticleType GHAST_TRAP = register("ghast_trap", FabricParticleTypes.simple(false));
	public static final SimpleParticleType PROTECTION = register("protection", FabricParticleTypes.simple(true));
	public static final SimpleParticleType SNOW = register("snow", FabricParticleTypes.simple(false));
	public static final SimpleParticleType SNOW_WARNING = register("snow_warning", FabricParticleTypes.simple(false));
	public static final SimpleParticleType EXTENDED_SNOW_WARNING = register("extended_snow_warning", FabricParticleTypes.simple(false));
	public static final SimpleParticleType SNOW_GUARDIAN = register("snow_guardian", FabricParticleTypes.simple(false));
	public static final SimpleParticleType ICE_BEAM = register("ice_beam", FabricParticleTypes.simple(false));
	public static final SimpleParticleType ANNIHILATE = register("annihilate", FabricParticleTypes.simple(false));
	public static final SimpleParticleType HUGE_SMOKE = register("huge_smoke", FabricParticleTypes.simple(false));
	public static final SimpleParticleType FIREFLY = register("firefly", FabricParticleTypes.simple(false));
	public static final SimpleParticleType WANDERING_FIREFLY = register("wandering_firefly", FabricParticleTypes.simple(false));
	public static final SimpleParticleType PARTICLE_SPAWNER_FIREFLY = register("particle_spawner_firefly", FabricParticleTypes.simple(false));
	public static final ParticleType<LeafParticleData> FALLEN_LEAF = register("fallen_leaf", new ParticleType<>(false) {
		@Override
		public MapCodec<LeafParticleData> codec() {
			return LeafParticleData.CODEC;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, LeafParticleData> streamCodec() {
			return LeafParticleData.STREAM_CODEC;
		}
	});
	public static final SimpleParticleType DIM_FLAME = register("dim_flame", FabricParticleTypes.simple(false));
	public static final SimpleParticleType OMINOUS_FLAME = register("ominous_flame", FabricParticleTypes.simple(false));
	public static final SimpleParticleType SORTING_PARTICLE = register("sorting_particle", FabricParticleTypes.simple(false));
	public static final SimpleParticleType TRANSFORMATION_PARTICLE = register("transformation_particle", FabricParticleTypes.simple(false));
	public static final SimpleParticleType LOG_CORE_PARTICLE = register("log_core_particle", FabricParticleTypes.simple(false));
	public static final SimpleParticleType CLOUD_PUFF = register("cloud_puff", FabricParticleTypes.simple(false));
	public static final ParticleType<ColorParticleOption> MAGIC_EFFECT = register("magic_effect", new ParticleType<>(false) {
		@Override
		public MapCodec<ColorParticleOption> codec() {
			return ColorParticleOption.codec(this);
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
			return ColorParticleOption.streamCodec(this);
		}
	});
	public static final SimpleParticleType ANGRY_LICH = register("angry_lich", FabricParticleTypes.simple(false));
	public static final SimpleParticleType TWILIGHT_ORB = register("twilight_orb", FabricParticleTypes.simple(false));
	public static final SimpleParticleType SHIELD_BREAK = register("shield_break", FabricParticleTypes.simple(false));
	public static final SimpleParticleType DRYING_RACK = register("drying_rack", FabricParticleTypes.simple(false));

	private static <T extends ParticleType<?>> T register(String name, T type) {
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix(name), type);
	}

	public static void init() {
	}
}
