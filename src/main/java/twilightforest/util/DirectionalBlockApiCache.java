package twilightforest.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Cached Fabric {@link BlockApiLookup} access keyed by level and position.
 */
public final class DirectionalBlockApiCache<R> {

	private final Map<CacheKey<R>, BlockApiCache<R, Direction>> data = new HashMap<>();

	@Nullable
	public R get(BlockApiLookup<R, Direction> lookup, ServerLevel level, BlockPos pos, Direction direction) {
		CacheKey<R> key = new CacheKey<>(level, pos.immutable(), lookup);
		BlockApiCache<R, Direction> cache = this.data.get(key);
		if (cache == null) {
			cache = BlockApiCache.create(lookup, level, pos);
			this.data.put(key, cache);
		}
		return cache.find(direction);
	}

	private record CacheKey<R>(ServerLevel level, BlockPos pos, BlockApiLookup<R, Direction> lookup) {

	}
}
