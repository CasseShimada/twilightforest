package twilightforest.world.components.structures.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// FIXME Using IDs to enumerate lists of mob spawn tables is a bad idea... Using String for now in the config, will transition this implementation detail later
public interface ControlledSpawns {
	WeightedList<MobSpawnSettings.SpawnerData> getCombinedMonsterSpawnableList();

	WeightedList<MobSpawnSettings.SpawnerData> getCombinedCreatureSpawnableList();

	/**
	 * Returns a list of hostile monsters.  Are we ever going to need passive or water creatures?
	 */
	WeightedList<MobSpawnSettings.SpawnerData> getSpawnableList(MobCategory creatureType);

	/**
	 * Returns a list of hostile monsters in the specified indexed category
	 */
	WeightedList<MobSpawnSettings.SpawnerData> getSpawnableMonsterList(int index);

	record ControlledSpawningConfig(Map<String, WeightedList<MobSpawnSettings.SpawnerData>> spawnableMonsterLists, WeightedList<MobSpawnSettings.SpawnerData> ambientCreatureList, WeightedList<MobSpawnSettings.SpawnerData> waterCreatureList, WeightedList<MobSpawnSettings.SpawnerData> combinedMonsterSpawnableCache, WeightedList<MobSpawnSettings.SpawnerData> combinedCreatureSpawnableCache) {
		public static final MapCodec<ControlledSpawningConfig> FLAT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, WeightedList.codec(MobSpawnSettings.SpawnerData.CODEC)).fieldOf("labelled_monster_spawns").forGetter(ControlledSpawningConfig::spawnableMonsterLists),
			WeightedList.codec(MobSpawnSettings.SpawnerData.CODEC).fieldOf("ambient_spawns").forGetter(ControlledSpawningConfig::ambientCreatureList),
			WeightedList.codec(MobSpawnSettings.SpawnerData.CODEC).fieldOf("water_spawns").forGetter(ControlledSpawningConfig::waterCreatureList)
		).apply(instance, ControlledSpawningConfig::create));

		public static final ControlledSpawningConfig EMPTY = create(Map.of(), WeightedList.of(), WeightedList.of());

		public static Weighted<MobSpawnSettings.SpawnerData> weightedSpawn(MobSpawnSettings.SpawnerData data, int weight) {
			return new Weighted<>(data, weight);
		}

		public static Weighted<MobSpawnSettings.SpawnerData> weightedSpawn(net.minecraft.world.entity.EntityType<?> type, int weight, int minCount, int maxCount) {
			return weightedSpawn(new MobSpawnSettings.SpawnerData(type, minCount, maxCount), weight);
		}

		public static ControlledSpawningConfig firstIndexMonsters(MobSpawnSettings.SpawnerData... spawnableMonsterList) {
			return justMonsters(List.of(Arrays.stream(spawnableMonsterList).map(data -> weightedSpawn(data, 1)).toList()));
		}

		public static ControlledSpawningConfig justMonsters(List<List<Weighted<MobSpawnSettings.SpawnerData>>> spawnableMonsterLists) {
			return create(convertMonsterList(spawnableMonsterLists), WeightedList.of(), WeightedList.of());
		}

		public static ControlledSpawningConfig create(List<List<Weighted<MobSpawnSettings.SpawnerData>>> spawnableMonsterLists, List<Weighted<MobSpawnSettings.SpawnerData>> ambientCreatureList, List<Weighted<MobSpawnSettings.SpawnerData>> waterCreatureList) {
			return create(convertMonsterList(spawnableMonsterLists), WeightedList.of(ambientCreatureList), WeightedList.of(waterCreatureList));
		}

		public static ControlledSpawningConfig create(Map<String, WeightedList<MobSpawnSettings.SpawnerData>> spawnableMonsterLists, WeightedList<MobSpawnSettings.SpawnerData> ambientCreatureList, WeightedList<MobSpawnSettings.SpawnerData> waterCreatureList) {
			return new ControlledSpawningConfig(
				spawnableMonsterLists,
				ambientCreatureList,
				waterCreatureList,
				mergeWeightedLists(spawnableMonsterLists.values()),
				mergeWeightedLists(List.of(ambientCreatureList, waterCreatureList))
			);
		}

		private static Map<String, WeightedList<MobSpawnSettings.SpawnerData>> convertMonsterList(List<List<Weighted<MobSpawnSettings.SpawnerData>>> lists) {
			int i = 0;
			Map<String, WeightedList<MobSpawnSettings.SpawnerData>> map = new HashMap<>();

			for (List<Weighted<MobSpawnSettings.SpawnerData>> list : lists) {
				map.put(String.valueOf(i), WeightedList.of(list));
				i++;
			}

			return map;
		}

		private static WeightedList<MobSpawnSettings.SpawnerData> mergeWeightedLists(Iterable<WeightedList<MobSpawnSettings.SpawnerData>> lists) {
			WeightedList.Builder<MobSpawnSettings.SpawnerData> builder = WeightedList.builder();
			for (WeightedList<MobSpawnSettings.SpawnerData> list : lists) {
				for (Weighted<MobSpawnSettings.SpawnerData> entry : list.unwrap()) {
					builder.add(entry.value(), entry.weight());
				}
			}
			return builder.build();
		}

		public WeightedList<MobSpawnSettings.SpawnerData> getForLabel(String index) {
			return this.spawnableMonsterLists().getOrDefault(index, WeightedList.of());
		}
	}
}
