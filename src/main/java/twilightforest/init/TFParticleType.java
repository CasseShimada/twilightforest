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
	private static boolean registered;

	public static final SimpleParticleType LARGE_FLAME = FabricParticleTypes.simple(false);
	public static final SimpleParticleType LEAF_RUNE = FabricParticleTypes.simple(false);
	public static final SimpleParticleType BOSS_TEAR = FabricParticleTypes.simple(false);
	public static final SimpleParticleType GHAST_TRAP = FabricParticleTypes.simple(false);
	public static final SimpleParticleType PROTECTION = FabricParticleTypes.simple(true);
	public static final SimpleParticleType SNOW = FabricParticleTypes.simple(false);
	public static final SimpleParticleType SNOW_WARNING = FabricParticleTypes.simple(false);
	public static final SimpleParticleType EXTENDED_SNOW_WARNING = FabricParticleTypes.simple(false);
	public static final SimpleParticleType SNOW_GUARDIAN = FabricParticleTypes.simple(false);
	public static final SimpleParticleType ICE_BEAM = FabricParticleTypes.simple(false);
	public static final SimpleParticleType ANNIHILATE = FabricParticleTypes.simple(false);
	public static final SimpleParticleType HUGE_SMOKE = FabricParticleTypes.simple(false);
	public static final SimpleParticleType FIREFLY = FabricParticleTypes.simple(false);
	public static final SimpleParticleType WANDERING_FIREFLY = FabricParticleTypes.simple(false);
	public static final SimpleParticleType PARTICLE_SPAWNER_FIREFLY = FabricParticleTypes.simple(false);
	public static final ParticleType<LeafParticleData> FALLEN_LEAF = new ParticleType<>(false) {
		@Override
		public MapCodec<LeafParticleData> codec() {
			return LeafParticleData.CODEC;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, LeafParticleData> streamCodec() {
			return LeafParticleData.STREAM_CODEC;
		}
	};
	public static final SimpleParticleType DIM_FLAME = FabricParticleTypes.simple(false);
	public static final SimpleParticleType OMINOUS_FLAME = FabricParticleTypes.simple(false);
	public static final SimpleParticleType SORTING_PARTICLE = FabricParticleTypes.simple(false);
	public static final SimpleParticleType TRANSFORMATION_PARTICLE = FabricParticleTypes.simple(false);
	public static final SimpleParticleType LOG_CORE_PARTICLE = FabricParticleTypes.simple(false);
	public static final SimpleParticleType CLOUD_PUFF = FabricParticleTypes.simple(false);
	public static final ParticleType<ColorParticleOption> MAGIC_EFFECT = new ParticleType<>(false) {
		@Override
		public MapCodec<ColorParticleOption> codec() {
			return ColorParticleOption.codec(this);
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
			return ColorParticleOption.streamCodec(this);
		}
	};
	public static final SimpleParticleType ANGRY_LICH = FabricParticleTypes.simple(false);
	public static final SimpleParticleType TWILIGHT_ORB = FabricParticleTypes.simple(false);
	public static final SimpleParticleType SHIELD_BREAK = FabricParticleTypes.simple(false);
	public static final SimpleParticleType DRYING_RACK = FabricParticleTypes.simple(false);

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("large_flame"), LARGE_FLAME);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("leaf_rune"), LEAF_RUNE);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("boss_tear"), BOSS_TEAR);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("ghast_trap"), GHAST_TRAP);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("protection"), PROTECTION);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("snow"), SNOW);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("snow_warning"), SNOW_WARNING);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("extended_snow_warning"), EXTENDED_SNOW_WARNING);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("snow_guardian"), SNOW_GUARDIAN);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("ice_beam"), ICE_BEAM);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("annihilate"), ANNIHILATE);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("huge_smoke"), HUGE_SMOKE);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("firefly"), FIREFLY);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("wandering_firefly"), WANDERING_FIREFLY);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("particle_spawner_firefly"), PARTICLE_SPAWNER_FIREFLY);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("fallen_leaf"), FALLEN_LEAF);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("dim_flame"), DIM_FLAME);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("ominous_flame"), OMINOUS_FLAME);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("sorting_particle"), SORTING_PARTICLE);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("transformation_particle"), TRANSFORMATION_PARTICLE);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("log_core_particle"), LOG_CORE_PARTICLE);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("cloud_puff"), CLOUD_PUFF);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("magic_effect"), MAGIC_EFFECT);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("angry_lich"), ANGRY_LICH);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("twilight_orb"), TWILIGHT_ORB);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("shield_break"), SHIELD_BREAK);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, TwilightForestMod.prefix("drying_rack"), DRYING_RACK);
	}
}
