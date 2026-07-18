package twilightforest.item.mapdata;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import twilightforest.network.MazeMapPacket;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TFMazeMapData extends MapItemSavedData {
	private static final Map<String, TFMazeMapData> CLIENT_DATA = new HashMap<>();
	private static final Map<String, SavedDataType<TFMazeMapData>> TYPES = new HashMap<>();
	private static final Codec<TFMazeMapData> CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<TFMazeMapData, T>> decode(DynamicOps<T> ops, T input) {
			return MapItemSavedData.CODEC.decode(ops, input).map(pair -> {
				MapItemSavedData data = pair.getFirst();
				boolean trackingPosition = readBoolean(ops, input, "trackingPosition", true);
				boolean unlimitedTracking = readBoolean(ops, input, "unlimitedTracking", false);
				boolean locked = readBoolean(ops, input, "locked", data.locked);
				TFMazeMapData tfdata = new TFMazeMapData(data.centerX, data.centerZ, data.scale, trackingPosition, unlimitedTracking, locked, data.dimension);

				tfdata.colors = data.colors;
				tfdata.bannerMarkers.putAll(data.bannerMarkers);
				tfdata.decorations.putAll(data.decorations);
				tfdata.frameMarkers.putAll(data.frameMarkers);
				tfdata.trackedDecorationCount = data.trackedDecorationCount;
				tfdata.yCenter = readInt(ops, input, "yCenter", 0);
				tfdata.ore = readBoolean(ops, input, "mapOres", false);

				return Pair.of(tfdata, pair.getSecond());
			});
		}

		@Override
		public <T> DataResult<T> encode(TFMazeMapData value, DynamicOps<T> ops, T prefix) {
			return MapItemSavedData.CODEC.encode(value, ops, prefix)
				.flatMap(tag -> ops.mergeToMap(tag, ops.createString("yCenter"), ops.createInt(value.yCenter)))
				.flatMap(tag -> ops.mergeToMap(tag, ops.createString("mapOres"), ops.createBoolean(value.ore)));
		}
	};

	public int yCenter;
	public boolean ore;

	public TFMazeMapData(int x, int z, byte scale, boolean trackpos, boolean unlimited, boolean locked, ResourceKey<Level> dim) {
		super(x, z, scale, trackpos, unlimited, locked, dim);
	}

	// [VanillaCopy] Adapted from World.getMapData
	@Nullable
	public static TFMazeMapData getMazeMapData(Level level, String name) {
		if (level.isClientSide()) return CLIENT_DATA.get(name);
		else return LegacyMapSavedData.get((ServerLevel) level, name, type(name), CODEC);
	}

	@Nullable
	static TFMazeMapData getMazeMapData(
		SavedDataStorage storage,
		Path currentDataFolder,
		Path legacyWorldDataFolder,
		HolderLookup.Provider registries,
		String name) {
		return LegacyMapSavedData.get(storage, currentDataFolder, legacyWorldDataFolder, registries, name, type(name), CODEC);
	}

	// Like the method above, but if we know we're on client
	@Nullable
	public static TFMazeMapData getClientMagicMapData(String name) {
		return CLIENT_DATA.get(name);
	}

	// [VanillaCopy] Adapted from World.registerMapData
	public static void registerMazeMapData(Level level, TFMazeMapData data, String id) {
		if (level.isClientSide()) CLIENT_DATA.put(id, data);
		else ((ServerLevel) level).getServer().overworld().getDataStorage().set(type(id), data);
	}

	@Nullable
	@Override
	public Packet<?> getUpdatePacket(MapId mapId, Player player) {
		Packet<?> packet = super.getUpdatePacket(mapId, player);
		return packet instanceof ClientboundMapItemDataPacket mapItemDataPacket ? new ClientboundCustomPayloadPacket(new MazeMapPacket(mapItemDataPacket, this.ore, this.yCenter)) : packet;
	}

	private static SavedDataType<TFMazeMapData> type(String id) {
		return TYPES.computeIfAbsent(id, key -> new SavedDataType<>(Identifier.withDefaultNamespace(key), () -> {
			throw new IllegalStateException("Should never create an empty map saved data");
		}, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA));
	}

	private static <T> boolean readBoolean(DynamicOps<T> ops, T input, String key, boolean defaultValue) {
		return ops.get(input, key).result()
			.map(value -> ops.getBooleanValue(value).getOrThrow())
			.orElse(defaultValue);
	}

	private static <T> int readInt(DynamicOps<T> ops, T input, String key, int defaultValue) {
		return ops.get(input, key).result()
			.map(value -> ops.getNumberValue(value).getOrThrow().intValue())
			.orElse(defaultValue);
	}
}
