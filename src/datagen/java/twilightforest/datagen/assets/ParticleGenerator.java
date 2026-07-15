package twilightforest.datagen.assets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFParticleType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ParticleGenerator implements DataProvider {
	private final PackOutput.PathProvider pathProvider;

	public ParticleGenerator(FabricPackOutput output) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "particles");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		Map<Identifier, List<Identifier>> descriptions = new LinkedHashMap<>();
		addSprite(descriptions, TFParticleType.ANNIHILATE, TwilightForestMod.prefix("annihilate_particle"));
		addSpriteSet(descriptions, TFParticleType.CLOUD_PUFF, Identifier.withDefaultNamespace("generic"), 8, true);
		addSprite(descriptions, TFParticleType.DIM_FLAME, TwilightForestMod.prefix("dim_flame"));
		addSpriteSet(descriptions, TFParticleType.DRYING_RACK, Identifier.withDefaultNamespace("generic"), 8, true);
		addSpriteSet(descriptions, TFParticleType.EXTENDED_SNOW_WARNING, TwilightForestMod.prefix("snow"), 4, false);
		addSprite(descriptions, TFParticleType.FALLEN_LEAF, TwilightForestMod.prefix("fallen_leaf"));
		addSprite(descriptions, TFParticleType.FIREFLY, TwilightForestMod.prefix("firefly"));
		addSpriteSet(descriptions, TFParticleType.GHAST_TRAP, Identifier.withDefaultNamespace("generic"), 8, true);
		addSpriteSet(descriptions, TFParticleType.HUGE_SMOKE, Identifier.withDefaultNamespace("generic"), 8, true);
		addSpriteSet(descriptions, TFParticleType.ICE_BEAM, TwilightForestMod.prefix("snow"), 4, false);
		addSprite(descriptions, TFParticleType.LARGE_FLAME, Identifier.withDefaultNamespace("flame"));
		addLeafRunes(descriptions);
		addSprite(descriptions, TFParticleType.LOG_CORE_PARTICLE, TwilightForestMod.prefix("log_core"));
		addSprite(descriptions, TFParticleType.OMINOUS_FLAME, TwilightForestMod.prefix("ominous_flame"));
		addSprite(descriptions, TFParticleType.PARTICLE_SPAWNER_FIREFLY, TwilightForestMod.prefix("firefly"));
		addSprite(descriptions, TFParticleType.PROTECTION, Identifier.withDefaultNamespace("glint"));
		addSpriteSet(descriptions, TFParticleType.SNOW, TwilightForestMod.prefix("snow"), 4, false);
		addSpriteSet(descriptions, TFParticleType.SNOW_GUARDIAN, TwilightForestMod.prefix("snow"), 4, false);
		addSpriteSet(descriptions, TFParticleType.SNOW_WARNING, TwilightForestMod.prefix("snow"), 4, false);
		addSprite(descriptions, TFParticleType.SORTING_PARTICLE, TwilightForestMod.prefix("log_core"));
		addSprite(descriptions, TFParticleType.TRANSFORMATION_PARTICLE, TwilightForestMod.prefix("log_core"));
		addSprite(descriptions, TFParticleType.WANDERING_FIREFLY, TwilightForestMod.prefix("firefly"));
		addSpriteSet(descriptions, TFParticleType.MAGIC_EFFECT, Identifier.withDefaultNamespace("effect"), 8, true);
		addSprite(descriptions, TFParticleType.ANGRY_LICH, Identifier.withDefaultNamespace("angry"));
		addSprite(descriptions, TFParticleType.TWILIGHT_ORB, TwilightForestMod.prefix("twilight_orb"));
		addSprite(descriptions, TFParticleType.SHIELD_BREAK, TwilightForestMod.prefix("shield_break"));
		return DataProvider.saveAll(output, ParticleGenerator::toJson, this.pathProvider::json, descriptions);
	}

	@Override
	public String getName() {
		return "Twilight Forest Particle Descriptions";
	}

	private static void addSprite(Map<Identifier, List<Identifier>> descriptions, ParticleType<?> type, Identifier texture) {
		add(descriptions, type, List.of(texture));
	}

	private static void addSpriteSet(Map<Identifier, List<Identifier>> descriptions, ParticleType<?> type, Identifier texture, int count, boolean reverse) {
		List<Identifier> textures = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			int frame = reverse ? count - i - 1 : i;
			textures.add(texture.withSuffix("_" + frame));
		}
		add(descriptions, type, textures);
	}

	private static void addLeafRunes(Map<Identifier, List<Identifier>> descriptions) {
		List<Identifier> textures = new ArrayList<>(26);
		for (char rune = 'a'; rune <= 'z'; rune++) {
			textures.add(Identifier.withDefaultNamespace("sga_" + rune));
		}
		add(descriptions, TFParticleType.LEAF_RUNE, textures);
	}

	private static void add(Map<Identifier, List<Identifier>> descriptions, ParticleType<?> type, List<Identifier> textures) {
		Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
		List<Identifier> previous = descriptions.put(id, List.copyOf(textures));
		if (previous != null) {
			throw new IllegalStateException("Duplicate particle description " + id);
		}
	}

	private static JsonElement toJson(List<Identifier> textures) {
		JsonArray textureArray = new JsonArray(textures.size());
		textures.forEach(texture -> textureArray.add(texture.toString()));
		JsonObject description = new JsonObject();
		description.add("textures", textureArray);
		return description;
	}
}
