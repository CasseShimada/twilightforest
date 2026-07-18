package twilightforest.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccessor {
	@Accessor("spawnDelay")
	int twilightforest$getSpawnDelay();

	@Accessor("spawnDelay")
	void twilightforest$setSpawnDelay(int value);

	@Accessor("spawnCount")
	int twilightforest$getSpawnCount();

	@Accessor("maxNearbyEntities")
	int twilightforest$getMaxNearbyEntities();

	@Accessor("spawnRange")
	int twilightforest$getSpawnRange();

	@Accessor("spawnRange")
	void twilightforest$setSpawnRange(int value);

	@Accessor("maxNearbyEntities")
	void twilightforest$setMaxNearbyEntities(int value);

	@Accessor("spawnCount")
	void twilightforest$setSpawnCount(int value);

	@Accessor("spin")
	double twilightforest$getSpin();

	@Accessor("spin")
	void twilightforest$setSpin(double value);

	@Accessor("oSpin")
	void twilightforest$setOldSpin(double value);

	@Accessor("displayEntity")
	@Nullable Entity twilightforest$getDisplayEntity();

	@Invoker("isNearPlayer")
	boolean twilightforest$isNearPlayer(Level level, BlockPos pos);

	@Invoker("delay")
	void twilightforest$delay(Level level, BlockPos pos);

	@Invoker("getOrCreateNextSpawnData")
	SpawnData twilightforest$getOrCreateNextSpawnData(Level level, RandomSource random, BlockPos pos);

	@Invoker("setNextSpawnData")
	void twilightforest$setNextSpawnData(Level level, BlockPos pos, SpawnData spawnData);
}
