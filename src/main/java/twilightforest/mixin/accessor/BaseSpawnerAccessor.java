package twilightforest.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccessor {
	@Accessor("spawnRange")
	int twilightforest$getSpawnRange();

	@Accessor("spawnRange")
	void twilightforest$setSpawnRange(int value);

	@Accessor("maxNearbyEntities")
	void twilightforest$setMaxNearbyEntities(int value);

	@Accessor("spawnCount")
	void twilightforest$setSpawnCount(int value);

	@Invoker("setNextSpawnData")
	void twilightforest$setNextSpawnData(Level level, BlockPos pos, SpawnData spawnData);
}
